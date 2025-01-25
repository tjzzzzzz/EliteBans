package fi.tj88888.eliteBans.database;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import fi.tj88888.eliteBans.models.Punishment;
import org.bson.Document;
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
                    punishment.setDurationText(rs.getString("ban_duration"));
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
                punishment.setDurationText(document.getString("ban_duration"));
                return punishment;
            }
        }
        return null;
    }

    public void addPunishment(Punishment punishment, String playerName, String issuedByName, boolean nameBased) {
        long currentTimestamp = System.currentTimeMillis();
        punishment.setTimestamp(currentTimestamp);
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
                statement.setString(10, durationText);
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
                    .append("ban_duration", durationText);
            collection.insertOne(document);
        }
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
        public String getIssuedByName() {
            return issuedByName;
        }
    }

    public enum DatabaseType {
        MYSQL, MONGODB
    }

    public void removePunishment(UUID playerUUID, String punishmentType) {
        Punishment punishment = getPunishment(playerUUID);
        if (punishment != null) {
            if (databaseType == DatabaseType.MYSQL) {
                String query = "DELETE FROM punishments WHERE player_uuid = ? AND type = ?";
                try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                    statement.setString(1, playerUUID.toString());
                    statement.setString(2, punishmentType.equals("tban") ? "TEMPBAN" :
                            punishmentType.equals("ban") ? "BAN" :
                                    punishmentType.toUpperCase());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (databaseType == DatabaseType.MONGODB) {
                String actualType = punishmentType.equals("tban") ? "TEMPBAN" :
                        punishmentType.equals("ban") ? "BAN" :
                                punishmentType.equals("tmute") ? "TEMPMUTE" :
                                        punishmentType.equals("mute") ? "MUTE" :
                                                punishmentType.toUpperCase();

                mongoDatabase.getCollection("punishments").deleteOne(
                        Filters.and(
                                eq("player_uuid", playerUUID.toString()),
                                eq("type", actualType)
                        )
                );
            }
        }
    }

    public void archivePunishment(Punishment punishment, String targetDisplayName, UUID unbannedByUUID,
                                  String unbannedByName, String unbanReason, String archiveType) {
        long now = System.currentTimeMillis();
        String banDuration = punishment.getDurationText() != null ? punishment.getDurationText() : "N/A";

        if (databaseType == DatabaseType.MYSQL) {
            String query;
            if (archiveType.equals("mute") || archiveType.equals("tmute")) {
                query = "INSERT INTO punishment_history (uuid, type, reason, issued_by_uuid, issued_by_name, issued_at, " +
                        "unmuted_at, unmuted_by_uuid, unmuted_by_name, unmute_reason, mute_duration) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            } else {
                query = "INSERT INTO punishment_history (uuid, type, reason, issued_by_uuid, issued_by_name, issued_at, " +
                        "unbanned_at, unbanned_by_uuid, unbanned_by_name, unban_reason, ban_duration) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }

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
                    .append("issued_at", punishment.getTimestamp());

            if (archiveType.equals("mute") || archiveType.equals("tmute")) {
                newHistoryEntry.append("unmuted_at", now)
                        .append("unmuted_by_uuid", unbannedByUUID != null ? unbannedByUUID.toString() : null)
                        .append("unmuted_by_name", unbannedByName)
                        .append("unmute_reason", unbanReason)
                        .append("mute_duration", banDuration);
            } else {
                newHistoryEntry.append("unbanned_at", now)
                        .append("unbanned_by_uuid", unbannedByUUID != null ? unbannedByUUID.toString() : null)
                        .append("unbanned_by_name", unbannedByName)
                        .append("unban_reason", unbanReason)
                        .append("ban_duration", banDuration);
            }

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
                    String durationType = rs.getString("type").toLowerCase().contains("mute") ? "mute_duration" : "ban_duration";
                    String duration = rs.getString(durationType);
                    if (duration == null || duration.trim().isEmpty()) {
                        duration = "N/A";
                    }
                    punishment.setDurationText(duration);
                    if (rs.getString("type").toLowerCase().contains("mute")) {
                        if (rs.getString("unmuted_by_name") != null) {
                            punishment.setUnbanDetails(
                                    rs.getString("unmuted_by_name"),
                                    rs.getString("unmute_reason"),
                                    rs.getLong("unmuted_at")
                            );
                        }
                    } else {
                        if (rs.getString("unbanned_by_name") != null) {
                            punishment.setUnbanDetails(
                                    rs.getString("unbanned_by_name"),
                                    rs.getString("unban_reason"),
                                    rs.getLong("unbanned_at")
                            );
                        }
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

                    Punishment punishment = new Punishment(
                            document.getInteger("id", -1),
                            UUID.fromString(document.getString("uuid")),
                            document.getString("reason"),
                            document.getString("type"),
                            Optional.ofNullable(document.getLong("expiration_time")).orElse(-1L),
                            issuedByUUID
                    );
                    punishment.setTimestamp(document.getLong("issued_at"));
                    String durationType = document.getString("type").toLowerCase().contains("mute") ? "mute_duration" : "ban_duration";
                    String duration = document.getString(durationType);
                    if (duration == null || duration.trim().isEmpty()) {
                        duration = "N/A";
                    }
                    punishment.setDurationText(duration);
                    if (document.getString("type").toLowerCase().contains("mute")) {
                        if (document.getString("unmuted_by_name") != null) {
                            punishment.setUnbanDetails(
                                    document.getString("unmuted_by_name"),
                                    document.getString("unmute_reason"),
                                    Optional.ofNullable(document.getLong("unmuted_at")).orElse(-1L)
                            );
                        }
                    } else {
                        if (document.getString("unbanned_by_name") != null) {
                            punishment.setUnbanDetails(
                                    document.getString("unbanned_by_name"),
                                    document.getString("unban_reason"),
                                    Optional.ofNullable(document.getLong("unbanned_at")).orElse(-1L)
                            );
                        }
                    }
                    punishments.add(punishment);
                }
            }
        }
        return punishments;
    }

    public List<Punishment> removeExpiredPunishments() {
        List<Punishment> expiredPunishments = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        if (databaseType == DatabaseType.MYSQL) {
            String query = "SELECT * FROM punishments WHERE expiration_time > 0 AND expiration_time <= ?";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setLong(1, currentTime);
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
                    punishment.setDurationText(rs.getString("ban_duration"));
                    expiredPunishments.add(punishment);
                }
                statement.executeUpdate("DELETE FROM punishments WHERE expiration_time > 0 AND expiration_time <= " + currentTime);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("punishments");
            try (MongoCursor<Document> cursor = collection.find(Filters.and(
                    Filters.gt("expiration_time", 0),
                    Filters.lte("expiration_time", currentTime)
            )).iterator()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    String issuedByString = doc.getString("issued_by");
                    UUID issuedByUUID = "CONSOLE".equals(issuedByString) ? null : UUID.fromString(issuedByString);
                    Punishment punishment = new Punishment(
                            doc.getInteger("id", -1),
                            UUID.fromString(doc.getString("player_uuid")),
                            doc.getString("reason"),
                            doc.getString("type"),
                            doc.getLong("expiration_time"),
                            issuedByUUID
                    );
                    punishment.setTimestamp(doc.getLong("timestamp"));
                    punishment.setDurationText(doc.getString("ban_duration"));
                    expiredPunishments.add(punishment);
                }
                collection.deleteMany(Filters.and(
                        Filters.gt("expiration_time", 0),
                        Filters.lte("expiration_time", currentTime)
                ));
            }
        }
        return expiredPunishments;
    }

    public void archiveExpiredPunishment(Punishment punishment, String type) {
        String banDuration = punishment.getDurationText() != null ? punishment.getDurationText() : "N/A";
        if (databaseType == DatabaseType.MYSQL) {
            String query = "INSERT INTO punishment_history (uuid, type, reason, issued_by_uuid, issued_by_name, issued_at, expired_at, ban_duration) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setString(1, punishment.getPlayerId().toString());
                statement.setString(2, type);
                statement.setString(3, punishment.getReason());
                statement.setString(4, punishment.getIssuedBy() != null ? punishment.getIssuedBy().toString() : null);
                statement.setString(5, punishment.getIssuedByName());
                statement.setLong(6, punishment.getTimestamp());
                statement.setLong(7, System.currentTimeMillis());
                statement.setString(8, banDuration);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("punishment_history");
            Document historyEntry = new Document()
                    .append("uuid", punishment.getPlayerId().toString())
                    .append("type", type)
                    .append("reason", punishment.getReason())
                    .append("issued_by_uuid", punishment.getIssuedBy() != null ? punishment.getIssuedBy().toString() : null)
                    .append("issued_by_name", punishment.getIssuedByName())
                    .append("issued_at", punishment.getTimestamp())
                    .append("expired_at", System.currentTimeMillis())
                    .append("ban_duration", banDuration);
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

    public int pruneLatestPunishments(UUID playerUUID, int amount) {
        int removedCount = 0;
        if (databaseType == DatabaseType.MYSQL) {
            String query = "DELETE FROM punishment_history WHERE uuid = ? ORDER BY issued_at DESC LIMIT ?";
            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                statement.setString(1, playerUUID.toString());
                statement.setInt(2, amount);
                removedCount = statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (databaseType == DatabaseType.MONGODB) {
            MongoCollection<Document> collection = mongoDatabase.getCollection("punishment_history");
            removedCount = (int) collection.find(eq("uuid", playerUUID.toString()))
                    .sort(Sorts.descending("issued_at"))
                    .limit(amount)
                    .map(doc -> {
                        collection.deleteOne(doc);
                        return 1;
                    })
                    .into(new ArrayList<>())
                    .size();
        }
        return removedCount;
    }

}