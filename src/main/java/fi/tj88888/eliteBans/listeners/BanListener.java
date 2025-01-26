package fi.tj88888.eliteBans.listeners;

import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.PlayerUtils.formatRemainingTime;

public class BanListener implements Listener {
    private final DatabaseManager databaseManager;

    public BanListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    private enum PunishmentType {
        BAN, TBAN
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        Punishment punishment = databaseManager.getPunishment(playerUUID);

        if (punishment == null) {
            return;
        }

        PunishmentType punishmentType;
        try {
            punishmentType = PunishmentType.valueOf(punishment.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }

        switch (punishmentType) {
            case BAN:
                disallowPlayer(event, "You are permanently banned from this server.", punishment.getReason());
                break;
            case TBAN:
                String banMessage = "You are temporarily banned from this server.\n" +
                        "Expires In: " + ChatColor.WHITE + formatRemainingTime(punishment.getExpirationTime());
                disallowPlayer(event, banMessage, punishment.getReason());
                break;
        }
    }

    private void disallowPlayer(PlayerLoginEvent event, String banMessage, String reason) {
        event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                ChatColor.RED + banMessage + ChatColor.RED + "\nReason: " + ChatColor.WHITE + reason);
    }
}
