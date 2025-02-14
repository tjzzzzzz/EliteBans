package fi.tj88888.eliteBans.commands;
import fi.tj88888.eliteBans.EliteBans;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import fi.tj88888.eliteBans.utils.MessageUtil;
import fi.tj88888.eliteBans.utils.WebhookUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.PlayerUtil.getPlayerUUID;

public class UnmuteCommand implements CommandExecutor {
    private final DatabaseManager databaseManager;
    private static EliteBans plugin;
    public UnmuteCommand(DatabaseManager databaseManager, EliteBans instance) {
        this.databaseManager = databaseManager;
        this.plugin = instance;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("elitebans.command.unmute")) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.no-permission",
                    "&cYou don't have permission to use this command!"));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.unmute-usage",
                    "Usage: /&dunmute &f<&dplayer&f>"));
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
        UUID unmutedByUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        String unmutedByName = sender instanceof Player ? sender.getName() : "Console";
        List<Punishment> activePunishments = databaseManager.getActivePunishments(targetUUID);
        boolean foundMute = false;
        for (Punishment punishment : activePunishments) {
            if ("MUTE".equalsIgnoreCase(punishment.getType()) || "TMUTE".equalsIgnoreCase(punishment.getType())) {
                String punishmentType = punishment.getType().equalsIgnoreCase("TMUTE") ? "tmute" : "mute";
                databaseManager.archivePunishment(
                        punishment,
                        targetName,
                        unmutedByUUID,
                        unmutedByName,
                        reason,
                        punishmentType
                );
                databaseManager.removePunishment(targetUUID, punishmentType, targetName);
                foundMute = true;
                break;
            }
        }
        if (foundMute) {
            String webhookUrl = plugin.getConfig().getString("webhooks.unmute");
            boolean discordLogging = plugin.getConfig().getBoolean("discord-logging", false);
            if (discordLogging && webhookUrl != null) {
                WebhookUtil.logCommand(
                        "Unmute",
                        unmutedByName,
                        targetName,
                        reason,
                        webhookUrl
                );
            }
            String tbanMessage = (MessageUtil.getColoredMessage("messages.player-unmuted",
                    "&7(Silent) &d%player%&f has been unmuted by &d%unmuter%&f Reason: &d%reason%",
                    "%player%", targetName,
                    "%unmuter%", unmutedByName,
                    "%reason%", reason));
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("elitebans.command.base")) {
                    player.sendMessage(tbanMessage);
                }
            }
        } else {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.player-not-muted",
                    "&d%player%&f is not muted!",
                    "%player%", targetName));
        }
        return true;
    }
}