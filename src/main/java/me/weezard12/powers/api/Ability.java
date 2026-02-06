package me.weezard12.powers.api;

import java.util.Collections;
import java.util.List;
import me.weezard12.powers.cooldowns.AbilityCooldown;
import me.weezard12.powers.cooldowns.Cooldown;
import me.weezard12.powers.cooldowns.CooldownManager;
import org.bukkit.entity.Player;

public interface Ability {
    String getId();

    String getName();

    /**
     * Optional description for menus or tooltips.
     */
    default List<String> getDescription() {
        return Collections.emptyList();
    }

    /**
     * Optional icon key for resource-pack driven textures.
     */
    default String getIconKey() {
        return null;
    }

    default void onGrant(Player player, Power power) {
        // Optional: override for grant-side effects.
    }

    default void onRevoke(Player player, Power power) {
        // Optional: override for revoke-side effects.
    }

    /**
     * Implement the ability's action. Return true if activation was handled.
     */
    default boolean activate(Player player, Power power) {
        return false;
    }

    /**
     * Cooldown ID for this ability. Defaults to the ability ID.
     */
    default String getCooldownId() {
        return getId();
    }

    /**
     * Cooldown duration in seconds. Return 0 or less for no cooldown.
     */
    default long getCooldownSeconds(Player player, Power power) {
        return 0L;
    }

    /**
     * Returns true if this ability should use cooldowns.
     */
    default boolean hasCooldown(Player player, Power power) {
        return getCooldownSeconds(player, power) > 0L;
    }

    /**
     * Create a cooldown instance for this ability. Override for custom behavior.
     */
    default Cooldown createCooldown(Player player, Power power) {
        long seconds = getCooldownSeconds(player, power);
        return seconds > 0L ? new AbilityCooldown(player, getCooldownId(), seconds) : null;
    }

    /**
     * Returns true if the ability is currently on cooldown for the player.
     */
    default boolean isOnCooldown(Player player, Power power) {
        return hasCooldown(player, power) && CooldownManager.isOnCooldown(player, getCooldownId());
    }

    /**
     * Convenience overload when power is not relevant.
     */
    default boolean isOnCooldown(Player player) {
        return isOnCooldown(player, null);
    }

    /**
     * Activates the ability and starts its cooldown if configured.
     * Returns true if activation succeeded.
     */
    default boolean tryActivate(Player player, Power power) {
        if (player == null) {
            return false;
        }
        if (hasCooldown(player, power) && CooldownManager.isOnCooldown(player, getCooldownId())) {
            return false;
        }
        boolean handled = activate(player, power);
        if (handled && hasCooldown(player, power)) {
            Cooldown cooldown = createCooldown(player, power);
            if (cooldown != null) {
                CooldownManager.startCooldown(cooldown);
            }
        }
        return handled;
    }
}
