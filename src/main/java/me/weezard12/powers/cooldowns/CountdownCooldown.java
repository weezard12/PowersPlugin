package me.weezard12.powers.cooldowns;

import org.bukkit.entity.Player;

public class CountdownCooldown extends Cooldown {

    private final long decrementIntervalTicks;
    private long lastDecrementTick = 0;
    private long currentCountdown;

    /**
     * Constructor using seconds for total time and interval.
     * @param player Player affected
     * @param id Cooldown ID
     * @param maxSeconds Total duration in seconds
     * @param decrementIntervalSeconds Interval between countdown ticks in seconds
     */
    public CountdownCooldown(Player player, String id, int maxSeconds, int decrementIntervalSeconds) {
        super(player, id, maxSeconds * 20L);
        this.decrementIntervalTicks = decrementIntervalSeconds * 20L;
        this.currentCountdown = maxSeconds;
    }

    /**
     * Constructor using ticks for both total time and interval.
     * @param player Player affected
     * @param id Cooldown ID
     * @param maxTicks Total duration in ticks
     * @param decrementIntervalTicks Interval between countdown ticks in ticks
     */
    public CountdownCooldown(Player player, String id, long maxTicks, long decrementIntervalTicks) {
        super(player, id, maxTicks);
        this.decrementIntervalTicks = decrementIntervalTicks;
        this.currentCountdown = maxTicks / decrementIntervalTicks;
    }

    @Override
    public void onTick() {
        if ((ticksElapsed - lastDecrementTick) >= decrementIntervalTicks) {
            currentCountdown--;
            lastDecrementTick = ticksElapsed;

        }
    }

    @Override
    public void onFinish() {
        player.sendMessage("Cooldown [" + id + "] has ended.");
    }

    public long getCurrentCountdown() {
        return currentCountdown;
    }
}
