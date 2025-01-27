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
import static fi.tj88888.eliteBans.utils.PlayerUtils.*;

public class BanCommand implements CommandExecutor {
    private final DatabaseManager databaseManager;
    public BanCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("elitebans.command.ban")) {
                player.sendMessage(MessageUtil.getColoredMessage("messages.no-permission", "&cYou don't have permission to use this command!"));
                return true;
            }
        }
        if (args.length < 2) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.ban-usage", "&fUsage: /&dban &f<&dplayer&f> <&dreason&f>"));
            return true;
        }
        String targetName = args[0];
        String reason = buildReason(args, 1);
        UUID targetUUID = getPlayerUUID(targetName);
        if (targetUUID == null) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.player-not-found", "&fPlayer &d%player%&f does not exist!")
                    .replace("%player%", targetName));
            return true;
        }
        UUID issuerUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        String issuerName = sender instanceof Player ? ((Player) sender).getName() : "Console";
        boolean isNameBasedBan = Bukkit.getPlayerExact(targetName) == null && !Bukkit.getOfflinePlayer(targetName).hasPlayedBefore();
        Punishment punishment = new Punishment(targetUUID, reason, "ban", -1, issuerUUID);
        punishment.setTimestamp(System.currentTimeMillis());
        databaseManager.addPunishment(punishment, targetName, issuerName, isNameBasedBan);
        kickPlayer(targetName, reason);
        String banMessage = (MessageUtil.getColoredMessage("messages.player-banned",
                "&d%player%&f has been banned by &d%banner%&f: &d%reason%",
                "%player%", targetName,
                "%banner%", issuerName,
                "%reason%", reason));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("elitebans.command.base")) {
                player.sendMessage(banMessage);
            }
        }
        return true;
    }
}