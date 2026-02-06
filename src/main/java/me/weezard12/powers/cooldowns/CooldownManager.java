package me.weezard12.powers.cooldowns;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public final class CooldownManager {

    private static Plugin plugin;

    // cooldowns map: player UUID -> (Cooldown -> taskId)
    private static final Map<UUID, Map<Cooldown, Integer>> activeCooldowns = new HashMap<>();

    private CooldownManager() {
        // prevent instantiation
    }

    public static void init(Plugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static void startCooldown(Cooldown cooldown) {
        if (cooldown == null) {
            return;
        }
        if (plugin == null) {
            throw new IllegalStateException("CooldownManager.init(plugin) must be called before starting cooldowns.");
        }

        int taskId;
        if (cooldown instanceof LongDurationCooldown) {
            long delayTicks = Math.max(1L, cooldown.getTicksRemaining());
            taskId = Bukkit.getScheduler().runTaskLater(plugin, cooldown::finish, delayTicks).getTaskId();
        } else {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, cooldown, 1L, 1L);
        }

        activeCooldowns.computeIfAbsent(cooldown.getPlayer().getUniqueId(), k -> new HashMap<>())
                .put(cooldown, taskId);
    }

    public static CountdownCooldown startCountdownCooldown(Player player, String id, int seconds, int decrementIntervalSeconds) {
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
    public static LongDurationCooldown startLongDurationCooldown(Player player, String id, long durationSeconds) {
        LongDurationCooldown longCd = new LongDurationCooldown(player, id, durationSeconds);
        startCooldown(longCd);
        return longCd;
    }

    public static boolean isOnCooldown(Player player, String id) {
        Map<Cooldown, Integer> cooldownMap = activeCooldowns.get(player.getUniqueId());
        if (cooldownMap == null) return false;

        for (Cooldown cooldown : cooldownMap.keySet()) {
            if (cooldown.getId().equalsIgnoreCase(id)) {
                return cooldown.isRunning();
            }
        }
        return false;
    }

    public static void removeCooldown(Player player, String id) {
        Map<Cooldown, Integer> cooldownMap = activeCooldowns.get(player.getUniqueId());
        if (cooldownMap == null) return;

        Iterator<Map.Entry<Cooldown, Integer>> it = cooldownMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Cooldown, Integer> entry = it.next();
            if (entry.getKey().getId().equalsIgnoreCase(id)) {
                Bukkit.getScheduler().cancelTask(entry.getValue());
                it.remove();
                break;
            }
        }

        if (cooldownMap.isEmpty()) {
            activeCooldowns.remove(player.getUniqueId());
        }
    }

    public static Set<Cooldown> getCooldowns(Player player) {
        Map<Cooldown, Integer> map = activeCooldowns.get(player.getUniqueId());
        return map != null ? new HashSet<>(map.keySet()) : Collections.emptySet();
    }
}
