package fi.tj88888.eliteBans.commands;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import fi.tj88888.eliteBans.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.List;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.DateUtil.formatDate;
import static fi.tj88888.eliteBans.utils.PlayerUtil.formatRemainingTime;
import static fi.tj88888.eliteBans.utils.PlayerUtil.getPlayerUUID;

public class HistCommand implements CommandExecutor {
    private final DatabaseManager databaseManager;

    public HistCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.usage", "&cUsage: /hist <player>"));
            return true;
        }

        String targetName = args[0];
        UUID targetUUID = getPlayerUUID(targetName);
        if (targetUUID == null) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.player-not-found",
                    "&cPlayer %player% does not exist or has no history.",
                    "%player%", targetName));
            return true;
        }

        List<Punishment> activePunishments = databaseManager.getActivePunishments(targetUUID);
        List<Punishment> historicalPunishments = databaseManager.getPunishmentHistory(targetUUID);

        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.header",
                "&fPunishment History for &d%player%&f:",
                "%player%", targetName));
        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.divider",
                "&7-----------------------------------"));

        displayActivePunishments(sender, activePunishments);
        displayHistoricalPunishments(sender, historicalPunishments);

        return true;
    }

    private void displayActivePunishments(CommandSender sender, List<Punishment> activePunishments) {
        if (!activePunishments.isEmpty()) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.active-punishments",
                    "&cActive Punishments:"));
            sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.divider",
                    "&7-----------------------------------"));

            for (Punishment punishment : activePunishments) {
                displayPunishmentDetails(sender, punishment, true);
            }
        }
    }

    private void displayHistoricalPunishments(CommandSender sender, List<Punishment> historicalPunishments) {
        if (!historicalPunishments.isEmpty()) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-history",
                    "&ePunishment History:"));
            sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.divider",
                    "&7-----------------------------------"));

            historicalPunishments.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
            for (Punishment punishment : historicalPunishments) {
                displayPunishmentDetails(sender, punishment, false);
            }
        }
    }

    private void displayPunishmentDetails(CommandSender sender, Punishment punishment, boolean isActive) {
        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-type",
                "&dType: &f%type%",
                "%type%", punishment.getType()));
        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-reason",
                "&dReason: &f%reason%",
                "%reason%", punishment.getReason()));
        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-issued-by",
                "&dIssued By: &f%issuer%",
                "%issuer%", punishment.getIssuedByName()));
        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-issued-at",
                "&dIssued At: &f%time%",
                "%time%", formatDate(punishment.getTimestamp())));

        if (!punishment.getType().equalsIgnoreCase("warn")) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-duration",
                    "&dDuration: &f%duration%",
                    "%duration%", punishment.getDurationText()));

            if (isActive && punishment.getExpirationTime() > 0) {
                sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-expires",
                        "&dExpires In: &f%expires%",
                        "%expires%", formatRemainingTime(punishment.getExpirationTime())));
            }
        }

        displayUnbanDetails(sender, punishment, isActive);
        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.divider",
                "&7-----------------------------------"));
    }

    private void displayUnbanDetails(CommandSender sender, Punishment punishment, boolean isActive) {
        if (punishment.getUnbannedByName() != null) {
            if (punishment.getType().equalsIgnoreCase("ban") ||
                    punishment.getType().equalsIgnoreCase("tban")) {
                displayBanRevocation(sender, punishment);
            } else if (punishment.getType().equalsIgnoreCase("mute") ||
                    punishment.getType().equalsIgnoreCase("tmute")) {
                displayMuteRevocation(sender, punishment);
            }
        } else if (!isActive) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-status",
                    "&dPunishment Status: &f%status%",
                    "%status%", "Expired"));
        }
    }

    private void displayBanRevocation(CommandSender sender, Punishment punishment) {
        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-unbanned-by",
                "&dUnbanned By: &f%unbanner%",
                "%unbanner%", punishment.getUnbannedByName()));
        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-unban-reason",
                "&dUnban Reason: &f%reason%",
                "%reason%", punishment.getUnbanReason()));
        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-unbanned-at",
                "&dUnbanned At: &f%time%",
                "%time%", formatDate(punishment.getUnbanTimestamp())));
    }

    private void displayMuteRevocation(CommandSender sender, Punishment punishment) {
        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-unmuted-by",
                "&dUnmuted By: &f%unmuter%",
                "%unmuter%", punishment.getUnbannedByName()));
        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-unmute-reason",
                "&dUnmute Reason: &f%reason%",
                "%reason%", punishment.getUnbanReason()));
        sender.sendMessage(MessageUtil.getColoredMessage("messages.hist.punishment-unmuted-at",
                "&dUnmuted At: &f%time%",
                "%time%", formatDate(punishment.getUnbanTimestamp())));
    }

}



