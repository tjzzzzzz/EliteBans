package fi.tj88888.eliteBans.utils;

import fi.tj88888.eliteBans.EliteBans;

public class LogUtil {
    private static EliteBans plugin;

    public static void init(EliteBans instance) {
        plugin = instance;
    }

    public static void debug(String message) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[Debug] " + message);
        }
    }
}