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

public class TempBan implements CommandExecutor {

    private final DatabaseManager databaseManager;

    public TempBan(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("elitebans.command.tban")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                return true;
            }
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player> <time> <reason>");
            return true;
        }

        String targetName = args[0];
        String timeInput = args[1];
        StringBuilder reasonBuilder = new StringBuilder();

        for (int i = 2; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        long duration = parseDuration(timeInput);
        if (duration <= 0) {
            sender.sendMessage(ChatColor.RED + "Invalid duration format! Use formats like 30m, 2h, 1d.");
            return true;
        }
        long expirationTimestamp = System.currentTimeMillis() + duration;

        UUID targetUUID = getPlayerUUID(targetName);
        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " does not exist!");
            return true;
        }

        UUID bannerUUID = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;
        String bannerName = (sender instanceof Player) ? sender.getName() : "Console";

        String targetDisplayName = getPlayerDisplayName(targetUUID);
        if (targetDisplayName == null) {
            targetDisplayName = targetName;
        }

        String bannerDisplayName = (bannerUUID != null) ? getPlayerDisplayName(bannerUUID) : "Console";
        if (bannerDisplayName == null) {
            bannerDisplayName = bannerName;
        }

        boolean nameBasedBan = Bukkit.getPlayerExact(targetName) == null
                && !Bukkit.getOfflinePlayer(targetName).hasPlayedBefore();

        Punishment punishment = new Punishment(
                targetUUID,  // Banned player's UUID
                reason,      // Ban reason
                "TEMPBAN",   // Temporary ban type
                expirationTimestamp, // Expiration timestamp (temporary ban)
                bannerUUID   // UUID of who banned the player (null if console, handled later)
        );
        long timestamp = System.currentTimeMillis();
        punishment.setTimestamp(timestamp);
        databaseManager.addPunishment(punishment, targetName, bannerDisplayName, nameBasedBan);

        Bukkit.broadcastMessage(ChatColor.RED + targetDisplayName + " has been temporarily banned by " + bannerDisplayName +
                " for " + timeInput + ": " + ChatColor.WHITE + reason);

        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        if (targetPlayer != null) {
            targetPlayer.kickPlayer(ChatColor.RED + "You have been temporarily banned from this server!\n" +
                    "Reason: " + reason + "\nDuration: " + timeInput);
        }

        return true;
    }

    private long parseDuration(String input) {
        try {
            long duration = 0;
            if (input.endsWith("m")) {
                duration = Long.parseLong(input.replace("m", "")) * 60 * 1000;
            } else if (input.endsWith("h")) {
                duration = Long.parseLong(input.replace("h", "")) * 60 * 60 * 1000;
            } else if (input.endsWith("d")) {
                duration = Long.parseLong(input.replace("d", "")) * 24 * 60 * 60 * 1000;
            }
            return duration > 0 ? duration : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private UUID getPlayerUUID(String targetName) {
        Player onlinePlayer = Bukkit.getPlayerExact(targetName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            return offlinePlayer.getUniqueId();
        }

        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + targetName).getBytes(StandardCharsets.UTF_8));
    }

    private String getPlayerDisplayName(UUID playerUUID) {
        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        if (onlinePlayer != null) {
            return onlinePlayer.getDisplayName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        if (offlinePlayer.isOnline() || offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getName();
        }
        return null;
    }
}