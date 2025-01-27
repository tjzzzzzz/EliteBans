package fi.tj88888.eliteBans.commands;

import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import fi.tj88888.eliteBans.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.PlayerUtils.getPlayerUUID;

public class WarnCommand implements CommandExecutor {
    private final DatabaseManager databaseManager;

    public WarnCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("elitebans.command.warn")) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.no-permission",
                    "&cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.warn-usage",
                    "Usage: /&dwarn &f<&dplayer&f> <&dreason&f>"));
            return true;
        }

        String targetName = args[0];
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        UUID targetUUID = getPlayerUUID(targetName);
        UUID issuerUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        String issuerName = sender instanceof Player ? sender.getName() : "Console";

        Punishment punishment = new Punishment(
                targetUUID,
                reason,
                "warn",
                -1,
                issuerUUID
        );
        punishment.setTimestamp(System.currentTimeMillis());

        databaseManager.addPunishment(punishment, targetName, issuerName, false);

        // Notify the target player if they're online
        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer != null) {
            targetPlayer.sendMessage(MessageUtil.getColoredMessage("messages.warn-notification",
                    "&dYou have been warned!\\n&fReason: &d%reason%",
                    "%reason%", reason));
        }
        databaseManager.archivePunishment(
                punishment,
                targetName,
                issuerUUID,
                issuerName,
                reason,
                "warn"
        );
        String punishmentType = "warn";
        databaseManager.removePunishment(targetUUID, punishmentType, targetName);
        String tmuteMessage = (MessageUtil.getColoredMessage("messages.player-warned",
                "&7(Silent) &d%player%&f has been warned by &d%warner%&f Reason: &d%reason%",
                "%player%", targetName,
                "%warner%", issuerName,
                "%reason%", reason));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("elitebans.command.base")) {
                player.sendMessage(tmuteMessage);
            }
        }
        return true;
    }
}