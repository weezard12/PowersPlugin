package me.weezard12.powers.api.builtin;

import me.weezard12.powers.api.AbilityActivation;
import me.weezard12.powers.api.ActiveAbility;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Player;

/**
 * Active ability that heals the player.
 */
public final class HealActiveAbility implements ActiveAbility {
    private final String id;
    private final String name;
    private final double healAmount;
    private final long cooldownSeconds;

    public HealActiveAbility(String id, String name, double healAmount) {
        this(id, name, healAmount, 0L);
    }

    public HealActiveAbility(String id, String name, double healAmount, long cooldownSeconds) {
        this.id = BuiltinAbilityUtils.requireNonEmpty(id, "id");
        this.name = BuiltinAbilityUtils.requireNonEmpty(name, "name");
        this.healAmount = BuiltinAbilityUtils.clampMin(healAmount, 0.0);
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

    public double getHealAmount() {
        return healAmount;
    }

    @Override
    public long getCooldownSeconds(Player player, Power power) {
        return cooldownSeconds;
    }

    @Override
    public boolean activate(Player player, Power power, AbilityActivation activation) {
        if (player == null || player.isDead()) {
            return false;
        }
        if (healAmount <= 0.0) {
            return false;
        }
        double maxHealth = player.getMaxHealth();
        double health = player.getHealth();
        if (health >= maxHealth) {
            return false;
        }
        double newHealth = Math.min(maxHealth, health + healAmount);
        if (newHealth > health) {
            player.setHealth(newHealth);
            return true;
        }
        return false;
    }
}
