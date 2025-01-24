package fi.tj88888.eliteBans.commands;

import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class Hist implements CommandExecutor {

    private final DatabaseManager databaseManager;

    public Hist(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /hist <player>");
            return true;
        }

        String targetName = args[0];
        UUID targetUUID = getPlayerUUID(targetName);
        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " does not exist or has no history.");
            return true;
        }

        List<Punishment> activePunishments = databaseManager.getActivePunishments(targetUUID);

        List<Punishment> historicalPunishments = databaseManager.getPunishmentHistory(targetUUID);

        sender.sendMessage(ChatColor.WHITE + "Punishment History for " + ChatColor.LIGHT_PURPLE + targetName + ChatColor.WHITE + ":");

        // Active Punishments
        if (!activePunishments.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Active Punishments:");
            for (Punishment punishment : activePunishments) {
                String expiresIn = (punishment.getExpirationTime() > 0)
                        ? formatDuration(punishment.getExpirationTime())
                        : "Permanent";

                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type: " + ChatColor.WHITE + punishment.getType());
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Reason: " + ChatColor.WHITE + punishment.getReason());
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Issued By: " + ChatColor.WHITE + punishment.getIssuedByName());

                // Safeguard duration
                String duration = punishment.getDurationText() != null ? punishment.getDurationText() : "N/A";
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Duration: " + ChatColor.WHITE + duration);

                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Expires In: " + ChatColor.WHITE + expiresIn);
                sender.sendMessage(ChatColor.GRAY + "-----------------------------------");
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "No active punishments.");
        }

        // Historical Punishments
        // Historical Punishments
        // Historical Punishments
        if (!historicalPunishments.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Punishment History:");

            // Sort punishments by timestamp (most recent first)
            historicalPunishments.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));

            for (Punishment punishment : historicalPunishments) {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type: " + ChatColor.WHITE + punishment.getType());
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Reason: " + ChatColor.WHITE + punishment.getReason());
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Issued By: " + ChatColor.WHITE + punishment.getIssuedByName());

                // Safeguard duration text
                String durationText = punishment.getDurationText() != null ? punishment.getDurationText() : "N/A";
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Duration: " + ChatColor.WHITE + durationText);

                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Issued At: " + ChatColor.WHITE + formatDate(punishment.getTimestamp()));

                if (punishment.getUnbannedByName() != null) {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "Unbanned By: " + ChatColor.WHITE + punishment.getUnbannedByName());
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "Unban Reason: " + ChatColor.WHITE + punishment.getUnbanReason());
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "Unbanned At: " + ChatColor.WHITE + formatDate(punishment.getUnbanTimestamp()));
                } else {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "Unban Reason:" + ChatColor.WHITE + " Expired");
                }

                sender.sendMessage(ChatColor.GRAY + "-----------------------------------");
            }
        } else {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "No past punishments found.");
        }

        return true;
    }

    private UUID getPlayerUUID(String playerName) {
        if (Bukkit.getPlayerExact(playerName) != null) {
            return Bukkit.getPlayerExact(playerName).getUniqueId();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            return offlinePlayer.getUniqueId();
        }

        return null;
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(timestamp);
    }

    private String formatDuration(long expirationTime) {
        if (expirationTime <= 0) {
            return "Permanent";
        }

        long remainingTime = expirationTime - System.currentTimeMillis();
        if (remainingTime <= 0) {
            return "Expired";
        }

        long days = remainingTime / (1000L * 60 * 60 * 24);
        long hours = (remainingTime / (1000L * 60 * 60)) % 24;
        long minutes = (remainingTime / (1000L * 60)) % 60;

        return (days > 0 ? days + "d " : "") +
                (hours > 0 ? hours + "h " : "") +
                (minutes > 0 ? minutes + "m" : "a few seconds");
    }
}