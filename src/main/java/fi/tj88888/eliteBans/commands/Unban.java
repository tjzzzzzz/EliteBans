package fi.tj88888.eliteBans.commands;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.PlayerUtils.getPlayerDisplayName;
import static fi.tj88888.eliteBans.utils.PlayerUtils.getPlayerUUID;

public class Unban implements CommandExecutor {
    private final DatabaseManager databaseManager;
    public Unban(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("elitebans.command.unban")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                return true;
            }
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unban <player> [reason]");
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
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " does not exist!");
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
            sender.sendMessage(ChatColor.GREEN + "Player " + targetDisplayName + " has been successfully unbanned.");
            Bukkit.broadcastMessage(ChatColor.YELLOW + targetDisplayName + " has been unbanned by " + unbannedByName + ".");
        } else {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " is not banned!");
        }
        return true;
    }
}