package me.weezard12.powers.api;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Passive abilities are driven by events and ticks (no direct activation).
 */
public interface PassiveAbility extends Ability {

    /**
     * Called when the player joins the server.
     */
    default void onJoin(PlayerJoinEvent event, Player player, Power power) {
    }

    /**
     * Called when the player quits the server.
     */
    default void onQuit(PlayerQuitEvent event, Player player, Power power) {
    }

    /**
     * Called when the player takes damage.
     */
    default void onDamage(EntityDamageEvent event, Player player, Power power) {
    }

    /**
     * Called when the player dies.
     */
    default void onDeath(PlayerDeathEvent event, Player player, Power power) {
    }

    /**
     * Called when the player kills another player.
     */
    default void onKill(PlayerDeathEvent event, Player killer, Player victim, Power power) {
    }

    /**
     * Called on a repeating interval while the player is online.
     * @param tick a monotonically increasing tick counter
     */
    default void onTick(Player player, Power power, long tick) {
    }

    /**
     * How often to call onTick, in server ticks. Return <= 0 to disable ticking.
     */
    default long getTickIntervalTicks() {
        return 20L;
    }
}
