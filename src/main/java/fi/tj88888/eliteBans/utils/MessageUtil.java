package fi.tj88888.eliteBans.utils;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

public class MessageUtil {
    private static Plugin plugin;

    public static void init(Plugin instance) {
        plugin = instance;
    }

    public static String getColoredMessage(String path, String defaultMessage, String... replacements) {
        String message = plugin.getConfig().getString(path, defaultMessage);
        message = ChatColor.translateAlternateColorCodes('&', message);

        if(replacements != null && replacements.length % 2 == 0) {
            for(int i = 0; i < replacements.length; i += 2) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }
}