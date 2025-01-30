package fi.tj88888.eliteBans.listeners;

import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import fi.tj88888.eliteBans.utils.LogUtil;
import fi.tj88888.eliteBans.utils.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.List;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.PlayerUtil.formatRemainingTime;

public class BanListener implements Listener {
    private final DatabaseManager databaseManager;

    public BanListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    private enum PunishmentType {
        ban, tban
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        List<Punishment> activePunishments = databaseManager.getActivePunishments(playerUUID);


        if (activePunishments == null) {
            LogUtil.debug("Punishment on join is null");
            return;
        }


        for (Punishment punishment : activePunishments) {
            if (punishment.getType().equalsIgnoreCase("ban")) {
                String banMessage = MessageUtil.getColoredMessage("messages.ban-message",
                        "&dYou are permanently banned from this server.\n&fReason: &d%reason%\n&fAppeal At: &ddiscord.gg/example",
                        "%reason%", punishment.getReason());
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, banMessage);
                return;
            } else if (punishment.getType().equalsIgnoreCase("tban")) {
                String tbanMessage = MessageUtil.getColoredMessage("messages.temp-ban-message",
                        "&dYou are temporarily banned from this server.\nExpires In: &f%expires%\n&fAppeal At: &ddiscord.gg/example",
                        "%expires%", formatRemainingTime(punishment.getExpirationTime()),
                        "%reason%", punishment.getReason());
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, tbanMessage);
                return;
            }
        }

    }
}
