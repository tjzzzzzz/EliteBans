package fi.tj88888.eliteBans.listeners;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import fi.tj88888.eliteBans.utils.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.PlayerUtil.formatRemainingTime;

public class ChatListener implements Listener {
    private final DatabaseManager databaseManager;

    public ChatListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();

        Punishment punishment = databaseManager.getPunishment(playerUUID);

        if (punishment == null) {
            punishment = databaseManager.getPunishmentByName(playerName);
        }

        if (punishment == null) {
            return;
        }

        String type = punishment.getType().toLowerCase();
        if (type.equals("mute")) {
            handleMute(event, punishment, false);
        } else if (type.equals("tmute")) {
            handleMute(event, punishment, true);
        }
    }

    private void handleMute(AsyncPlayerChatEvent event, Punishment punishment, boolean isTemporary) {
        event.setCancelled(true);

        if (isTemporary) {
            event.getPlayer().sendMessage(MessageUtil.getColoredMessage(
                    "messages.temp-mute-notification",
                    "&dYou have been temporarily muted!\n&dReason: &f%reason%\n&dDuration: &f%duration%",
                    "%reason%", punishment.getReason(),
                    "%duration%", formatRemainingTime(punishment.getExpirationTime())
            ));
        } else {
            event.getPlayer().sendMessage(MessageUtil.getColoredMessage(
                    "messages.mute-notification",
                    "&dYou have been muted!\n&fReason: &d%reason%",
                    "%reason%", punishment.getReason()
            ));
        }
    }
}