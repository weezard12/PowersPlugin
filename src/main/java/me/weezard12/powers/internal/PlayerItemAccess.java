package me.weezard12.powers.internal;

import java.lang.reflect.Method;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

final class PlayerItemAccess {
    private PlayerItemAccess() {
    }

    static ItemStack getMainHand(Player player) {
        if (player == null) {
            return null;
        }
        return cloneOrNull(player.getItemInHand());
    }

    static ItemStack getOffHand(Player player) {
        if (player == null) {
            return null;
        }
        try {
            Object inventory = player.getInventory();
            Method method = inventory.getClass().getMethod("getItemInOffHand");
            Object value = method.invoke(inventory);
            return value instanceof ItemStack ? cloneOrNull((ItemStack) value) : null;
        } catch (Exception ignored) {
            // 1.8 has no offhand.
            return null;
        }
    }

    static ItemStack cloneOrNull(ItemStack itemStack) {
        if (isAir(itemStack)) {
            return null;
        }
        return itemStack.clone();
    }

    static boolean isAir(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }
}
