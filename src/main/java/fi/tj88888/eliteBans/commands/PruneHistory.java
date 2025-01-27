package fi.tj88888.eliteBans.commands;

import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.utils.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.logging.Logger;

import static fi.tj88888.eliteBans.utils.PlayerUtils.getPlayerUUID;

public class PruneHistory implements CommandExecutor {
    private final DatabaseManager databaseManager;
    private static final String NUMBER_ERROR_MSG = ChatColor.RED + "Amount must be a number!";
    private static final String PLAYER_NOT_FOUND_MSG = ChatColor.RED + "Player not found!";
    private final Logger logger;

    public PruneHistory(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
        if (!player.hasPermission("elitebans.command.prunehistory")) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.no-permission", "&cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.prune-history-usage", "&fUsage: /&dprunehistory &f<&dplayer&f> <&dnumber of punishments&f>"));
            return true;
        }

        String targetName = args[0];
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(NUMBER_ERROR_MSG);
            logger.warning("Invalid number format for amount: " + args[1]);
            return true;
        }

        UUID targetUUID = getPlayerUUID(targetName);
        if (targetUUID == null) {
            player.sendMessage(PLAYER_NOT_FOUND_MSG);
            return true;
        }

        int removedCount = databaseManager.pruneLatestPunishments(targetUUID, amount);
        if (removedCount > 0) {
            player.sendMessage(ChatColor.GRAY + "Removed " + removedCount + " latest punishments from " + targetName + "'s history.");
        } else {
            player.sendMessage(ChatColor.RED + "No punishments found to remove from " + targetName + "'s history.");
        }
        return true;
    }
}