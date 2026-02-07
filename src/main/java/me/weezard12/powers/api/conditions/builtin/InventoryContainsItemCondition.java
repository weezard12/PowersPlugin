package me.weezard12.powers.api.conditions.builtin;

import me.weezard12.powers.api.conditions.AbilityCondition;
import me.weezard12.powers.api.conditions.AbilityConditionContext;
import me.weezard12.powers.api.conditions.ConditionTrigger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Matches while the player's inventory contains a matching item.
 */
public final class InventoryContainsItemCondition implements AbilityCondition {
    private final ItemMatcher matcher;

    public InventoryContainsItemCondition(ItemMatcher matcher) {
        if (matcher == null) {
            throw new IllegalArgumentException("matcher cannot be null");
        }
        this.matcher = matcher;
    }

    @Override
    public boolean supports(String triggerKey) {
        return ConditionTrigger.fromString(triggerKey) == ConditionTrigger.HOLD_INVENTORY;
    }

    @Override
    public boolean supports(ConditionTrigger trigger) {
        return trigger == ConditionTrigger.HOLD_INVENTORY;
    }

    @Override
    public boolean matches(AbilityConditionContext context) {
        if (context == null || context.getTrigger() != ConditionTrigger.HOLD_INVENTORY) {
            return false;
        }
        Player player = context.getPlayer();
        if (player == null || player.getInventory() == null) {
            return false;
        }
        ItemStack[] contents = player.getInventory().getContents();
        if (contents == null || contents.length == 0) {
            return false;
        }
        for (ItemStack itemStack : contents) {
            if (itemStack != null && itemStack.getType() != Material.AIR && matcher.matches(itemStack)) {
                return true;
            }
        }
        return false;
    }
}
