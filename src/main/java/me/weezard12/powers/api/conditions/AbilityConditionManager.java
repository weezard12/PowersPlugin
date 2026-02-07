package me.weezard12.powers.api.conditions;

import java.util.UUID;
import me.weezard12.powers.api.Ability;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Runtime manager for ability condition state.
 */
public interface AbilityConditionManager {
    boolean isAbilityEnabled(Player player, Power power, Ability ability);

    /**
     * Dispatch a custom trigger key for the player.
     * @return true if any active ability handled the trigger.
     */
    boolean dispatchCustomTrigger(Player player, String triggerKey);

    /**
     * Dispatch a custom trigger key with optional event context.
     * @return true if any active ability handled the trigger.
     */
    boolean dispatchCustomTrigger(Player player, String triggerKey, Object sourceEvent, ItemStack eventItem);

    /**
     * Notification hook invoked after a power is added to a player.
     */
    default void onPowerAdded(Player player, Power power) {
    }

    /**
     * Notification hook invoked after a power is removed from a player.
     */
    default void onPowerRemoved(Player player, Power power) {
    }

    /**
     * Notification hook invoked after all powers are cleared for a player.
     */
    default void onPowersCleared(Player player) {
    }

    void clearPlayerState(Player player);

    void clearPlayerState(UUID playerId);
}
