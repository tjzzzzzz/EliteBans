package fi.tj88888.eliteBans.utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PlayerUtil {
    public static UUID getPlayerUUID(String playerName) {

        Player onlinePlayer = Bukkit.getPlayerExact(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null &&
                    offlinePlayer.getName().equalsIgnoreCase(playerName)) {
                return offlinePlayer.getUniqueId();
            }
        }

        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName.toLowerCase())
                .getBytes(StandardCharsets.UTF_8));
    }

    public static String getPlayerDisplayName(UUID playerUUID) {
        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        if (onlinePlayer != null) {
            return onlinePlayer.getDisplayName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        if (offlinePlayer != null) {
            if (offlinePlayer.hasPlayedBefore()) {
                return offlinePlayer.getName();
            }
            return offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown Player";
        }
        return "Unknown Player";
    }

    public static String formatDuration(long durationMillis) {
        long days = durationMillis / (1000L * 60 * 60 * 24);
        long hours = (durationMillis / (1000L * 60 * 60)) % 24;
        long minutes = (durationMillis / (1000L * 60)) % 60;

        return (days > 0 ? days + "d " : "") +
                (hours > 0 ? hours + "h " : "") +
                (minutes > 0 ? minutes + "m" : "").trim();
    }

    public static void kickPlayer(String playerName, String reason) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            player.kickPlayer(MessageUtil.getColoredMessage("messages.ban-notification", "&dYou have been permanently banned!\\nReason: &f%reason%&d\\nAppeal At:&fdiscord.gg/example",
                    "%reason%", reason));
        }
    }

    public static String buildReason(String[] args, int startIndex) {
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        return reasonBuilder.toString().trim();
    }

    public static long parseDuration(String input) {
        try {
            long duration = 0;
            if (input.endsWith("m")) {
                duration = Long.parseLong(input.replace("m", "")) * 60 * 1000;
            } else if (input.endsWith("h")) {
                duration = Long.parseLong(input.replace("h", "")) * 60 * 60 * 1000;
            } else if (input.endsWith("d")) {
                duration = Long.parseLong(input.replace("d", "")) * 24 * 60 * 60 * 1000;
            }
            return duration > 0 ? duration : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String formatRemainingTime(long expirationTime) {
        long remainingTime = expirationTime - System.currentTimeMillis();
        long days = remainingTime / (1000L * 60 * 60 * 24);
        long hours = (remainingTime / (1000L * 60 * 60)) % 24;
        long minutes = (remainingTime / (1000L * 60)) % 60;
        return (days > 0 ? days + "d " : "") +
                (hours > 0 ? hours + "h " : "") +
                (minutes > 0 ? minutes + "m" : "a few seconds");
    }

}