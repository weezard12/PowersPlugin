package me.weezard12.powers.cooldowns;

import org.bukkit.entity.Player;

public abstract class Cooldown implements Runnable {
    protected final Player player;
    protected final String id;
    protected final long maxTicks;
    protected long ticksElapsed = 0;

    protected boolean running = true;

    public Cooldown(Player player, String id, long maxTicks) {
        this.player = player;
        this.id = id;
        this.maxTicks = maxTicks;
    }

    public abstract void onTick();
    public abstract void onFinish();

    @Override
    public void run() {
        if (!running) return;

        if (ticksElapsed >= maxTicks) {
            running = false;
            onFinish();
            CooldownManager.removeCooldown(player, id);
            return;
        }

        onTick();
        ticksElapsed++;
    }

    public long getTicksRemaining() {
        return maxTicks - ticksElapsed;
    }

    public long getTicksElapsed() {
        return ticksElapsed;
    }

    public boolean isRunning() {
        return running;
    }

    public String getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public void cancel() {
        running = false;
        CooldownManager.removeCooldown(player, id);
    }

    /**
     * Gets the remaining time in a human-readable format (e.g. "2h 3m 5s").
     * @return a string representing the remaining cooldown time
     */
    public String getTimeRemaining() {
        long totalSeconds = getTicksRemaining() / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    /**
     * Finishes the cooldown immediately, invoking onFinish and unregistering it.
     * Useful for long-duration cooldowns scheduled with a single delayed task.
     */
    public void finish() {
        if (!running) return;
        running = false;
        onFinish();
        CooldownManager.removeCooldown(player, id);
    }
}
