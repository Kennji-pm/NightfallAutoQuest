package org.kennji.nightfallAutoQuest.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {
    private final NightfallAutoQuest plugin;
    private HikariDataSource dataSource;
    private final String databaseType;

    public DatabaseManager(NightfallAutoQuest plugin, String dbType) {
        this.plugin = plugin;
        this.databaseType = dbType.toLowerCase();
        plugin.getPluginLogger().log(Level.INFO, "Initializing DatabaseManager with provider [" + databaseType.toUpperCase() + "]");
        setupDataSource();
    }

    public void initialize() {
        plugin.getPluginLogger().log(Level.INFO, "Starting database initialization...");
        createTables();
        plugin.getPluginLogger().log(Level.INFO, "Database initialization completed successfully.");
    }

    private void setupDataSource() {
        HikariConfig config = new HikariConfig();
        FileConfiguration cfg = plugin.getConfigManager().getConfig();

        try {
            if (databaseType.equals("mysql")) {
                String host = cfg.getString("database.mysql.host", "localhost");
                String port = cfg.getString("database.mysql.port", "3306");
                String database = cfg.getString("database.mysql.database", "nightfallautoquest");
                config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC");
                config.setUsername(cfg.getString("database.mysql.username", "root"));
                config.setPassword(cfg.getString("database.mysql.password", ""));
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            } else {
                // Default to SQLite if unknown type
                try {
                    Class.forName("org.sqlite.JDBC");
                    plugin.getPluginLogger().log(Level.INFO, "SQLite driver loaded successfully.");
                } catch (ClassNotFoundException e) {
                    plugin.getPluginLogger().log(Level.SEVERE, "Failed to load SQLite driver: " + e.getMessage());
                    throw new RuntimeException("SQLite driver not found", e);
                }
                String dbPath = plugin.getDataFolder().getAbsolutePath() + "/nightfallautoquest.db";
                config.setJdbcUrl("jdbc:sqlite:" + dbPath);
                config.setDriverClassName("org.sqlite.JDBC");
            }

            // HikariCP configuration (updated for 6.0.0)
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.addDataSourceProperty("cachePrepStmts", "true"); // Retained for MySQL compatibility
            config.setPoolName("NightfallAutoQuest-HikariPool");

            dataSource = new HikariDataSource(config);
            testConnection();
        } catch (Exception e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to setup database provider " + databaseType + ": " + e.getMessage());
            throw new RuntimeException("Database setup failed", e);
        }
    }

    private void testConnection() {
        try (Connection conn = dataSource.getConnection()) {
            plugin.getPluginLogger().log(Level.INFO, "Successfully established connection to " + databaseType + " database.");
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to connect to " + databaseType + " database: " + e.getMessage());
            throw new RuntimeException("Database connection test failed", e);
        }
    }

    private void createTables() {
        try (Connection conn = dataSource.getConnection()) {
            plugin.getPluginLogger().log(Level.INFO, "Creating database tables if they do not exist...");

            // Create quest_completions table
            try (PreparedStatement stmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS quest_completions (" +
                            "player_uuid VARCHAR(36) NOT NULL," +
                            "quest_type VARCHAR(50) NOT NULL," +
                            "completions INT DEFAULT 0," +
                            "PRIMARY KEY (player_uuid, quest_type))")) {
                stmt.executeUpdate();
                plugin.getPluginLogger().log(Level.INFO, "Table 'quest_completions' created or already exists.");
            }

            // Create quest_failures table
            try (PreparedStatement stmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS quest_failures (" +
                            "player_uuid VARCHAR(36) NOT NULL," +
                            "quest_type VARCHAR(50) NOT NULL," +
                            "failures INT DEFAULT 0," +
                            "PRIMARY KEY (player_uuid, quest_type))")) {
                stmt.executeUpdate();
                plugin.getPluginLogger().log(Level.INFO, "Table 'quest_failures' created or already exists.");
            }

            // Create player_quests table
            try (PreparedStatement stmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_quests (" +
                            "player_uuid VARCHAR(36) PRIMARY KEY," +
                            "quest_name VARCHAR(100) NOT NULL," +
                            "progress INT DEFAULT 0," +
                            "expiration BIGINT NOT NULL," +
                            "placeholder_start_value INT DEFAULT 0)")) {
                stmt.executeUpdate();
                plugin.getPluginLogger().log(Level.INFO, "Table 'player_quests' created or already exists.");
            }
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to create tables: " + e.getMessage());
            throw new RuntimeException("Table creation failed", e);
        }
    }

    public void savePlayerData(PlayerData playerData) {
        if (playerData == null || playerData.uuid == null) {
            plugin.getPluginLogger().log(Level.WARNING, "Invalid PlayerData for savePlayerData: " + playerData);
            return;
        }

        UUID playerUUID = UUID.fromString(playerData.uuid);

        try (Connection conn = dataSource.getConnection()) {
            // Save quest completions
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO quest_completions (player_uuid, quest_type, completions) VALUES (?, ?, ?)")) {
                stmt.setString(1, playerData.uuid);
                // Assuming questType is not directly in PlayerData, this needs to be handled differently
                // For now, I'll just update the total completions. This needs refinement if individual quest completions are tracked.
                // For now, I'll assume PlayerData.completions is the total completions.
                // If individual quest completions are needed, PlayerData needs to be updated to store a map of questType to completions.
                // For this task, I will assume PlayerData.completions is the total completions.
                stmt.setString(2, "TOTAL"); // Placeholder quest type for total completions
                stmt.setInt(3, playerData.completions);
                stmt.executeUpdate();
            }

            // Save quest failures
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO quest_failures (player_uuid, quest_type, failures) VALUES (?, ?, ?)")) {
                stmt.setString(1, playerData.uuid);
                stmt.setString(2, "TOTAL"); // Placeholder quest type for total failures
                stmt.setInt(3, playerData.failures);
                stmt.executeUpdate();
            }

            // Save active quest
            if (playerData.activeQuest != null) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO player_quests (player_uuid, quest_name, progress, expiration, placeholder_start_value) VALUES (?, ?, ?, ?, ?)")) {
                    stmt.setString(1, playerData.uuid);
                    stmt.setString(2, playerData.activeQuest);
                    stmt.setInt(3, playerData.questProgress);
                    stmt.setLong(4, playerData.questExpiration);
                    stmt.setInt(5, playerData.placeholderStartValue);
                    stmt.executeUpdate();
                }
            } else {
                // If active quest is null, remove it from the database
                this.removeActiveQuest(playerUUID);
            }
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to save PlayerData for UUID: " + playerData.uuid + ": " + e.getMessage());
        }
    }

    public PlayerData loadPlayerData(UUID playerUUID) {
        if (playerUUID == null) {
            plugin.getPluginLogger().log(Level.WARNING, "Invalid UUID for loadPlayerData: null");
            return null;
        }

        PlayerData playerData = new PlayerData(playerUUID);

        try (Connection conn = dataSource.getConnection()) {
            // Load completions
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT completions FROM quest_completions WHERE player_uuid = ? AND quest_type = 'TOTAL'")) {
                stmt.setString(1, playerUUID.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    playerData.completions = rs.getInt("completions");
                }
            }

            // Load failures
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT failures FROM quest_failures WHERE player_uuid = ? AND quest_type = 'TOTAL'")) {
                stmt.setString(1, playerUUID.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    playerData.failures = rs.getInt("failures");
                }
            }

            // Load active quest
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT quest_name, progress, expiration, placeholder_start_value FROM player_quests WHERE player_uuid = ?")) {
                stmt.setString(1, playerUUID.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    playerData.activeQuest = rs.getString("quest_name");
                    playerData.questProgress = rs.getInt("progress");
                    playerData.questExpiration = rs.getLong("expiration");
                    playerData.placeholderStartValue = rs.getInt("placeholder_start_value");
                }
            }
            playerData.completionRate = Util.calculateCompletionRate(playerData.completions, playerData.failures);
            plugin.getPluginLogger().log(Level.INFO, "Loaded PlayerData for UUID: " + playerUUID);
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to load PlayerData for UUID: " + playerUUID + ": " + e.getMessage());
            return null;
        }
        return playerData;
    }

    public Integer getCompletions(UUID playerUUID) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COALESCE(SUM(completions), 0) as total FROM quest_completions WHERE player_uuid = ?")) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to fetch completions for UUID: " + playerUUID + ": " + e.getMessage());
        }
        plugin.getPluginLogger().log(Level.INFO, "No completions found for UUID: " + playerUUID);
        return 0;
    }

    public Integer getFailures(UUID playerUUID) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COALESCE(SUM(failures), 0) as total FROM quest_failures WHERE player_uuid = ?")) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to fetch failures for UUID: " + playerUUID + ": " + e.getMessage());
        }
        plugin.getPluginLogger().log(Level.INFO, "No failures found for UUID: " + playerUUID);
        return 0;
    }

    public List<String> getTopPlayers(int page, int limit) {
        List<String> topPlayers = new ArrayList<>();
        int offset = (page - 1) * limit;
        String query = "SELECT " +
                "    c.player_uuid, " +
                "    COALESCE(c.total_completions, 0) as total_completions, " +
                "    COALESCE(f.total_failures, 0) as total_failures " +
                "FROM ( " +
                "    SELECT player_uuid, SUM(completions) as total_completions " +
                "    FROM quest_completions " +
                "    GROUP BY player_uuid " +
                "    HAVING SUM(completions) > 0 " +
                ") c " +
                "LEFT JOIN ( " +
                "    SELECT player_uuid, SUM(failures) as total_failures " +
                "    FROM quest_failures " +
                "    GROUP BY player_uuid " +
                ") f ON c.player_uuid = f.player_uuid " +
                "ORDER BY total_completions DESC, total_failures ASC, c.player_uuid ASC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String uuid = rs.getString("player_uuid");
                int completions = rs.getInt("total_completions");
                int failures = rs.getInt("total_failures");
                if (completions < 0 || failures < 0) {
                    plugin.getPluginLogger().log(Level.WARNING, "Invalid data for UUID: " + uuid + ", completions: " + completions + ", failures: " + failures);
                    continue;
                }
                String playerName = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                if (playerName != null) {
                    String data = playerName + ":" + completions + ":" + failures;
                    topPlayers.add(data);
                } else {
                    plugin.getPluginLogger().log(Level.WARNING, "Player name not found for UUID: " + uuid);
                }
            }
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to fetch top players for page " + page + ": " + e.getMessage());
        }
        return topPlayers;
    }

    public ResultSet getActiveQuests() {
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT player_uuid, quest_name, progress, expiration, placeholder_start_value FROM player_quests");
            return stmt.executeQuery();
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to fetch active quests: " + e.getMessage());
            return null;
        }
    }

    public int getTotalPlayersWithCompletions() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(DISTINCT player_uuid) as total FROM quest_completions WHERE completions > 0")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to fetch total players with completions: " + e.getMessage());
        }
        return 0;
    }

    public int getPlayerRank(UUID playerUUID) {
        if (playerUUID == null) {
            plugin.getPluginLogger().log(Level.WARNING, "Invalid UUID for getPlayerRank: null");
            return -1;
        }
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT player_uuid, COALESCE(SUM(completions), 0) as total_completions " +
                             "FROM quest_completions GROUP BY player_uuid " +
                             "HAVING total_completions > 0 " +
                             "ORDER BY total_completions DESC, player_uuid ASC")) {
            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                String uuid = rs.getString("player_uuid");
                if (uuid.equals(playerUUID.toString())) {
                    return rank;
                }
                rank++;
            }
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to fetch rank for UUID: " + playerUUID + ": " + e.getMessage());
        }
        plugin.getPluginLogger().log(Level.INFO, "No rank found for UUID: " + playerUUID);
        return -1;
    }

    public PlayerData getPlayerData(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            plugin.getPluginLogger().log(Level.WARNING, "Invalid player name for getPlayerData: " + playerName);
            return null;
        }

        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();

        // This method should now load from cache first, then database if not in cache.
        // For now, I'll keep it as is, but it will be replaced by PlayerCacheManager.getPlayerData
        return loadPlayerData(playerUUID);
    }

    public void removeActiveQuest(UUID playerUUID) {
        if (playerUUID == null) {
            plugin.getPluginLogger().log(Level.WARNING, "Invalid UUID for removeActiveQuest: null");
            return;
        }
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM player_quests WHERE player_uuid = ?")) {
            stmt.setString(1, playerUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to remove active quest for UUID: " + playerUUID + ": " + e.getMessage());
        }
    }

    public void purgeDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM quest_completions")) {
                stmt.executeUpdate();
                plugin.getPluginLogger().log(Level.INFO, "Cleared quest_completions table.");
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM quest_failures")) {
                stmt.executeUpdate();
                plugin.getPluginLogger().log(Level.INFO, "Cleared quest_failures table.");
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM player_quests")) {
                stmt.executeUpdate();
                plugin.getPluginLogger().log(Level.INFO, "Cleared player_quests table.");
            }
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to purge database: " + e.getMessage());
        }
    }

    public void close() {
        if (dataSource == null || dataSource.isClosed()) {
            plugin.getPluginLogger().log(Level.INFO, databaseType + " database connection pool is already closed or not initialized.");
            return;
        }
        try {
            dataSource.close();
            plugin.getPluginLogger().log(Level.INFO, "Successfully closed " + databaseType + " database connection pool.");
        } catch (Exception e) {
            plugin.getPluginLogger().log(Level.WARNING, "Failed to close database: " + e.getMessage());
        } finally {
            dataSource = null;
        }
    }
}
