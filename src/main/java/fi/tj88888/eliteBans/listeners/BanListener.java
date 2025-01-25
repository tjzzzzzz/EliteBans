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
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        Punishment punishment = databaseManager.getPunishment(playerUUID);
        if (punishment != null) {
            if ("BAN".equalsIgnoreCase(punishment.getType())) {
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                        ChatColor.RED + "You are permanently banned from this server.\n" +
                                "Reason: " + ChatColor.WHITE + punishment.getReason());
                return;
            }
            if ("TEMPBAN".equalsIgnoreCase(punishment.getType())) {
                String banMessage = ChatColor.RED + "You are temporarily banned from this server.\n" +
                        "Reason: " + ChatColor.WHITE + punishment.getReason() + "\n" +
                        "Expires In: " + ChatColor.WHITE + formatRemainingTime(punishment.getExpirationTime());
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, banMessage);
            }
        }
    }
}