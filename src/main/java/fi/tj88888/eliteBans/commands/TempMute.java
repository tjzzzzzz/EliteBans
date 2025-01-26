package fi.tj88888.eliteBans.commands;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import fi.tj88888.eliteBans.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.PlayerUtils.*;

public class TempMute implements CommandExecutor {
    private final DatabaseManager databaseManager;
    public TempMute(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("elitebans.command.tmute")) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.no-permission",
                    "&cYou don't have permission to use this command!"));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.tmute-usage",
                    "&fUsage: /&dtmute &f<&dplayer&f> <&dduration&f> <&dreason&f>"));
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
        UUID muterUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        String muterName = sender instanceof Player ? sender.getName() : "Console";
        Punishment punishment = new Punishment(targetUUID, reason, "tmute", expirationTimestamp, muterUUID);
        punishment.setTimestamp(System.currentTimeMillis());
        punishment.setDurationText(durationText);
        databaseManager.addPunishment(punishment, targetName, muterName, false);
        String tmuteMessage = (MessageUtil.getColoredMessage("messages.player-temp-muted",
                "&7(Silent) &d%player%&f has been muted by &d%muter%&f for &d%duration%&f Reason: &d%reason%",
                "%player%", targetName,
                "%muter%", muterName,
                "%duration%", durationText,
                "%reason%", reason));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("elitebans.command.base")) {
                player.sendMessage(tmuteMessage);
            }
        }
        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer != null) {
            targetPlayer.sendMessage(MessageUtil.getColoredMessage("messages.temp-mute-notification",
                    "&dYou have been temporarily muted!\\n&dReason: &f%reason%\\n&dDuration: &f%duration%",
                    "%reason%", reason,
                    "%duration%", durationText));
        }
        return true;
    }
}