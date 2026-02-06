package me.weezard12.powers.api;

import org.bukkit.entity.Player;

public interface AbilityActivationDispatcher {
    /**
     * Dispatch an activation to all applicable active abilities.
     * @return true if any ability handled the activation
     */
    boolean dispatch(Player player, AbilityActivation activation);
}
