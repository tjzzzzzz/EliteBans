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
import static fi.tj88888.eliteBans.utils.PlayerUtil.getPlayerDisplayName;
import static fi.tj88888.eliteBans.utils.PlayerUtil.getPlayerUUID;

public class UnbanCommand implements CommandExecutor {
    private final DatabaseManager databaseManager;
    private static EliteBans plugin;
    public UnbanCommand(DatabaseManager databaseManager, EliteBans instance) {
        this.databaseManager = databaseManager;
        this.plugin = instance;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("elitebans.command.unban")) {
                player.sendMessage(MessageUtil.getColoredMessage("messages.no-permission",
                        "&cYou don't have permission to use this command!"));
                return true;
            }
        }
        if (args.length < 1) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.unban-usage",
                    "Usage: /&dunban &f<&dplayer&f>"));
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
        if (targetUUID == null) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.player-not-found",
                    "&fPlayer &d%player%&f does not exist!",
                    "%player%", targetName));
            return true;
        }
        String targetDisplayName = getPlayerDisplayName(targetUUID);
        if (targetDisplayName == null) {
            targetDisplayName = targetName;
        }
        UUID unbannedByUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        String unbannedByName = sender instanceof Player ? sender.getName() : "Console";
        List<Punishment> activePunishments = databaseManager.getActivePunishments(targetUUID);
        boolean foundBan = false;
        for (Punishment punishment : activePunishments) {
            if ("BAN".equalsIgnoreCase(punishment.getType()) || "TBAN".equalsIgnoreCase(punishment.getType())) {
                String punishmentType = punishment.getType().equalsIgnoreCase("TBAN") ? "tban" : "ban";
                databaseManager.archivePunishment(punishment, targetDisplayName, unbannedByUUID, unbannedByName, reason, punishmentType);
                databaseManager.removePunishment(targetUUID, punishmentType, targetName);
                foundBan = true;
                break;
            }
        }
        if (foundBan) {
            String webhookUrl = plugin.getConfig().getString("webhooks.unban");
            boolean discordLogging = plugin.getConfig().getBoolean("discord-logging", false);
            if (discordLogging && webhookUrl != null) {
                WebhookUtil.logCommand(
                        "Unban",
                        unbannedByName,
                        targetDisplayName,
                        reason,
                        webhookUrl
                );
            }

            String tbanMessage = (MessageUtil.getColoredMessage("messages.player-unbanned",
                    "&7(Silent) &d%player%&f has been unbanned by &d%unbanner%&f Reason: &d%reason%",
                    "%player%", targetDisplayName,
                    "%unbanner%", unbannedByName,
                    "%reason%", reason));
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("elitebans.command.base")) {
                    player.sendMessage(tbanMessage);
                }
            }
        } else {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.player-not-banned",
                    "&d%player%&f is not banned!",
                    "%player%", targetName));
        }
        return true;
    }
}