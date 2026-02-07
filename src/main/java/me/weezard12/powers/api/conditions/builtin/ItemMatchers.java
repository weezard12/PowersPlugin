package me.weezard12.powers.api.conditions.builtin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Common item matcher implementations.
 */
public final class ItemMatchers {
    private ItemMatchers() {
    }

    public static ItemMatcher anyItem() {
        return itemStack -> itemStack != null && itemStack.getType() != Material.AIR;
    }

    public static ItemMatcher material(Material material) {
        if (material == null) {
            throw new IllegalArgumentException("material cannot be null");
        }
        return itemStack -> itemStack != null && itemStack.getType() == material;
    }

    public static ItemMatcher similar(ItemStack template) {
        if (template == null || template.getType() == Material.AIR) {
            throw new IllegalArgumentException("template cannot be null/air");
        }
        final ItemStack copy = template.clone();
        return itemStack -> itemStack != null && itemStack.getType() != Material.AIR && itemStack.isSimilar(copy);
    }

    public static ItemMatcher not(ItemMatcher matcher) {
        if (matcher == null) {
            throw new IllegalArgumentException("matcher cannot be null");
        }
        return itemStack -> !matcher.matches(itemStack);
    }

    public static ItemMatcher anyOf(Collection<ItemMatcher> matchers) {
        final List<ItemMatcher> copy = normalized(matchers);
        return itemStack -> {
            for (ItemMatcher matcher : copy) {
                if (matcher.matches(itemStack)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static ItemMatcher allOf(Collection<ItemMatcher> matchers) {
        final List<ItemMatcher> copy = normalized(matchers);
        return itemStack -> {
            for (ItemMatcher matcher : copy) {
                if (!matcher.matches(itemStack)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static List<ItemMatcher> normalized(Collection<ItemMatcher> matchers) {
        if (matchers == null || matchers.isEmpty()) {
            throw new IllegalArgumentException("matchers cannot be empty");
        }
        List<ItemMatcher> copy = new ArrayList<ItemMatcher>();
        for (ItemMatcher matcher : matchers) {
            if (matcher != null) {
                copy.add(matcher);
            }
        }
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("matchers cannot be empty");
        }
        return copy;
    }
}
