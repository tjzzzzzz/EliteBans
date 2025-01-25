package fi.tj88888.eliteBans.commands;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import org.bukkit.ChatColor;
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
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /mute <player> <reason>");
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
                "MUTE",
                -1,
                issuerUUID
        );
        punishment.setTimestamp(System.currentTimeMillis());
        databaseManager.addPunishment(punishment, targetName, issuerName, false);
        sender.sendMessage(ChatColor.GREEN + targetName + " has been muted: " + reason);
        return true;
    }
}