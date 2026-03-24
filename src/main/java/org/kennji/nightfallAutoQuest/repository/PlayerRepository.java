package org.kennji.nightfallAutoQuest.repository;

import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.repository.base.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PlayerRepository implements Repository<UUID, PlayerData> {
    private final NightfallAutoQuest plugin;

    public PlayerRepository(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull CompletableFuture<PlayerData> load(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                int completions = 0;
                int failures = 0;
                try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM player_stats WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        completions = rs.getInt("completions");
                        failures = rs.getInt("failures");
                    }
                }

                String questId = null;
                String activeTask = null;
                int progress = 0;
                long expiration = 0;
                int startValue = 0;
                int targetAmount = 0;
                try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM player_quests WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        questId = rs.getString("quest_id");
                        activeTask = rs.getString("active_task");
                        progress = rs.getInt("progress");
                        expiration = rs.getLong("expiration");
                        startValue = rs.getInt("start_value");
                        targetAmount = rs.getInt("target_amount");
                    }
                }

                return new PlayerData(uuid, completions, failures, questId, activeTask, progress, expiration, startValue, targetAmount);
            } catch (SQLException e) {
                plugin.getPluginLogger().error("Failed to load player data for " + uuid, e);
                return new PlayerData(uuid, 0, 0, null, null, 0, 0, 0, 0);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> save(@NotNull PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Save stats
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO player_stats (uuid, completions, failures) VALUES (?, ?, ?) " +
                                    "ON CONFLICT(uuid) DO UPDATE SET completions = ?, failures = ?")) {
                        stmt.setString(1, data.uuid().toString());
                        stmt.setInt(2, data.completions());
                        stmt.setInt(3, data.failures());
                        stmt.setInt(4, data.completions());
                        stmt.setInt(5, data.failures());
                        stmt.executeUpdate();
                    }

                    // Save active quest
                    if (data.activeQuestId() != null) {
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "INSERT INTO player_quests (uuid, quest_id, active_task, progress, expiration, start_value, target_amount) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                                        "ON CONFLICT(uuid) DO UPDATE SET quest_id = ?, active_task = ?, progress = ?, expiration = ?, start_value = ?, target_amount = ?")) {
                            stmt.setString(1, data.uuid().toString());
                            stmt.setString(2, data.activeQuestId());
                            stmt.setString(3, data.activeTask());
                            stmt.setInt(4, data.questProgress());
                            stmt.setLong(5, data.questExpiration());
                            stmt.setInt(6, data.placeholderStartValue());
                            stmt.setInt(7, data.targetAmount());
                            
                            stmt.setString(8, data.activeQuestId());
                            stmt.setString(9, data.activeTask());
                            stmt.setInt(10, data.questProgress());
                            stmt.setLong(11, data.questExpiration());
                            stmt.setInt(12, data.placeholderStartValue());
                            stmt.setInt(13, data.targetAmount());
                            stmt.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM player_quests WHERE uuid = ?")) {
                            stmt.setString(1, data.uuid().toString());
                            stmt.executeUpdate();
                        }
                    }

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                plugin.getPluginLogger().error("Failed to save player data for " + data.uuid(), e);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> delete(@NotNull UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM player_stats WHERE uuid = ?")) {
                        stmt.setString(1, uuid.toString());
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM player_quests WHERE uuid = ?")) {
                        stmt.setString(1, uuid.toString());
                        stmt.executeUpdate();
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                plugin.getPluginLogger().error("Failed to delete player data for " + uuid, e);
            }
        });
    }
}
