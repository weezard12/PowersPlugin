package me.weezard12.powers.api.builtin;

import me.weezard12.powers.api.PassiveAbility;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Passive ability that heals the player after killing another player.
 */
public final class KillHealPassiveAbility implements PassiveAbility {
    private final String id;
    private final String name;
    private final double healAmount;

    public KillHealPassiveAbility(String id, String name, double healAmount) {
        this.id = BuiltinAbilityUtils.requireNonEmpty(id, "id");
        this.name = BuiltinAbilityUtils.requireNonEmpty(name, "name");
        this.healAmount = BuiltinAbilityUtils.clampMin(healAmount, 0.0);
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
    public void onKill(PlayerDeathEvent event, Player killer, Player victim, Power power) {
        if (killer == null || killer.isDead() || healAmount <= 0.0) {
            return;
        }
        double maxHealth = killer.getMaxHealth();
        double health = killer.getHealth();
        if (health >= maxHealth) {
            return;
        }
        double newHealth = Math.min(maxHealth, health + healAmount);
        if (newHealth > health) {
            killer.setHealth(newHealth);
        }
    }
}
