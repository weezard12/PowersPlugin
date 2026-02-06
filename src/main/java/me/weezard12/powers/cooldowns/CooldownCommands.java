package me.weezard12.powers.cooldowns;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Commands class for debugging and managing cooldowns.
 * Provides information about active cooldowns for players.
 */
public class CooldownCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player targetPlayer;

        // Determine the target player. If no player is specified in the arguments,
        // it defaults to the command sender if they are a player.
        if (args.length == 0) {
            if (sender instanceof Player) {
                targetPlayer = (Player) sender;
            } else {
                sender.sendMessage(ChatColor.RED + "From the console, you must specify a player.");
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player>");
                return true;
            }
        } else {
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' is not online.");
                return true;
            }
        }

        // Retrieve the set of active cooldowns for the target player.
        Set<Cooldown> cooldowns = CooldownManager.getCooldowns(targetPlayer);

        if (cooldowns.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + targetPlayer.getName() + " has no active cooldowns.");
            return true;
        }

        // Display the cooldown information in a formatted way.
        sender.sendMessage(ChatColor.GOLD + "--- Active Cooldowns for " + targetPlayer.getName() + " ---");
        for (Cooldown cooldown : cooldowns) {
            sender.sendMessage(ChatColor.AQUA + "ID: " + ChatColor.WHITE + cooldown.getId());
            sender.sendMessage(ChatColor.GRAY + "  - Type: " + ChatColor.WHITE + cooldown.getClass().getSimpleName());
            sender.sendMessage(ChatColor.GRAY + "  - Time Remaining: " + ChatColor.WHITE + cooldown.getTimeRemaining());
            sender.sendMessage(ChatColor.GRAY + "  - Ticks Remaining: " + ChatColor.WHITE + cooldown.getTicksRemaining());
            sender.sendMessage(ChatColor.GRAY + "  - Ticks Elapsed: " + ChatColor.WHITE + cooldown.getTicksElapsed());
        }
        sender.sendMessage(ChatColor.GOLD + "--------------------------------------");

        return true;
    }
}