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
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.PlayerUtil.*;

public class TempBanCommand implements CommandExecutor {
    private final DatabaseManager databaseManager;
    private static EliteBans plugin;

    public TempBanCommand(DatabaseManager databaseManager, EliteBans instance) {
        this.databaseManager = databaseManager;
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("elitebans.command.tban")) {
                player.sendMessage(MessageUtil.getColoredMessage("messages.no-permission",
                        "&cYou don't have permission to use this command!"));
                return true;
            }
        }

        if (args.length < 3) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.tban-usage",
                    "&fUsage: /&dtban &f<&dplayer&f> <&dduration&f> <&dreason&f>"));
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
            sender.sendMessage(MessageUtil.getColoredMessage("messages.invalid-duration",
                    "&cInvalid duration format! Use formats like 30m, 2h, 1d."));
            return true;
        }

        long expirationTimestamp = System.currentTimeMillis() + duration;
        String durationText = formatDuration(duration);
        UUID targetUUID = getPlayerUUID(targetName);

        if (targetUUID == null) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.player-not-found",
                    "&fPlayer &d%player%&f does not exist!",
                    "%player%", targetName));
            return true;
        }

        UUID bannerUUID = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;
        String bannerName = (sender instanceof Player) ? sender.getName() : "Console";
        String targetDisplayName = getPlayerDisplayName(targetUUID);
        if (targetDisplayName == null) targetDisplayName = targetName;

        String bannerDisplayName = (bannerUUID != null) ? getPlayerDisplayName(bannerUUID) : "Console";
        if (bannerDisplayName == null) bannerDisplayName = bannerName;

        boolean nameBasedBan = Bukkit.getPlayerExact(targetName) == null &&
                !Bukkit.getOfflinePlayer(targetName).hasPlayedBefore();

        Punishment punishment = new Punishment(targetUUID, reason, "tban", expirationTimestamp, bannerUUID);
        punishment.setTimestamp(System.currentTimeMillis());
        punishment.setDurationText(durationText);
        databaseManager.addPunishment(punishment, targetName, bannerDisplayName, nameBasedBan);

        String tbanMessage = (MessageUtil.getColoredMessage("messages.player-temp-banned",
                "&7(Silent) &d%player%&f has been banned by &d%banner%&f for &d%duration% &fReason: &d%reason%",
                "%player%", targetDisplayName,
                "%banner%", bannerDisplayName,
                "%duration%", durationText,
                "%reason%", reason));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("elitebans.command.base")) {
                player.sendMessage(tbanMessage);
            }
        }

        String webhookUrl = plugin.getConfig().getString("webhooks.tban");
        boolean discordLogging = plugin.getConfig().getBoolean("discord-logging", false);
        if (discordLogging && webhookUrl != null) {
            WebhookUtil.logCommand(
                    "Temporary Ban",
                    bannerName,
                    targetDisplayName,
                    reason + " (Duration: " + durationText + ")",
                    webhookUrl
            );
        }

        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        if (targetPlayer != null) {
            targetPlayer.kickPlayer(MessageUtil.getColoredMessage("messages.temp-ban-notification",
                    "&dYou are temporarily banned from this server!\\n&dReason: &f%reason%\\n&dExpires In: &f%expires%\\n&dAppeal At: &fdiscord.gg/example",
                    "%reason%", reason,
                    "%duration%", durationText,
                    "%expires%", formatRemainingTime(punishment.getExpirationTime())
            ));
        }

        return true;
    }
}