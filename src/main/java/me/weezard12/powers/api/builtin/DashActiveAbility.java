package me.weezard12.powers.api.builtin;

import me.weezard12.powers.api.AbilityActivation;
import me.weezard12.powers.api.ActiveAbility;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Active ability that pushes the player forward with optional upward lift.
 */
public final class DashActiveAbility implements ActiveAbility {
    private final String id;
    private final String name;
    private final double forwardVelocity;
    private final double upwardVelocity;
    private final boolean addToCurrent;
    private final long cooldownSeconds;

    public DashActiveAbility(String id, String name, double forwardVelocity, double upwardVelocity) {
        this(id, name, forwardVelocity, upwardVelocity, false, 0L);
    }

    public DashActiveAbility(String id, String name, double forwardVelocity, double upwardVelocity,
                             boolean addToCurrent, long cooldownSeconds) {
        this.id = BuiltinAbilityUtils.requireNonEmpty(id, "id");
        this.name = BuiltinAbilityUtils.requireNonEmpty(name, "name");
        this.forwardVelocity = forwardVelocity;
        this.upwardVelocity = upwardVelocity;
        this.addToCurrent = addToCurrent;
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

    public double getForwardVelocity() {
        return forwardVelocity;
    }

    public double getUpwardVelocity() {
        return upwardVelocity;
    }

    public boolean isAddToCurrent() {
        return addToCurrent;
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
        if (forwardVelocity == 0.0 && upwardVelocity == 0.0) {
            return false;
        }
        Vector direction = player.getLocation().getDirection();
        Vector velocity;
        if (direction == null || direction.lengthSquared() == 0.0) {
            velocity = new Vector(0.0, 0.0, 0.0);
        } else {
            velocity = direction.normalize().multiply(forwardVelocity);
        }
        velocity.setY(upwardVelocity);
        if (addToCurrent) {
            velocity.add(player.getVelocity());
        }
        player.setVelocity(velocity);
        return true;
    }
}
