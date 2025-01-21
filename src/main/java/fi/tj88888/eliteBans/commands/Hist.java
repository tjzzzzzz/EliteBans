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

        // Fetch active punishments
        List<Punishment> activePunishments = databaseManager.getActivePunishments(targetUUID);

        // Fetch past punishments
        List<Punishment> historicalPunishments = databaseManager.getPunishmentHistory(targetUUID);

        // Display results
        sender.sendMessage(ChatColor.AQUA + "Punishment History for " + ChatColor.YELLOW + targetName + ChatColor.AQUA + ":");

        // Display active punishments
        if (!activePunishments.isEmpty()) {
            sender.sendMessage(ChatColor.GOLD + "Active Punishments:");
            for (Punishment punishment : activePunishments) {
                String expiresIn = (punishment.getExpirationTime() > 0)
                        ? formatDuration(punishment.getExpirationTime())
                        : "Permanent";

                String status = punishment.getExpirationTime() > 0 && punishment.getExpirationTime() <= System.currentTimeMillis()
                        ? ChatColor.RED + "Expired"
                        : ChatColor.GREEN + "Active";

                sender.sendMessage(ChatColor.DARK_AQUA + "Type: " + ChatColor.GOLD + punishment.getType());
                sender.sendMessage(ChatColor.DARK_AQUA + "Reason: " + ChatColor.GOLD + punishment.getReason());
                sender.sendMessage(ChatColor.DARK_AQUA + "Issued By: " + ChatColor.GOLD + punishment.getIssuedByName());
                sender.sendMessage(ChatColor.DARK_AQUA + "Expires In: " + ChatColor.YELLOW + expiresIn);
                sender.sendMessage(ChatColor.DARK_AQUA + "Status: " + status);
                sender.sendMessage(ChatColor.GRAY + "-----------------------------------");
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "No active punishments.");
        }

        // Display past punishments
        if (!historicalPunishments.isEmpty()) {
            sender.sendMessage(ChatColor.GOLD + "Past Punishments:");
            for (Punishment punishment : historicalPunishments) {
                String issuedDate = formatDate(punishment.getTimestamp());
                String unbanDate = punishment.getUnbanTimestamp() > 0 ? formatDate(punishment.getUnbanTimestamp()) : "N/A";

                sender.sendMessage(ChatColor.DARK_AQUA + "Type: " + ChatColor.GOLD + punishment.getType());
                sender.sendMessage(ChatColor.DARK_AQUA + "Reason: " + ChatColor.GOLD + punishment.getReason());
                sender.sendMessage(ChatColor.DARK_AQUA + "Issued By: " + ChatColor.GOLD + punishment.getIssuedByName());
                sender.sendMessage(ChatColor.DARK_AQUA + "Issued Date: " + ChatColor.GOLD + issuedDate);

                if (punishment.getUnbannedByName() != null) {
                    sender.sendMessage(ChatColor.DARK_GREEN + "Unban Date: " + ChatColor.GOLD + unbanDate);
                    sender.sendMessage(ChatColor.DARK_GREEN + "Unbanned By: " + ChatColor.GOLD + punishment.getUnbannedByName());
                    sender.sendMessage(ChatColor.DARK_GREEN + "Unban Reason: " + ChatColor.GOLD + punishment.getUnbanReason());
                } else {
                    sender.sendMessage(ChatColor.RED + "Unban Status: Expired");
                }

                sender.sendMessage(ChatColor.GRAY + "-----------------------------------");
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "No past punishments found.");
        }

        return true;
    }

    // Method to get the player UUID
    private UUID getPlayerUUID(String playerName) {
        if (Bukkit.getPlayerExact(playerName) != null) {
            return Bukkit.getPlayerExact(playerName).getUniqueId();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            return offlinePlayer.getUniqueId();
        }

        // Player has never joined
        return null;
    }

    // Method to format timestamps into readable dates
    private String formatDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(timestamp);
    }

    // Method to format ban durations
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