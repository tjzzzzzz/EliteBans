package fi.tj88888.eliteBans;

import fi.tj88888.eliteBans.commands.Ban;
import fi.tj88888.eliteBans.commands.Hist;
import fi.tj88888.eliteBans.commands.TempBan;
import fi.tj88888.eliteBans.commands.Unban;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.listeners.BanListener;
import org.bukkit.plugin.java.JavaPlugin;

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

        cleanExpiredBans();

        this.getCommand("ban").setExecutor(new Ban(databaseManager));
        this.getCommand("unban").setExecutor(new Unban(databaseManager));
        this.getCommand("hist").setExecutor(new Hist(databaseManager));
        this.getCommand("tban").setExecutor(new TempBan(databaseManager)); // Register tempban

        getServer().getPluginManager().registerEvents(new BanListener(databaseManager), this);
    }

    private void cleanExpiredBans() {
        int removedCount = databaseManager.removeExpiredPunishments();
        getLogger().info("Removed " + removedCount + " expired punishments from the database.");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }

    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }



}
