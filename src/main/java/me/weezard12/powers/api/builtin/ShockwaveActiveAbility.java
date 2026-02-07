package me.weezard12.powers.api.builtin;

import me.weezard12.powers.api.AbilityActivation;
import me.weezard12.powers.api.ActiveAbility;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Active ability that knocks back nearby entities and optionally damages them.
 */
public final class ShockwaveActiveAbility implements ActiveAbility {
    private final String id;
    private final String name;
    private final double radius;
    private final double horizontalStrength;
    private final double verticalStrength;
    private final double damage;
    private final boolean affectPlayers;
    private final boolean affectNonPlayers;
    private final long cooldownSeconds;

    public ShockwaveActiveAbility(String id, String name, double radius, double horizontalStrength,
                                  double verticalStrength, double damage, long cooldownSeconds) {
        this(id, name, radius, horizontalStrength, verticalStrength, damage, true, true, cooldownSeconds);
    }

    public ShockwaveActiveAbility(String id, String name, double radius, double horizontalStrength,
                                  double verticalStrength, double damage,
                                  boolean affectPlayers, boolean affectNonPlayers, long cooldownSeconds) {
        this.id = BuiltinAbilityUtils.requireNonEmpty(id, "id");
        this.name = BuiltinAbilityUtils.requireNonEmpty(name, "name");
        this.radius = BuiltinAbilityUtils.clampMin(radius, 0.0);
        this.horizontalStrength = horizontalStrength;
        this.verticalStrength = verticalStrength;
        this.damage = BuiltinAbilityUtils.clampMin(damage, 0.0);
        this.affectPlayers = affectPlayers;
        this.affectNonPlayers = affectNonPlayers;
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

    public double getRadius() {
        return radius;
    }

    public double getHorizontalStrength() {
        return horizontalStrength;
    }

    public double getVerticalStrength() {
        return verticalStrength;
    }

    public double getDamage() {
        return damage;
    }

    public boolean isAffectPlayers() {
        return affectPlayers;
    }

    public boolean isAffectNonPlayers() {
        return affectNonPlayers;
    }

    @Override
    public long getCooldownSeconds(Player player, Power power) {
        return cooldownSeconds;
    }

    @Override
    public boolean activate(Player player, Power power, AbilityActivation activation) {
        if (player == null || radius <= 0.0) {
            return false;
        }
        boolean handled = false;
        Vector origin = player.getLocation().toVector();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }
            if (entity == player) {
                continue;
            }
            if (entity instanceof Player) {
                if (!affectPlayers) {
                    continue;
                }
            } else if (!affectNonPlayers) {
                continue;
            }
            LivingEntity living = (LivingEntity) entity;
            Vector direction = entity.getLocation().toVector().subtract(origin);
            direction.setY(0.0);
            if (direction.lengthSquared() > 0.0) {
                direction.normalize().multiply(horizontalStrength);
            }
            direction.setY(verticalStrength);
            living.setVelocity(direction);
            if (damage > 0.0) {
                living.damage(damage, player);
            }
            handled = true;
        }
        return handled;
    }
}
