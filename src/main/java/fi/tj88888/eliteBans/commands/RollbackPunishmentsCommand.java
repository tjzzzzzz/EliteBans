package fi.tj88888.eliteBans.commands;

import fi.tj88888.eliteBans.EliteBans;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.utils.MessageUtil;
import fi.tj88888.eliteBans.utils.PlayerUtil;
import fi.tj88888.eliteBans.utils.WebhookUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RollbackPunishmentsCommand implements CommandExecutor {
    private final DatabaseManager databaseManager;
    private static EliteBans plugin;

    public RollbackPunishmentsCommand(DatabaseManager databaseManager, EliteBans instance) {
        this.databaseManager = databaseManager;
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("elitebans.command.rollbackpunishments")) {
            sender.sendMessage(MessageUtil.getColoredMessage("messages.no-permission",
                    "&cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("§cUsage: /rollbackpunishments <time> <type>");
            return true;
        }

        long duration = PlayerUtil.parseDuration(args[0]);
        if (duration <= 0) {
            sender.sendMessage("§cInvalid time format! Use: 30m, 2h, 1d");
            return true;
        }

        String type = args[1].toLowerCase();
        if (!isValidType(type)) {
            sender.sendMessage("§cInvalid type! Use: warn, ban, tban, mute, tmute, or all");
            return true;
        }

        long cutoffTime = System.currentTimeMillis() - duration;
        int removedCount = databaseManager.removePunishmentsByTime(cutoffTime, type);

        String webhookUrl = plugin.getConfig().getString("webhooks.rollbackpunishments");
        boolean discordLogging = plugin.getConfig().getBoolean("discord-logging", false);
        if (discordLogging && webhookUrl != null) {
            WebhookUtil.logCommand(
                    "Rollback Punishments",
                    sender instanceof Player ? sender.getName() : "Console",
                    type,
                    "Removed " + removedCount + " punishments from the last " + args[0],
                    webhookUrl
            );
        }

        sender.sendMessage("§7Removed " + removedCount + " Type: " + type + " punishments from the last " +
                PlayerUtil.formatDuration(duration));
        return true;
    }

    private boolean isValidType(String type) {
        return type.equals("warn") || type.equals("ban") || type.equals("tban") ||
                type.equals("mute") || type.equals("tmute") || type.equals("all");
    }
}