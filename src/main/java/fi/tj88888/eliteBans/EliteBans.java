package fi.tj88888.eliteBans;
import fi.tj88888.eliteBans.commands.*;
import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.listeners.BanListener;
import fi.tj88888.eliteBans.listeners.ChatListener;
import fi.tj88888.eliteBans.listeners.HistoryGUIListener;
import fi.tj88888.eliteBans.models.Punishment;
import fi.tj88888.eliteBans.utils.LogUtil;
import fi.tj88888.eliteBans.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import java.util.logging.Level;

public final class EliteBans extends JavaPlugin {
    private DatabaseManager databaseManager;
    private boolean debug;

    @Override
    public void onEnable() {
        try {
            LogUtil.init(this);
            MessageUtil.init(this);
            this.saveDefaultConfig();
            this.debug = this.getConfig().getBoolean("debug", false);
            LogUtil.debug("Loading configuration...");
            String databaseType = this.getConfig().getString("database.type");
            String connectionString = this.getConfig().getString("database.connectionString");
            String databaseName = this.getConfig().getString("database.name");
            String username = this.getConfig().getString("database.username", "");
            String password = this.getConfig().getString("database.password", "");

            if (databaseType == null || connectionString == null || databaseName == null) {
                LogUtil.debug("Database configuration is incomplete!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            databaseManager = new DatabaseManager(
                    DatabaseManager.DatabaseType.valueOf(databaseType.toUpperCase()),
                    connectionString,
                    databaseName,
                    username,
                    password
            );

            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    databaseManager.open(connectionString, databaseName, username, password);
                } catch (Exception e) {
                    getLogger().log(Level.SEVERE, "Failed to open database connection", e);
                    getServer().getPluginManager().disablePlugin(this);
                }
            });

            registerCommands();
            registerListeners();

            Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::archiveExpiredPunishments, 100L, 100L);

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred during plugin enable", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void registerCommands() {
        this.getCommand("ban").setExecutor(new BanCommand(databaseManager));
        this.getCommand("unban").setExecutor(new UnbanCommand(databaseManager));
        this.getCommand("tban").setExecutor(new TempBanCommand(databaseManager));
        this.getCommand("mute").setExecutor(new MuteCommand(databaseManager));
        this.getCommand("unmute").setExecutor(new UnmuteCommand(databaseManager));
        this.getCommand("tmute").setExecutor(new TempMuteCommand(databaseManager));
        this.getCommand("warn").setExecutor(new WarnCommand(databaseManager));
        this.getCommand("prunehistory").setExecutor(new PruneHistoryCommand(databaseManager, getLogger()));
        this.getCommand("rollbackpunishments").setExecutor(new RollbackPunishmentsCommand(databaseManager));
        String historyMode = getConfig().getString("history.mode", "both");

        if (historyMode.equalsIgnoreCase("gui")) {
            this.getCommand("hist").setExecutor(new HistGUICommand(databaseManager));
        } else if (historyMode.equalsIgnoreCase("text")) {
            this.getCommand("hist").setExecutor(new HistCommand(databaseManager));
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BanListener(databaseManager), this);
        getServer().getPluginManager().registerEvents(new ChatListener(databaseManager), this);
        getServer().getPluginManager().registerEvents(new HistoryGUIListener(databaseManager), this);
    }

    private void archiveExpiredPunishments() {
        List<Punishment> expiredPunishments = databaseManager.removeExpiredPunishments();
        for (Punishment punishment : expiredPunishments) {
            try {
                databaseManager.archiveExpiredPunishment(punishment, punishment.getType().toLowerCase());
                LogUtil.debug("Archived expired " + punishment.getType() + " for player UUID: " +
                        punishment.getPlayerId() + ", Duration: " + punishment.getDurationText());
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to archive expired punishment", e);
            }
        }
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            try {
                databaseManager.close();
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to close database connection", e);
            }
        }
    }

    public boolean isDebugEnabled() {
        return debug;
    }
}