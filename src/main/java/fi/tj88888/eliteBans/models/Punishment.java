package fi.tj88888.eliteBans.models;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class Punishment {
    private Integer id;
    private final UUID playerId;
    private final String reason;
    private final String type;
    private final long expirationTime;
    private final UUID issuedBy;
    private long timestamp;
    private String unbannedByName;
    private String unbanReason;
    private long unbanTimestamp;
    private String durationText;
    /**
     * Full constructor. ID can be null for new punishments.
     */
    public Punishment(Integer id, UUID playerId, String reason, String type, long expirationTime, UUID issuedBy) {
        this.id = id;
        this.playerId = playerId;
        this.reason = reason;
        this.type = type;
        this.expirationTime = expirationTime;
        this.issuedBy = issuedBy;
    }
    /**
     * Constructor for new punishments without a pre-defined ID.
     */
    public Punishment(UUID playerId, String reason, String type, long expirationTime, UUID issuedBy) {
        this(null, playerId, reason, type, expirationTime, issuedBy);
    }
    public void setUnbanDetails(String unbannedByName, String unbanReason, long unbanTimestamp) {
        this.unbannedByName = unbannedByName;
        this.unbanReason = unbanReason;
        this.unbanTimestamp = unbanTimestamp;
    }

    public String getUnbannedByName() {
        return unbannedByName;
    }
    public String getUnbanReason() {
        return unbanReason;
    }
    public long getUnbanTimestamp() {
        return unbanTimestamp;
    }
    public UUID getPlayerId() {
        return playerId;
    }
    public String getReason() {
        return reason;
    }
    public String getType() {
        return type;
    }
    public long getExpirationTime() {
        return expirationTime;
    }
    public UUID getIssuedBy() {
        return issuedBy;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getIssuedByName() {
        if (issuedBy == null) {
            return "Console";
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(issuedBy);
        if (player != null && (player.isOnline() || player.hasPlayedBefore())) {
            return player.getName();
        }

        return "Unknown";
    }
    public String getDurationText() {
        return durationText != null ? durationText : "N/A";
    }
    public void setDurationText(String durationText) {
        this.durationText = (durationText == null || durationText.trim().isEmpty()) ? "N/A" : durationText;
    }

}