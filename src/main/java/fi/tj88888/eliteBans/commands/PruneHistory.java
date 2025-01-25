package fi.tj88888.eliteBans.commands;
import fi.tj88888.eliteBans.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.PlayerUtils.getPlayerUUID;

public class PruneHistory implements CommandExecutor {
    private final DatabaseManager databaseManager;
    public PruneHistory(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("elitebans.command.prunehistory")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /prunehistory <player> <amount>");
            return true;
        }
        String targetName = args[0];
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Amount must be a number!");
            return true;
        }
        UUID targetUUID = getPlayerUUID(targetName);
        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }
        int removedCount = databaseManager.pruneLatestPunishments(targetUUID, amount);
        if (removedCount > 0) {
            sender.sendMessage(ChatColor.GREEN + "Removed " + removedCount + " latest punishments from " + targetName + "'s history.");
        } else {
            sender.sendMessage(ChatColor.RED + "No punishments found to remove from " + targetName + "'s history.");
        }
        return true;
    }
}