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

public class Mute implements CommandExecutor {
    private final DatabaseManager databaseManager;
    public Mute(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("elitebans.command.mute")) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.mute-usage", "&fUsage: /&dmute &f<&dplayer&f> <&dreason&f>"));
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
                "mute",
                -1,
                issuerUUID
        );
        punishment.setTimestamp(System.currentTimeMillis());
        databaseManager.addPunishment(punishment, targetName, issuerName, false);
        String muteMessage = (MessageUtil.getColoredMessage("messages.player-muted",
                "&7(Silent) &d%player%&f has been muted by &d%muter%&f for &d%reason%",
                "%player%", targetName,
                "%muter%", issuerName,
                "%reason%", reason));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("elitebans.command.base")) {
                player.sendMessage(muteMessage);
            }
        }

        Player targetPlayer = Bukkit.getPlayer(targetName);

        if (targetPlayer != null) {
            targetPlayer.sendMessage(MessageUtil.getColoredMessage("messages.mute-notification",
                    "&dYou have been muted!\\n&fReason: &d%reason%",
                    "%reason%", reason));
        }
        return true;
    }
}