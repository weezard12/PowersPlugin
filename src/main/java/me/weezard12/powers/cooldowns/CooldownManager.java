package me.weezard12.powers.cooldowns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class CooldownManager {
    private static Plugin plugin;

    // player UUID -> (normalized cooldown id -> entry)
    private static final Map<UUID, Map<String, CooldownEntry>> activeCooldowns =
            new ConcurrentHashMap<UUID, Map<String, CooldownEntry>>();

    private CooldownManager() {
        // prevent instantiation
    }

    public static synchronized void init(Plugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static synchronized void startCooldown(Cooldown cooldown) {
        if (cooldown == null || cooldown.getPlayer() == null) {
            return;
        }
        if (plugin == null) {
            throw new IllegalStateException("CooldownManager.init(plugin) must be called before starting cooldowns.");
        }

        String normalizedId = normalizeId(cooldown.getId());
        if (normalizedId.isEmpty()) {
            return;
        }

        UUID playerId = cooldown.getPlayer().getUniqueId();
        Map<String, CooldownEntry> cooldownMap = getOrCreateMap(playerId);

        CooldownEntry existing = cooldownMap.remove(normalizedId);
        if (existing != null) {
            Bukkit.getScheduler().cancelTask(existing.taskId);
            existing.cooldown.stopInternal();
        }

        int taskId;
        if (cooldown instanceof LongDurationCooldown) {
            long delayTicks = Math.max(1L, cooldown.getTicksRemaining());
            taskId = Bukkit.getScheduler().runTaskLater(plugin, cooldown::finish, delayTicks).getTaskId();
        } else {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, cooldown, 1L, 1L);
        }

        cooldownMap.put(normalizedId, new CooldownEntry(cooldown, taskId));
    }

    public static synchronized CountdownCooldown startCountdownCooldown(Player player, String id, int seconds,
                                                                        int decrementIntervalSeconds) {
        CountdownCooldown countdownCooldown = new CountdownCooldown(player, id, seconds, decrementIntervalSeconds);
        startCooldown(countdownCooldown);
        return countdownCooldown;
    }

    /**
     * Start a long-duration cooldown that is efficient for hours/days.
     * It schedules a single delayed task to finish, avoiding per-tick overhead.
     * @param player target player
     * @param id cooldown id
     * @param durationSeconds total duration in seconds
     * @return the created LongDurationCooldown
     */
    public static synchronized LongDurationCooldown startLongDurationCooldown(Player player, String id, long durationSeconds) {
        LongDurationCooldown longCd = new LongDurationCooldown(player, id, durationSeconds);
        startCooldown(longCd);
        return longCd;
    }

    public static synchronized boolean isOnCooldown(Player player, String id) {
        if (player == null) {
            return false;
        }
        Map<String, CooldownEntry> cooldownMap = activeCooldowns.get(player.getUniqueId());
        if (cooldownMap == null) {
            return false;
        }
        CooldownEntry entry = cooldownMap.get(normalizeId(id));
        return entry != null && entry.cooldown.isRunning();
    }

    public static synchronized void removeCooldown(Player player, String id) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUniqueId();
        Map<String, CooldownEntry> cooldownMap = activeCooldowns.get(playerId);
        if (cooldownMap == null) {
            return;
        }
        CooldownEntry entry = cooldownMap.remove(normalizeId(id));
        if (entry == null) {
            return;
        }
        Bukkit.getScheduler().cancelTask(entry.taskId);
        entry.cooldown.stopInternal();
        if (cooldownMap.isEmpty()) {
            activeCooldowns.remove(playerId);
        }
    }

    static synchronized void removeCooldown(Cooldown cooldown) {
        if (cooldown == null || cooldown.getPlayer() == null) {
            return;
        }
        UUID playerId = cooldown.getPlayer().getUniqueId();
        Map<String, CooldownEntry> cooldownMap = activeCooldowns.get(playerId);
        if (cooldownMap == null) {
            return;
        }

        String normalizedId = normalizeId(cooldown.getId());
        CooldownEntry entry = cooldownMap.get(normalizedId);
        if (entry == null || entry.cooldown != cooldown) {
            return;
        }

        Bukkit.getScheduler().cancelTask(entry.taskId);
        cooldownMap.remove(normalizedId);
        if (cooldownMap.isEmpty()) {
            activeCooldowns.remove(playerId);
        }
    }

    public static synchronized Set<Cooldown> getCooldowns(Player player) {
        if (player == null) {
            return Collections.emptySet();
        }
        Map<String, CooldownEntry> map = activeCooldowns.get(player.getUniqueId());
        if (map == null || map.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Cooldown> cooldowns = new HashSet<Cooldown>();
        for (CooldownEntry entry : map.values()) {
            if (entry != null && entry.cooldown != null) {
                cooldowns.add(entry.cooldown);
            }
        }
        return cooldowns;
    }

    public static synchronized void clearAll() {
        for (Map<String, CooldownEntry> cooldownMap : new ArrayList<Map<String, CooldownEntry>>(activeCooldowns.values())) {
            for (CooldownEntry entry : new ArrayList<CooldownEntry>(cooldownMap.values())) {
                Bukkit.getScheduler().cancelTask(entry.taskId);
                entry.cooldown.stopInternal();
            }
            cooldownMap.clear();
        }
        activeCooldowns.clear();
    }

    private static Map<String, CooldownEntry> getOrCreateMap(UUID playerId) {
        Map<String, CooldownEntry> cooldownMap = activeCooldowns.get(playerId);
        if (cooldownMap != null) {
            return cooldownMap;
        }
        Map<String, CooldownEntry> created = new ConcurrentHashMap<String, CooldownEntry>();
        Map<String, CooldownEntry> existing = activeCooldowns.putIfAbsent(playerId, created);
        return existing == null ? created : existing;
    }

    private static String normalizeId(String id) {
        if (id == null) {
            return "";
        }
        return id.trim().toLowerCase(Locale.ROOT);
    }

    private static final class CooldownEntry {
        private final Cooldown cooldown;
        private final int taskId;

        private CooldownEntry(Cooldown cooldown, int taskId) {
            this.cooldown = cooldown;
            this.taskId = taskId;
        }
    }
}
