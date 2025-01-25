package fi.tj88888.eliteBans;
import fi.tj88888.eliteBans.commands.*;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.listeners.BanListener;
import fi.tj88888.eliteBans.listeners.ChatListener;
import fi.tj88888.eliteBans.models.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

public final class EliteBans extends JavaPlugin {
    private static DatabaseManager databaseManager;
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        getLogger().info("Loading configuration...");
        String databaseType = this.getConfig().getString("database.type");
        String connectionString = this.getConfig().getString("database.connectionString");
        String databaseName = this.getConfig().getString("database.name");
        String username = this.getConfig().getString("database.username", "");
        String password = this.getConfig().getString("database.password", "");

        getLogger().info("Database Type: " + databaseType);
        getLogger().info("Connection String: " + connectionString);
        getLogger().info("Database Name: " + databaseName);

        databaseManager = new DatabaseManager(
                DatabaseManager.DatabaseType.valueOf(databaseType.toUpperCase()),
                connectionString,
                databaseName,
                username,
                password
        );
        databaseManager.open(connectionString, databaseName, username, password);
        this.getCommand("ban").setExecutor(new Ban(databaseManager));
        this.getCommand("unban").setExecutor(new Unban(databaseManager));
        this.getCommand("hist").setExecutor(new Hist(databaseManager));
        this.getCommand("tban").setExecutor(new TempBan(databaseManager)); // Register tempban
        this.getCommand("mute").setExecutor(new Mute(databaseManager));
        this.getCommand("unmute").setExecutor(new Unmute(databaseManager));
        this.getCommand("tmute").setExecutor(new TempMute(databaseManager));
        this.getCommand("warn").setExecutor(new Warn(databaseManager));
        this.getCommand("prunehistory").setExecutor(new PruneHistory(databaseManager));
        getServer().getPluginManager().registerEvents(new BanListener(databaseManager), this);
        getServer().getPluginManager().registerEvents(new ChatListener(databaseManager), this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            List<Punishment> expiredPunishments = databaseManager.removeExpiredPunishments();
            for (Punishment punishment : expiredPunishments) {
                databaseManager.archiveExpiredPunishment(punishment, punishment.getType().toLowerCase());
                getLogger().info("Archived expired " + punishment.getType() + " for player UUID: " +
                        punishment.getPlayerId() + ", Duration: " + punishment.getDurationText());
            }
        }, 100L, 100L); // 5 sec
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

}
