package me.weezard12.powers.api;

import me.weezard12.powers.cooldowns.Cooldown;
import me.weezard12.powers.cooldowns.CooldownManager;
import org.bukkit.entity.Player;

/**
 * Active abilities are triggered by player actions (right-click, shift-right-click, etc).
 */
public interface ActiveAbility extends Ability {

    /**
     * Handle an activation of this ability with an explicit activation type.
     * Return true if activation was handled.
     */
    boolean activate(Player player, Power power, AbilityActivation activation);

    /**
     * Fallback activation without a specific trigger.
     */
    @Override
    default boolean activate(Player player, Power power) {
        return activate(player, power, AbilityActivation.UNKNOWN);
    }

    /**
     * Try to activate with cooldown checks.
     */
    default boolean tryActivate(Player player, Power power, AbilityActivation activation) {
        if (player == null) {
            return false;
        }
        if (hasCooldown(player, power) && CooldownManager.isOnCooldown(player, getCooldownId())) {
            return false;
        }
        boolean handled = activate(player, power, activation);
        if (handled && hasCooldown(player, power)) {
            Cooldown cooldown = createCooldown(player, power);
            if (cooldown != null) {
                CooldownManager.startCooldown(cooldown);
            }
        }
        return handled;
    }

    default boolean onRightClick(Player player, Power power) {
        return tryActivate(player, power, AbilityActivation.RIGHT_CLICK);
    }

    default boolean onShiftRightClick(Player player, Power power) {
        return tryActivate(player, power, AbilityActivation.SHIFT_RIGHT_CLICK);
    }

    default boolean onLeftClick(Player player, Power power) {
        return tryActivate(player, power, AbilityActivation.LEFT_CLICK);
    }

    default boolean onShiftLeftClick(Player player, Power power) {
        return tryActivate(player, power, AbilityActivation.SHIFT_LEFT_CLICK);
    }

    default boolean onUltimate(Player player, Power power) {
        return tryActivate(player, power, AbilityActivation.ULTIMATE);
    }
}
