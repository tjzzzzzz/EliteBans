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
        Punishment punishment = databaseManager.getPunishment(playerUUID);
        if (punishment != null) {
            if (punishment.getType().equals("MUTE")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You are muted!\n" +
                        "Reason: " + ChatColor.WHITE + punishment.getReason());
                return;
            }
            if (punishment.getType().equals("TEMPMUTE")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You are temporarily muted!\n" +
                        "Reason: " + ChatColor.WHITE + punishment.getReason() + "\n" +
                        ChatColor.RED  +  "Expires In: " + ChatColor.WHITE + formatRemainingTime(punishment.getExpirationTime()));
            }
        }
    }
}