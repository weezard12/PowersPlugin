package me.weezard12.powers.cooldowns;

import org.bukkit.entity.Player;

/**
 * A cooldown optimized for very long durations (hours/days).
 * It does not tick every server tick; instead it computes remaining time
 * from wall-clock timestamps and relies on a single delayed task to finish.
 */
public class LongDurationCooldown extends Cooldown {

    private final long startMillis;
    private final long endMillis;

    /**
     * Create a long-duration cooldown using seconds.
     * @param player target player
     * @param id cooldown id
     * @param durationSeconds total duration in seconds (supports hours/days)
     */
    public LongDurationCooldown(Player player, String id, long durationSeconds) {
        super(player, id, durationSeconds * 20L);
        this.startMillis = System.currentTimeMillis();
        this.endMillis = startMillis + (durationSeconds * 1000L);
    }

    @Override
    public void onTick() {
        // no-op: we don't run per-tick for long cooldowns
    }

    @Override
    public void onFinish() {
        player.sendMessage("Cooldown [" + id + "] has ended.");
    }

    @Override
    public long getTicksElapsed() {
        long now = System.currentTimeMillis();
        long elapsedMillis = Math.max(0, now - startMillis);
        long elapsedTicks = (elapsedMillis / 1000L) * 20L;
        return Math.min(maxTicks, elapsedTicks);
    }

    @Override
    public long getTicksRemaining() {
        long now = System.currentTimeMillis();
        long remainingMillis = Math.max(0, endMillis - now);
        return (remainingMillis / 1000L) * 20L;
    }

    @Override
    public String getTimeRemaining() {
        long totalSeconds = getTicksRemaining() / 20L;
        long days = totalSeconds / 86_400L;
        long hours = (totalSeconds % 86_400L) / 3_600L;
        long minutes = (totalSeconds % 3_600L) / 60L;
        long seconds = totalSeconds % 60L;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");
        return sb.toString().trim();
    }
}
