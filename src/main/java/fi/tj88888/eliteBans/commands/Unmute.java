package fi.tj88888.eliteBans.commands;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;
import static fi.tj88888.eliteBans.utils.PlayerUtils.getPlayerUUID;

public class Unmute implements CommandExecutor {
    private final DatabaseManager databaseManager;
    public Unmute(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("elitebans.command.unmute")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unmute <player> [reason]");
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
        UUID unmutedByUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        String unmutedByName = sender instanceof Player ? sender.getName() : "Console";
        List<Punishment> activePunishments = databaseManager.getActivePunishments(targetUUID);
        boolean foundMute = false;
        for (Punishment punishment : activePunishments) {
            if ("MUTE".equalsIgnoreCase(punishment.getType()) || "TMUTE".equalsIgnoreCase(punishment.getType())) {
                String punishmentType = punishment.getType().equalsIgnoreCase("TMUTE") ? "tmute" : "mute";
                databaseManager.archivePunishment(
                        punishment,
                        targetName,
                        unmutedByUUID,
                        unmutedByName,
                        reason,
                        punishmentType
                );
                databaseManager.removePunishment(targetUUID, punishmentType, targetName);
                foundMute = true;
                break;
            }
        }
        if (foundMute) {
            sender.sendMessage(ChatColor.GREEN + "Player " + targetName + " has been unmuted.");
        } else {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " is not muted!");
        }
        return true;
    }
}