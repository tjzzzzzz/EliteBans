package fi.tj88888.eliteBans.commands;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.List;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.DateUtil.formatDate;
import static fi.tj88888.eliteBans.utils.PlayerUtils.getPlayerUUID;

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
        sender.sendMessage(ChatColor.GRAY + "-----------------------------------");
        if (!activePunishments.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Active Punishments:");
            sender.sendMessage(ChatColor.GRAY + "-----------------------------------");
            for (Punishment punishment : activePunishments) {
                String expiresIn = (punishment.getExpirationTime() > 0)
                        ? formatDuration(punishment.getExpirationTime())
                        : "Permanent";
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type: " + ChatColor.WHITE + punishment.getType());
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Reason: " + ChatColor.WHITE + punishment.getReason());
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Issued By: " + ChatColor.WHITE + punishment.getIssuedByName());
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Issued At: " + ChatColor.WHITE + formatDate(punishment.getTimestamp()));
                String duration = punishment.getDurationText() != null ? punishment.getDurationText() : "N/A";
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Duration: " + ChatColor.WHITE + duration);
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Expires In: " + ChatColor.WHITE + expiresIn);
                sender.sendMessage(ChatColor.GRAY + "-----------------------------------");
            }
        }
        if (!historicalPunishments.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Punishment History:");
            sender.sendMessage(ChatColor.GRAY + "-----------------------------------");
            historicalPunishments.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
            for (Punishment punishment : historicalPunishments) {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type: " + ChatColor.WHITE + punishment.getType());
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Reason: " + ChatColor.WHITE + punishment.getReason());
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Issued By: " + ChatColor.WHITE + punishment.getIssuedByName());
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Duration: " + ChatColor.WHITE + punishment.getDurationText());
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Issued At: " + ChatColor.WHITE + formatDate(punishment.getTimestamp()));
                if (punishment.getUnbannedByName() != null) {
                    if (punishment.getType().equalsIgnoreCase("ban") || punishment.getType().equalsIgnoreCase("tban")) {
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Unbanned By: " + ChatColor.WHITE + punishment.getUnbannedByName());
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Unban Reason: " + ChatColor.WHITE + punishment.getUnbanReason());
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Unbanned At: " + ChatColor.WHITE + formatDate(punishment.getUnbanTimestamp()));
                    } else if (punishment.getType().equalsIgnoreCase("mute") || punishment.getType().equalsIgnoreCase("tmute")) {
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Unmuted By: " + ChatColor.WHITE + punishment.getUnbannedByName());
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Unmute Reason: " + ChatColor.WHITE + punishment.getUnbanReason());
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Unmuted At: " + ChatColor.WHITE + formatDate(punishment.getUnbanTimestamp()));
                    }
                } else {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "Punishment Status:" + ChatColor.WHITE + " Expired");
                }
                sender.sendMessage(ChatColor.GRAY + "-----------------------------------");
            }
        }
        return true;
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
