package fi.tj88888.eliteBans.listeners;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.PlayerUtils.formatRemainingTime;

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
        StringBuilder message = new StringBuilder(ChatColor.RED + "You are ");
        message.append(isTemporary ? "temporarily " : "").append("muted!\n")
                .append("Reason: ").append(ChatColor.WHITE).append(punishment.getReason());

        if (isTemporary) {
            message.append("\n").append(ChatColor.RED)
                    .append("Expires In: ").append(ChatColor.WHITE)
                    .append(formatRemainingTime(punishment.getExpirationTime()));
        }

        event.getPlayer().sendMessage(message.toString());
    }
}