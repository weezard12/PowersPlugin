package me.weezard12.powers.api;

import org.bukkit.entity.Player;

public interface Ability {
    String getId();

    String getName();

    default void onGrant(Player player, Power power) {
        // Optional: override for grant-side effects.
    }

    default void onRevoke(Player player, Power power) {
        // Optional: override for revoke-side effects.
    }

    default boolean activate(Player player, Power power) {
        // Optional: return true if activation was handled.
        return false;
    }
}
