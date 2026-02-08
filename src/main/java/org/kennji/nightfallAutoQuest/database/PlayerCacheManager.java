package org.kennji.nightfallAutoQuest.database;

import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.utils.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PlayerCacheManager {

    private final NightfallAutoQuest plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, PlayerData> playerCache;
    private final ScheduledExecutorService scheduler;
    private final long saveIntervalSeconds;

    private final Logger logger;

    public PlayerCacheManager(NightfallAutoQuest plugin, DatabaseManager databaseManager, long saveIntervalSeconds) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playerCache = new ConcurrentHashMap<>();
        this.saveIntervalSeconds = saveIntervalSeconds;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.logger = plugin.getPluginLogger(); // Get the plugin's logger instance
        scheduleCacheFlush();
        logger.log(Level.INFO, "PlayerCacheManager initialized with save interval: " + saveIntervalSeconds + " seconds.");
    }

    public PlayerData getPlayerData(UUID playerUUID) {
        return playerCache.computeIfAbsent(playerUUID, uuid -> {
            PlayerData data = databaseManager.loadPlayerData(uuid);
            if (data == null) {
                data = new PlayerData(uuid); // Create new PlayerData if not found
            }
            return data;
        });
    }

    public void updatePlayerData(UUID playerUUID, PlayerData data) {
        playerCache.put(playerUUID, data);
    }

    public void removePlayerData(UUID playerUUID) {
        playerCache.remove(playerUUID);
    }

    private void scheduleCacheFlush() {
        scheduler.scheduleAtFixedRate(this::flushCacheToDatabase, saveIntervalSeconds, saveIntervalSeconds, TimeUnit.SECONDS);
        logger.log(Level.INFO, "Scheduled cache flush every " + saveIntervalSeconds + " seconds.");
    }

    public void flushCacheToDatabase() {
        if (playerCache.isEmpty()) {
            return;
        }

        logger.log(Level.INFO, "Flushing " + playerCache.size() + " player data entries from cache to database...");
        playerCache.forEach((uuid, data) -> {
            databaseManager.savePlayerData(data);
        });
        logger.log(Level.INFO, "Cache flush completed.");
    }

    public void shutdown() {
        logger.log(Level.INFO, "Shutting down PlayerCacheManager. Flushing remaining cache to database...");
        flushCacheToDatabase();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                logger.log(Level.WARNING, "Cache flush scheduler did not terminate in time. Forced shutdown.");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Cache flush scheduler shutdown interrupted: " + e.getMessage());
        }
    }
}
