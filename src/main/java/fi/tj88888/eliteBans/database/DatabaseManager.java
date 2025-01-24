package fi.tj88888.eliteBans.database;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import fi.tj88888.eliteBans.models.Punishment;
import fi.tj88888.eliteBans.utils.DateUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class DatabaseManager {

    private final DatabaseType databaseType;
    private Connection mysqlConnection = null;
    private MongoClient mongoClient = null;
    private MongoDatabase mongoDatabase = null;



    public DatabaseManager(DatabaseType databaseType, String connectionString, String name, String username, String password) {
        this.databaseType = databaseType;
        try {
            if (databaseType == DatabaseType.MYSQL) {

                Class.forName("com.mysql.cj.jdbc.Driver");
                mysqlConnection = DriverManager.getConnection(
                        "jdbc:mysql://" + connectionString + "?useSSL=false",
                        username,
                        password
                );
                setupMySQLTables();
            } else if (databaseType == DatabaseType.MONGODB) {

                mongoClient = MongoClients.create(connectionString);
                mongoDatabase = mongoClient.getDatabase(name);
                setupMongoDBCollections();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Punishment getPunishment(UUID playerUUID) {
        if (databaseType == DatabaseType.MYSQL) {
            String query = "SELECT * FROM punishments WHERE player_uuid = ? AND (type = 'BAN' OR type = 'TEMPBAN')";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setString(1, playerUUID.toString());
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    String issuedByString = rs.getString("issued_by");
                    UUID issuedByUUID = "CONSOLE".equals(issuedByString) ? null : UUID.fromString(issuedByString);

                    Punishment punishment = new Punishment(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("reason"),
                            rs.getString("type"),
                            rs.getLong("expiration_time"),
                            issuedByUUID
                    );

                    punishment.setTimestamp(rs.getLong("timestamp"));
                    punishment.setDurationText(rs.getString("ban_duration")); // FIX: Retrieve and set the duration text
                    return punishment;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("punishments");
            Document document = collection.find(eq("player_uuid", playerUUID.toString())).first();
            if (document != null) {
                String issuedByString = document.getString("issued_by");
                UUID issuedByUUID = "CONSOLE".equals(issuedByString) ? null : UUID.fromString(issuedByString);

                Long expirationTime = document.getLong("expiration_time") != null ? document.getLong("expiration_time") : -1L;

                Punishment punishment = new Punishment(
                        document.getInteger("id", -1),
                        UUID.fromString(document.getString("player_uuid")),
                        document.getString("reason"),
                        document.getString("type"),
                        expirationTime,
                        issuedByUUID
                );

                punishment.setTimestamp(document.getLong("timestamp"));
                punishment.setDurationText(document.getString("ban_duration")); // FIX: Retrieve and set the duration text
                return punishment;
            }
        }
        return null;
    }

    public Punishment getPunishmentByPlayerName(String playerName) {
        if (databaseType == DatabaseType.MYSQL) {
            String query = "SELECT * FROM punishments WHERE player_name = ? AND type = 'BAN' AND name_based = TRUE";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setString(1, playerName);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    String issuedByString = rs.getString("issued_by");
                    UUID issuedByUUID = "CONSOLE".equals(issuedByString) ? null : UUID.fromString(issuedByString);

                    return new Punishment(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("reason"),
                            rs.getString("type"),
                            rs.getLong("expiration_time"),
                            issuedByUUID
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("punishments");
            Document document = collection.find(eq("player_name", playerName)).first();
            if (document != null && document.getBoolean("name_based", false)) {
                String issuedByString = document.getString("issued_by");
                UUID issuedByUUID = "CONSOLE".equals(issuedByString) ? null : UUID.fromString(issuedByString);

                return new Punishment(
                        document.getInteger("id", -1),
                        UUID.fromString(document.getString("player_uuid")),
                        document.getString("reason"),
                        document.getString("type"),
                        document.getLong("expiration_time"),
                        issuedByUUID
                );
            }
        }
        return null;
    }

    public void addPunishment(Punishment punishment, String playerName, String issuedByName, boolean nameBased) {
        long currentTimestamp = System.currentTimeMillis();
        punishment.setTimestamp(currentTimestamp);

        // Safeguard ban_duration (ensure it is never null)
        String durationText = punishment.getDurationText() != null ? punishment.getDurationText() : "N/A";
        punishment.setDurationText(durationText);

        if (databaseType == DatabaseType.MYSQL) {
            String query = "INSERT INTO punishments (player_uuid, player_name, reason, type, expiration_time, issued_by, issued_by_name, name_based, timestamp, ban_duration) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setString(1, punishment.getPlayerId().toString());
                statement.setString(2, playerName);
                statement.setString(3, punishment.getReason());
                statement.setString(4, punishment.getType());
                statement.setLong(5, punishment.getExpirationTime() > 0 ? punishment.getExpirationTime() : -1);
                statement.setString(6, punishment.getIssuedBy() != null ? punishment.getIssuedBy().toString() : "CONSOLE");
                statement.setString(7, issuedByName);
                statement.setBoolean(8, nameBased);
                statement.setLong(9, punishment.getTimestamp());
                statement.setString(10, durationText); // Save the defaulted ban_duration here
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("punishments");
            Document document = new Document("player_uuid", punishment.getPlayerId().toString())
                    .append("player_name", playerName)
                    .append("reason", punishment.getReason())
                    .append("type", punishment.getType())
                    .append("expiration_time", punishment.getExpirationTime() > 0 ? punishment.getExpirationTime() : -1)
                    .append("issued_by", punishment.getIssuedBy() != null ? punishment.getIssuedBy().toString() : "CONSOLE")
                    .append("issued_by_name", issuedByName)
                    .append("name_based", nameBased)
                    .append("timestamp", punishment.getTimestamp())
                    .append("ban_duration", durationText); // Save the defaulted ban_duration here
            collection.insertOne(document);
        }
    }

    public void convertNameBanToUUID(int punishmentId, UUID playerUUID) {
        if (databaseType == DatabaseType.MYSQL) {
            String query = "UPDATE punishments SET player_uuid = ?, name_based = FALSE WHERE id = ?";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setString(1, playerUUID.toString());
                statement.setInt(2, punishmentId);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("punishments");
            collection.updateOne(eq("id", punishmentId), new Document("$set",
                    new Document("player_uuid", playerUUID.toString()).append("name_based", false)));
        }
    }

    private void saveToMySQL(Punishment punishment, String playerName, String issuedByName, boolean nameBased) {
        String query = "INSERT INTO punishments (player_uuid, player_name, reason, type, expiration_time, issued_by, issued_by_name, name_based, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
            statement.setString(1, punishment.getPlayerId().toString());
            statement.setString(2, playerName);
            statement.setString(3, punishment.getReason());
            statement.setString(4, punishment.getType());
            statement.setLong(5, punishment.getExpirationTime());
            statement.setString(6, (punishment.getIssuedBy() != null) ? punishment.getIssuedBy().toString() : "CONSOLE");
            statement.setString(7, issuedByName);
            statement.setBoolean(8, nameBased);
            statement.setLong(9, punishment.getTimestamp());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveToMongoDB(Punishment punishment, String playerName, String issuedByName, boolean nameBased) {
        MongoCollection<Document> collection = mongoDatabase.getCollection("punishments");
        String issuedByUUID = (punishment.getIssuedBy() != null) ? punishment.getIssuedBy().toString() : "CONSOLE";
        Document document = new Document()
                .append("player_uuid", punishment.getPlayerId().toString())
                .append("player_name", playerName)
                .append("reason", punishment.getReason())
                .append("type", punishment.getType())
                .append("expiration_time", punishment.getExpirationTime())
                .append("issued_by", issuedByUUID)
                .append("issued_by_name", issuedByName)
                .append("name_based", nameBased)
                .append("timestamp", punishment.getTimestamp());
        collection.insertOne(document);
    }

    private void setupMySQLTables() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS punishments (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "player_uuid VARCHAR(36) NOT NULL," +
                "player_name VARCHAR(100) NOT NULL," +
                "reason TEXT NOT NULL," +
                "type VARCHAR(10) NOT NULL," +
                "expiration_time BIGINT NOT NULL," +
                "issued_by VARCHAR(36) NOT NULL," +
                "issued_by_name VARCHAR(100) NOT NULL," +
                "name_based BOOLEAN DEFAULT FALSE" +
                ")";
        try (PreparedStatement statement = mysqlConnection.prepareStatement(createTableQuery)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupMongoDBCollections() {
        if (mongoDatabase.getCollection("punishments") == null) {
            mongoDatabase.createCollection("punishments");
        }
    }

    public void open(String connectionString, String name, String username, String password) {
        try {
            if (databaseType == DatabaseType.MYSQL) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                mysqlConnection = DriverManager.getConnection(
                        "jdbc:mysql://" + connectionString + "?useSSL=false",
                        username,
                        password
                );
                setupMySQLTables();
            } else if (databaseType == DatabaseType.MONGODB) {
                mongoClient = MongoClients.create(connectionString);
                mongoDatabase = mongoClient.getDatabase(name);
                setupMongoDBCollections();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (databaseType == DatabaseType.MYSQL && mysqlConnection != null) {
                mysqlConnection.close();
            }
            if (databaseType == DatabaseType.MONGODB && mongoClient != null) {
                mongoClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class PunishmentData extends Punishment {
        private final String playerName;
        private final String issuedByName;

        public PunishmentData(UUID playerId, String reason, String type, long expirationTime, UUID issuedBy, String playerName, String issuedByName) {
            super(playerId, reason, type, expirationTime, issuedBy);
            this.playerName = playerName;
            this.issuedByName = issuedByName;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getIssuedByName() {
            return issuedByName;
        }
    }

    public enum DatabaseType {
        MYSQL, MONGODB
    }

    public void removePunishment(UUID playerUUID) {
        Punishment punishment = getPunishment(playerUUID);
        if (punishment != null) {
            if (databaseType == DatabaseType.MYSQL) {
                String query = "DELETE FROM punishments WHERE player_uuid = ?";
                try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                    statement.setString(1, playerUUID.toString());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (databaseType == DatabaseType.MONGODB) {
                mongoDatabase.getCollection("punishments").deleteOne(eq("player_uuid", playerUUID.toString()));
            }
        }
    }

    public void archivePunishment(Punishment punishment, String targetDisplayName, UUID unbannedByUUID,
                                  String unbannedByName, String unbanReason, String archiveType) {
        long now = System.currentTimeMillis();


        String banDuration = punishment.getDurationText() != null ? punishment.getDurationText() : "N/A";

        if (databaseType == DatabaseType.MYSQL) {
            String query = "INSERT INTO punishment_history (uuid, type, reason, issued_by_uuid, issued_by_name, issued_at, " +
                    "unbanned_at, unbanned_by_uuid, unbanned_by_name, unban_reason, ban_duration) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setString(1, punishment.getPlayerId().toString());
                statement.setString(2, archiveType);
                statement.setString(3, punishment.getReason());
                statement.setString(4, punishment.getIssuedBy() != null ? punishment.getIssuedBy().toString() : null);
                statement.setString(5, punishment.getIssuedByName());
                statement.setLong(6, punishment.getTimestamp());
                statement.setLong(7, now);
                statement.setString(8, unbannedByUUID != null ? unbannedByUUID.toString() : null);
                statement.setString(9, unbannedByName);
                statement.setString(10, unbanReason);
                statement.setString(11, banDuration);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("punishment_history");
            Document newHistoryEntry = new Document()
                    .append("uuid", punishment.getPlayerId().toString())
                    .append("type", archiveType)
                    .append("reason", punishment.getReason())
                    .append("issued_by_uuid", punishment.getIssuedBy() != null ? punishment.getIssuedBy().toString() : null)
                    .append("issued_by_name", punishment.getIssuedByName())
                    .append("issued_at", punishment.getTimestamp())
                    .append("unbanned_at", now)
                    .append("unbanned_by_uuid", unbannedByUUID != null ? unbannedByUUID.toString() : null)
                    .append("unbanned_by_name", unbannedByName)
                    .append("unban_reason", unbanReason)
                    .append("ban_duration", banDuration);
            collection.insertOne(newHistoryEntry);
        }
    }

    public List<Punishment> getPunishmentHistory(UUID playerUUID) {
        List<Punishment> punishments = new ArrayList<>();

        if (databaseType == DatabaseType.MYSQL) {
            String query = "SELECT * FROM punishment_history WHERE uuid = ?";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setString(1, playerUUID.toString());
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    String issuedByString = rs.getString("issued_by_uuid");
                    UUID issuedByUUID = (issuedByString != null) ? UUID.fromString(issuedByString) : null;

                    Punishment punishment = new Punishment(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("reason"),
                            rs.getString("type"),
                            Optional.ofNullable(rs.getObject("expiration_time", Long.class)).orElse(-1L),
                            issuedByUUID
                    );

                    punishment.setTimestamp(rs.getLong("issued_at"));
                    String banDuration = rs.getString("ban_duration");
                    if (banDuration == null || banDuration.trim().isEmpty()) {
                        banDuration = "N/A";
                    }
                    punishment.setDurationText(banDuration);
                    if (rs.getString("unbanned_by_name") != null) {
                        punishment.setUnbanDetails(
                                rs.getString("unbanned_by_name"),
                                rs.getString("unban_reason"),
                                rs.getLong("unbanned_at")
                        );
                    }
                    punishments.add(punishment);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("punishment_history");
            try (MongoCursor<Document> cursor = collection.find(eq("uuid", playerUUID.toString())).iterator()) {
                while (cursor.hasNext()) {
                    Document document = cursor.next();
                    UUID issuedByUUID = (document.getString("issued_by_uuid") != null)
                            ? UUID.fromString(document.getString("issued_by_uuid"))
                            : null;

                    Long expirationTime = document.getLong("expiration_time");
                    if (expirationTime == null) {
                        expirationTime = -1L;
                    }

                    Long issuedAt = document.getLong("issued_at");
                    if (issuedAt == null) {
                        issuedAt = System.currentTimeMillis();
                    }

                    Punishment punishment = new Punishment(
                            document.getInteger("id", -1),
                            UUID.fromString(document.getString("uuid")),
                            document.getString("reason"),
                            document.getString("type"),
                            expirationTime,
                            issuedByUUID
                    );
                    punishment.setTimestamp(issuedAt);
                    String banDuration = document.getString("ban_duration");
                    if (banDuration == null || banDuration.trim().isEmpty()) {
                        banDuration = "N/A";
                    }
                    punishment.setDurationText(banDuration);
                    if (document.getString("unbanned_by_name") != null) {
                        punishment.setUnbanDetails(
                                document.getString("unbanned_by_name"),
                                document.getString("unban_reason"),
                                Optional.ofNullable(document.getLong("unbanned_at")).orElse(-1L)
                        );
                    }
                    punishments.add(punishment);
                }
            }
        }

        return punishments;
    }

    /**
     * Retrieves unban information for a specific punishment ID.
     */
    public String getUnbannedForID(int punishmentID) {
        if (databaseType == DatabaseType.MYSQL) {
            String query = "SELECT * FROM unban_log WHERE punishment_id = ?";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setInt(1, punishmentID);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    String unbannedByName = rs.getString("unbanned_by_name");
                    String reason = rs.getString("reason");
                    long timestamp = rs.getLong("timestamp");
                    return "Unbanned By: " + unbannedByName + ", Reason: " + reason + ", Date: " + new java.util.Date(timestamp).toString();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("unban_log");
            Document doc = collection.find(Filters.eq("punishment_id", punishmentID)).first();
            if (doc != null) {
                String unbannedByName = doc.getString("unbanned_by_name");
                String reason = doc.getString("reason");
                long timestamp = doc.getLong("timestamp");
                return "Unbanned By: " + unbannedByName + ", Reason: " + reason + ", Date: " + new java.util.Date(timestamp).toString();
            }
        }
        return null;
    }

    public int removeExpiredPunishments() {
        int removedCount = 0;

        if (databaseType == DatabaseType.MYSQL) {
            String query = "DELETE FROM punishments WHERE expiration_time > 0 AND expiration_time <= ?";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setLong(1, System.currentTimeMillis());
                removedCount = statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("punishments");
            removedCount = (int) collection.deleteMany(Filters.and(
                    Filters.gt("expiration_time", 0),
                    Filters.lte("expiration_time", System.currentTimeMillis())
            )).getDeletedCount();
        }

        return removedCount;
    }

    public void archiveExpiredPunishment(Punishment punishment) {
        String banDuration = punishment.getDurationText() != null ? punishment.getDurationText() : "N/A"; // Safeguard ban duration

        if (databaseType == DatabaseType.MYSQL) {
            String query = "INSERT INTO punishment_history (uuid, type, reason, issued_by_uuid, issued_by_name, issued_at, expired_at, ban_duration) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setString(1, punishment.getPlayerId().toString());
                statement.setString(2, "tban");
                statement.setString(3, punishment.getReason());
                statement.setString(4, punishment.getIssuedBy() != null ? punishment.getIssuedBy().toString() : null);
                statement.setString(5, punishment.getIssuedByName());
                statement.setLong(6, punishment.getTimestamp());
                statement.setLong(7, System.currentTimeMillis());
                statement.setString(8, banDuration); // Set the ban duration here
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("punishment_history");
            Document historyEntry = new Document()
                    .append("uuid", punishment.getPlayerId().toString())
                    .append("type", "tban")
                    .append("reason", punishment.getReason())
                    .append("issued_by_uuid", punishment.getIssuedBy() != null ? punishment.getIssuedBy().toString() : null)
                    .append("issued_by_name", punishment.getIssuedByName())
                    .append("issued_at", punishment.getTimestamp())
                    .append("expired_at", System.currentTimeMillis())
                    .append("ban_duration", banDuration); // Include the duration when archiving
            collection.insertOne(historyEntry);
        }
    }

    public List<Punishment> getActivePunishments(UUID playerUUID) {
        List<Punishment> activePunishments = new ArrayList<>();

        if (databaseType == DatabaseType.MYSQL) {
            String query = "SELECT * FROM punishments WHERE player_uuid = ?";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setString(1, playerUUID.toString());
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    String issuedByString = rs.getString("issued_by");
                    UUID issuedByUUID = "CONSOLE".equals(issuedByString) ? null : UUID.fromString(issuedByString);

                    Punishment punishment = new Punishment(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("reason"),
                            rs.getString("type"),
                            rs.getLong("expiration_time"),
                            issuedByUUID
                    );
                    punishment.setTimestamp(rs.getLong("timestamp"));

                    String banDuration = rs.getString("ban_duration");
                    punishment.setDurationText((banDuration == null || banDuration.trim().isEmpty()) ? "N/A" : banDuration);

                    activePunishments.add(punishment);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("punishments");
            try (MongoCursor<Document> cursor = collection.find(eq("player_uuid", playerUUID.toString())).iterator()) {
                while (cursor.hasNext()) {
                    Document document = cursor.next();
                    String issuedByString = document.getString("issued_by");
                    UUID issuedByUUID = "CONSOLE".equals(issuedByString) ? null : UUID.fromString(issuedByString);

                    Punishment punishment = new Punishment(
                            document.getInteger("id", -1),
                            UUID.fromString(document.getString("player_uuid")),
                            document.getString("reason"),
                            document.getString("type"),
                            Optional.ofNullable(document.getLong("expiration_time")).orElse(-1L),
                            issuedByUUID
                    );
                    punishment.setTimestamp(document.getLong("timestamp"));

                    String banDuration = document.getString("ban_duration");
                    punishment.setDurationText((banDuration == null || banDuration.trim().isEmpty()) ? "N/A" : banDuration);

                    activePunishments.add(punishment);
                }
            }
        }

        return activePunishments;
    }


}