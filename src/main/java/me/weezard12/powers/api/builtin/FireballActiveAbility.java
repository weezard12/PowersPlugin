package me.weezard12.powers.api.builtin;

import me.weezard12.powers.api.AbilityActivation;
import me.weezard12.powers.api.ActiveAbility;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;

/**
 * Active ability that launches a fireball.
 */
public final class FireballActiveAbility implements ActiveAbility {
    private final String id;
    private final String name;
    private final float yield;
    private final boolean incendiary;
    private final double speedMultiplier;
    private final long cooldownSeconds;

    public FireballActiveAbility(String id, String name, float yield, boolean incendiary) {
        this(id, name, yield, incendiary, 1.0, 0L);
    }

    public FireballActiveAbility(String id, String name, float yield, boolean incendiary,
                                 double speedMultiplier, long cooldownSeconds) {
        this.id = BuiltinAbilityUtils.requireNonEmpty(id, "id");
        this.name = BuiltinAbilityUtils.requireNonEmpty(name, "name");
        this.yield = (float) BuiltinAbilityUtils.clampMin(yield, 0.0f);
        this.incendiary = incendiary;
        this.speedMultiplier = speedMultiplier;
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

    public float getYield() {
        return yield;
    }

    public boolean isIncendiary() {
        return incendiary;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
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
        Fireball fireball = player.launchProjectile(Fireball.class);
        if (fireball == null) {
            return false;
        }
        fireball.setYield(yield);
        fireball.setIsIncendiary(incendiary);
        if (speedMultiplier > 0.0 && speedMultiplier != 1.0) {
            fireball.setVelocity(fireball.getVelocity().multiply(speedMultiplier));
        }
        return true;
    }
}
