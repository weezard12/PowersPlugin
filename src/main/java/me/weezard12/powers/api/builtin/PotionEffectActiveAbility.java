package me.weezard12.powers.api.builtin;

import me.weezard12.powers.api.AbilityActivation;
import me.weezard12.powers.api.ActiveAbility;
import me.weezard12.powers.api.PotionEffects;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Active ability that applies a potion effect to the player and nearby players.
 */
public final class PotionEffectActiveAbility implements ActiveAbility {
    private final String id;
    private final String name;
    private final PotionEffectType type;
    private final int amplifier;
    private final int durationTicks;
    private final boolean ambient;
    private final boolean particles;
    private final boolean icon;
    private final double radius;
    private final boolean includeSelf;
    private final long cooldownSeconds;

    public PotionEffectActiveAbility(String id, String name, String effectName, int amplifier, int durationTicks) {
        this(id, name, effectName, amplifier, durationTicks, false, true, true, 0.0, true, 0L);
    }

    public PotionEffectActiveAbility(String id, String name, String effectName, int amplifier, int durationTicks,
                                     boolean ambient, boolean particles, boolean icon,
                                     double radius, boolean includeSelf, long cooldownSeconds) {
        this.id = BuiltinAbilityUtils.requireNonEmpty(id, "id");
        this.name = BuiltinAbilityUtils.requireNonEmpty(name, "name");
        PotionEffectType resolved = PotionEffects.resolve(effectName);
        if (resolved == null) {
            throw new IllegalArgumentException("Unknown potion effect: " + effectName);
        }
        this.type = resolved;
        this.amplifier = Math.max(0, amplifier);
        this.durationTicks = Math.max(1, durationTicks);
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
        this.radius = BuiltinAbilityUtils.clampMin(radius, 0.0);
        this.includeSelf = includeSelf;
        this.cooldownSeconds = Math.max(0L, cooldownSeconds);
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

    public double getRadius() {
        return radius;
    }

    public boolean isIncludeSelf() {
        return includeSelf;
    }

    @Override
    public long getCooldownSeconds(Player player, Power power) {
        return cooldownSeconds;
    }

    @Override
    public boolean activate(Player player, Power power, AbilityActivation activation) {
        if (player == null) {
            return false;
        }
        boolean applied = false;
        if (includeSelf) {
            apply(player);
            applied = true;
        }
        if (radius > 0.0) {
            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (!(entity instanceof Player)) {
                    continue;
                }
                Player target = (Player) entity;
                if (!includeSelf && target.equals(player)) {
                    continue;
                }
                apply(target);
                applied = true;
            }
        }
        return applied;
    }

    private void apply(Player target) {
        if (target == null || !target.isOnline()) {
            return;
        }
        PotionEffect effect = PotionEffects.createEffect(type, durationTicks, amplifier, ambient, particles, icon);
        target.addPotionEffect(effect, true);
    }
}
