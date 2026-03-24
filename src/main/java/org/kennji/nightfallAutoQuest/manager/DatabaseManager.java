package org.kennji.nightfallAutoQuest.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.PlayerData;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DatabaseManager {
    private final NightfallAutoQuest plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        HikariConfig config = new HikariConfig();
        ConfigurationSection dbSection = plugin.getConfigManager().getConfig().getConfigurationSection("database");

        if (dbSection == null) {
            throw new IllegalStateException("Database configuration section is missing!");
        }

        String type = dbSection.getString("type", "sqlite").toLowerCase();

        if (type.equals("mysql")) {
            String host = dbSection.getString("mysql.host", "localhost");
            int port = dbSection.getInt("mysql.port", 3306);
            String database = dbSection.getString("mysql.database", "nightfallautoquest");
            String user = dbSection.getString("mysql.username", "root");
            String pass = dbSection.getString("mysql.password", "");

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(user);
            config.setPassword(pass);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        } else {
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
        }

        config.setPoolName("NightfallAutoQuest-Pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(600000);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(1800000);
        config.setKeepaliveTime(0);

        this.dataSource = new HikariDataSource(config);
        setupTables();
        plugin.getPluginLogger().info("Database connection initialized (" + type + ")");
    }

    private void setupTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS player_stats (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "completions INT DEFAULT 0, " +
                    "failures INT DEFAULT 0)");

            stmt.execute("CREATE TABLE IF NOT EXISTS player_quests (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "quest_id VARCHAR(128), " +
                    "active_task VARCHAR(128), " +
                    "progress INT DEFAULT 0, " +
                    "expiration BIGINT, " +
                    "start_value INT DEFAULT 0, " +
                    "target_amount INT DEFAULT 0)");

            plugin.getPluginLogger().info("Database tables verified/created.");
        } catch (SQLException e) {
            plugin.getPluginLogger().error("Failed to setup database tables", e);
        }
    }

    public @NotNull Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized!");
        }
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getPluginLogger().info("Database connection pool closed.");
        }
    }

    public int getTotalPlayers() {
        try (Connection conn = getConnection();
                java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM player_stats")) {
            java.sql.ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            plugin.getPluginLogger().error("Failed to get total players", e);
            return 0;
        }
    }

    public @NotNull List<PlayerData> getTopPlayers(int page, int size) {
        List<PlayerData> top = new ArrayList<>();
        try (Connection conn = getConnection();
                java.sql.PreparedStatement stmt = conn.prepareStatement(
                        "SELECT uuid, completions, failures FROM player_stats ORDER BY completions DESC LIMIT ? OFFSET ?")) {
            stmt.setInt(1, size);
            stmt.setInt(2, (page - 1) * size);
            java.sql.ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                top.add(new PlayerData(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getInt("completions"),
                        rs.getInt("failures"),
                        null, null, 0, 0, 0, 0));
            }
        } catch (SQLException e) {
            plugin.getPluginLogger().error("Failed to get top players", e);
        }
        return top;
    }

    public @NotNull List<PlayerData> getTopRatePlayers(int page, int size) {
        List<PlayerData> players = new ArrayList<>();
        int offset = (page - 1) * size;
        String sql = "SELECT *, (CAST(completions AS REAL) / NULLIF(completions + failures, 0)) as rate " +
                "FROM player_stats ORDER BY rate DESC, completions DESC LIMIT ? OFFSET ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, size);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                players.add(
                        new PlayerData(uuid, rs.getInt("completions"), rs.getInt("failures"), null, null, 0, 0, 0, 0));
            }
        } catch (SQLException e) {
            plugin.getPluginLogger().error("Failed to fetch top rate players", e);
        }
        return players;
    }
}
