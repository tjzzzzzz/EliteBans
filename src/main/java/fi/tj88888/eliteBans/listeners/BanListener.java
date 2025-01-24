package fi.tj88888.eliteBans.listeners;

import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public class BanListener implements Listener {

    private final DatabaseManager databaseManager;

    public BanListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();

        Punishment punishment = databaseManager.getPunishment(playerUUID);
        if (punishment == null) {
            punishment = databaseManager.getPunishmentByPlayerName(playerName);

            if (punishment != null) {
                databaseManager.convertNameBanToUUID(punishment.getId(), playerUUID);
            }
        }

        if (punishment != null && "BAN".equalsIgnoreCase(punishment.getType())) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.RED + "You are permanently banned from this server.\n" +
                    "Reason: " + ChatColor.WHITE + punishment.getReason());
            return;
        }

        if (punishment != null && ("TEMPBAN".equalsIgnoreCase(punishment.getType()))) {
            if (punishment.getExpirationTime() > 0 && System.currentTimeMillis() > punishment.getExpirationTime()) {
                databaseManager.archiveExpiredPunishment(punishment);
                databaseManager.removePunishment(playerUUID);
                Bukkit.getLogger().info("Archived and removed expired TEMPBAN for player " + playerName + " (UUID: " + playerUUID + ")");
                return;
            }

            String banMessage = ChatColor.RED + "You are temporarily banned from this server.\n" +
                    "Reason: " + ChatColor.WHITE + punishment.getReason() + "\n" +
                    "Expires In: " + ChatColor.WHITE + formatRemainingTime(punishment.getExpirationTime());
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, banMessage);
        }
    }

    private String formatRemainingTime(long expirationTime) {
        long remainingTime = expirationTime - System.currentTimeMillis();
        long days = remainingTime / (1000L * 60 * 60 * 24);
        long hours = (remainingTime / (1000L * 60 * 60)) % 24;
        long minutes = (remainingTime / (1000L * 60)) % 60;

        return (days > 0 ? days + "d " : "") +
                (hours > 0 ? hours + "h " : "") +
                (minutes > 0 ? minutes + "m" : "a few seconds");
    }
}