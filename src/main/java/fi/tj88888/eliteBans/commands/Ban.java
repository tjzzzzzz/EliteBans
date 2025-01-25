package fi.tj88888.eliteBans.commands;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.PlayerUtils.*;

public class Ban implements CommandExecutor {
    private final DatabaseManager databaseManager;
    public Ban(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("elitebans.command.ban")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                return true;
            }
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ban <player> <reason>");
            return true;
        }
        String targetName = args[0];
        String reason = buildReason(args, 1);
        UUID targetUUID = getPlayerUUID(targetName);
        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " does not exist!");
            return true;
        }
        UUID issuerUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        String issuerName = sender instanceof Player ? ((Player) sender).getName() : "Console";
        String targetDisplayName = getPlayerDisplayName(targetUUID);
        boolean isNameBasedBan = Bukkit.getPlayerExact(targetName) == null && !Bukkit.getOfflinePlayer(targetName).hasPlayedBefore();
        Punishment punishment = new Punishment(targetUUID, reason, "BAN", -1, issuerUUID);
        punishment.setTimestamp(System.currentTimeMillis());
        databaseManager.addPunishment(punishment, targetName, issuerName, isNameBasedBan);
        broadcastBan(targetDisplayName, issuerName, reason);
        kickPlayer(targetName, reason);
        return true;
    }
}