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

public class TempMute implements CommandExecutor {
    private final DatabaseManager databaseManager;
    public TempMute(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("elitebans.command.tmute")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player> <time> <reason>");
            return true;
        }
        String targetName = args[0];
        String timeInput = args[1];
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();
        long duration = parseDuration(timeInput);
        if (duration <= 0) {
            sender.sendMessage(ChatColor.RED + "Invalid duration format! Use formats like 30m, 2h, 1d.");
            return true;
        }
        long expirationTimestamp = System.currentTimeMillis() + duration;
        String durationText = formatDuration(duration);
        UUID targetUUID = getPlayerUUID(targetName);
        UUID muterUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        String muterName = sender instanceof Player ? sender.getName() : "Console";
        Punishment punishment = new Punishment(targetUUID, reason, "TEMPMUTE", expirationTimestamp, muterUUID);
        punishment.setTimestamp(System.currentTimeMillis());
        punishment.setDurationText(durationText);
        databaseManager.addPunishment(punishment, targetName, muterName, false);
        sender.sendMessage(ChatColor.GREEN + targetName + " muted for " + durationText + ": " + reason);
        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer != null) {
            targetPlayer.sendMessage(ChatColor.RED + "You have been temporarily muted!\n" + "Reason: " +ChatColor.WHITE + reason +ChatColor.RED +"\nDuration: " + ChatColor.WHITE+durationText);
        }
        return true;
    }
}