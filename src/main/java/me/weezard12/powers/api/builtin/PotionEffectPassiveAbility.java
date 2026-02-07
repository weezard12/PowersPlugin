package me.weezard12.powers.api.builtin;

import java.util.Collection;
import me.weezard12.powers.api.AbilityActivation;
import me.weezard12.powers.api.PassiveAbility;
import me.weezard12.powers.api.PotionEffects;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Built-in passive ability that keeps a potion effect active.
 */
public final class PotionEffectPassiveAbility implements PassiveAbility {
    private final String id;
    private final String name;
    private final PotionEffectType type;
    private final int amplifier;
    private final int durationTicks;
    private final boolean ambient;
    private final boolean particles;
    private final boolean icon;
    private final long tickIntervalTicks;
    private final int refreshThresholdTicks;

    public PotionEffectPassiveAbility(String id, String name, String effectName, int amplifier) {
        this(id, name, effectName, amplifier, 200, false, true, true, 20L);
    }

    public PotionEffectPassiveAbility(String id, String name, String effectName, int amplifier, int durationTicks,
                                      boolean ambient, boolean particles, boolean icon, long tickIntervalTicks) {
        this.id = requireNonEmpty(id, "id");
        this.name = requireNonEmpty(name, "name");
        PotionEffectType resolved = PotionEffects.resolve(effectName);
        if (resolved == null) {
            throw new IllegalArgumentException("Unknown potion effect: " + effectName);
        }
        this.type = resolved;
        this.amplifier = Math.max(0, amplifier);
        long interval = tickIntervalTicks <= 0L ? 20L : tickIntervalTicks;
        int minDuration = (int) Math.max(40L, interval * 2L);
        this.durationTicks = Math.max(minDuration, durationTicks);
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
        this.tickIntervalTicks = interval;
        this.refreshThresholdTicks = Math.max(20, this.durationTicks / 3);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public PotionEffectType getEffectType() {
        return type;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public long getTickIntervalTicks() {
        return tickIntervalTicks;
    }

    @Override
    public void onJoin(PlayerJoinEvent event, Player player, Power power) {
        apply(player);
    }

    @Override
    public void onQuit(PlayerQuitEvent event, Player player, Power power) {
        // no-op
    }

    @Override
    public void onDamage(EntityDamageEvent event, Player player, Power power) {
        // no-op
    }

    @Override
    public void onDeath(PlayerDeathEvent event, Player player, Power power) {
        // no-op
    }

    @Override
    public void onKill(PlayerDeathEvent event, Player killer, Player victim, Power power) {
        // no-op
    }

    @Override
    public void onTick(Player player, Power power, long tick) {
        apply(player);
    }

    private void apply(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        PotionEffect current = getActiveEffect(player, type);
        if (current != null) {
            if (current.getAmplifier() > amplifier && current.getDuration() > refreshThresholdTicks) {
                return;
            }
            if (current.getAmplifier() == amplifier && current.getDuration() > refreshThresholdTicks) {
                return;
            }
        }
        PotionEffect effect = PotionEffects.createEffect(type, durationTicks, amplifier, ambient, particles, icon);
        player.addPotionEffect(effect, true);
    }

    private static PotionEffect getActiveEffect(Player player, PotionEffectType type) {
        Collection<PotionEffect> effects = player.getActivePotionEffects();
        if (effects == null) {
            return null;
        }
        for (PotionEffect effect : effects) {
            if (effect != null && effect.getType().equals(type)) {
                return effect;
            }
        }
        return null;
    }

    private static String requireNonEmpty(String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " cannot be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty");
        }
        return trimmed;
    }
}
