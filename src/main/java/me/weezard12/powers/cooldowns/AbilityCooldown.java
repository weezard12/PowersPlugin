package me.weezard12.powers.cooldowns;

import org.bukkit.entity.Player;

/**
 * Default cooldown used by abilities. Silent on finish.
 */
public class AbilityCooldown extends LongDurationCooldown {

    public AbilityCooldown(Player player, String id, long durationSeconds) {
        super(player, id, durationSeconds);
    }

    @Override
    public void onFinish() {
        // no-op: abilities decide if they want to message on cooldown end
    }
}
