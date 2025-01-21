package fi.tj88888.eliteBans.commands;

import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Unban implements CommandExecutor {

    private final DatabaseManager databaseManager;

    public Unban(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("elitebans.command.unban")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                return true;
            }
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unban <player> [reason]");
            return true;
        }

        String targetName = args[0];
        StringBuilder reasonBuilder = new StringBuilder();

        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
        }
        String reason = reasonBuilder.toString().trim();
        if (reason.isEmpty()) {
            reason = "No reason provided";
        }

        UUID targetUUID = getPlayerUUID(targetName);
        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " does not exist!");
            return true;
        }

        String targetDisplayName = getPlayerDisplayName(targetUUID);
        if (targetDisplayName == null) {
            targetDisplayName = targetName;
        }

        UUID unbannedByUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        String unbannedByName = sender instanceof Player ? sender.getName() : "Console";

        Punishment punishment = databaseManager.getPunishment(targetUUID);
        if (punishment != null && ("BAN".equalsIgnoreCase(punishment.getType()) || "TEMPBAN".equalsIgnoreCase(punishment.getType()))) {
            String punishmentType = punishment.getType().equalsIgnoreCase("TEMPBAN") ? "tban" : "ban";


            long unbanTimestamp = System.currentTimeMillis();

            databaseManager.archivePunishment(
                    punishment,         // The punishment object to be archived
                    targetDisplayName,  // The target's display name
                    unbannedByUUID,     // UUID of the unbanner
                    unbannedByName,     // Name of the unbanner
                    reason,             // Reason for unban
                    punishmentType      // Archive as "ban" or "tban"
            );

            databaseManager.removePunishment(targetUUID);

            sender.sendMessage(ChatColor.GREEN + "Player " + targetDisplayName + " has been successfully unbanned.");
            Bukkit.broadcastMessage(ChatColor.YELLOW + targetDisplayName + " has been unbanned by " + unbannedByName + ".");

        } else {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " is not banned!");
        }

        return true;
    }

    private UUID getPlayerUUID(String playerName) {
        Player onlinePlayer = Bukkit.getPlayerExact(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            return offlinePlayer.getUniqueId();
        }

        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
    }

    private String getPlayerDisplayName(UUID playerUUID) {
        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        if (onlinePlayer != null) {
            return onlinePlayer.getDisplayName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        return (offlinePlayer != null && (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()))
                ? offlinePlayer.getName()
                : null;
    }
}