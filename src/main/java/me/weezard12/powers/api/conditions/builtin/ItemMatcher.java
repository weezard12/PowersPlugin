package me.weezard12.powers.api.conditions.builtin;

import org.bukkit.inventory.ItemStack;

/**
 * Matches item stacks for item-based conditions.
 */
public interface ItemMatcher {
    boolean matches(ItemStack itemStack);
}
