package me.weezard12.powers.api.builtin;

import me.weezard12.powers.api.PassiveAbility;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Player;

/**
 * Passive ability that heals the player on a repeating interval.
 */
public final class PeriodicHealPassiveAbility implements PassiveAbility {
    private static final long DEFAULT_INTERVAL_TICKS = 40L;

    private final String id;
    private final String name;
    private final double healAmount;
    private final long tickIntervalTicks;

    public PeriodicHealPassiveAbility(String id, String name, double healAmount) {
        this(id, name, healAmount, DEFAULT_INTERVAL_TICKS);
    }

    public PeriodicHealPassiveAbility(String id, String name, double healAmount, long tickIntervalTicks) {
        this.id = BuiltinAbilityUtils.requireNonEmpty(id, "id");
        this.name = BuiltinAbilityUtils.requireNonEmpty(name, "name");
        this.healAmount = BuiltinAbilityUtils.clampMin(healAmount, 0.0);
        this.tickIntervalTicks = BuiltinAbilityUtils.normalizeInterval(tickIntervalTicks, DEFAULT_INTERVAL_TICKS);
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
    public long getTickIntervalTicks() {
        return tickIntervalTicks;
    }

    @Override
    public void onTick(Player player, Power power, long tick) {
        if (player == null || !player.isOnline() || player.isDead()) {
            return;
        }
        if (healAmount <= 0.0) {
            return;
        }
        double maxHealth = player.getMaxHealth();
        double health = player.getHealth();
        if (health >= maxHealth) {
            return;
        }
        double newHealth = Math.min(maxHealth, health + healAmount);
        if (newHealth > health) {
            player.setHealth(newHealth);
        }
    }
}
