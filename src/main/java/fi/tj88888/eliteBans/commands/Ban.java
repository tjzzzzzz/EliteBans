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

public class Ban implements CommandExecutor {

    private final DatabaseManager databaseManager;

    public Ban(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permissions
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("elitebans.command.ban")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                return true;
            }
        }

        // Check arguments
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ban <player> <reason>");
            return true;
        }

        String targetName = args[0];
        String reason = buildReason(args);

        // Fetch player UUID
        UUID targetUUID = getPlayerUUID(targetName);
        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " does not exist!");
            return true;
        }

        // Fetch who issued the ban
        UUID issuerUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        String issuerName = sender instanceof Player ? sender.getName() : "Console";

        String targetDisplayName = getPlayerDisplayName(targetUUID, targetName);

        // Determine if the ban is name-based
        boolean nameBasedBan = Bukkit.getPlayerExact(targetName) == null
                && !Bukkit.getOfflinePlayer(targetName).hasPlayedBefore();

        // Create & save punishment
        Punishment punishment = new Punishment(
                targetUUID,
                reason,
                "BAN",
                -1, // -1 = perm ban
                issuerUUID
        );
        punishment.setTimestamp(System.currentTimeMillis());

        databaseManager.addPunishment(punishment, targetName, issuerName, nameBasedBan);

        // Notify players and handle kicking
        broadcastBan(targetDisplayName, issuerName, reason);
        kickPlayer(targetName, reason);

        return true;
    }

    private String buildReason(String[] args) {
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        return reasonBuilder.toString().trim();
    }

    private UUID getPlayerUUID(String playerName) {
        Player onlinePlayer = Bukkit.getPlayerExact(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getUniqueId();
        }

        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
    }

    private String getPlayerDisplayName(UUID playerUUID, String fallback) {
        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        if (onlinePlayer != null) {
            return onlinePlayer.getDisplayName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        if (offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getName();
        }

        return fallback;
    }

    private void broadcastBan(String targetName, String issuerName, String reason) {
        String message = ChatColor.RED + targetName + " has been banned by " + issuerName
                + " for: " + ChatColor.WHITE + reason;
        Bukkit.broadcastMessage(message);
    }

    private void kickPlayer(String playerName, String reason) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            player.kickPlayer(ChatColor.RED + "You have been banned from this server!\nReason: " + reason);
        }
    }
}