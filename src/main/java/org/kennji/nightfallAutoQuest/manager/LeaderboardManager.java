package org.kennji.nightfallAutoQuest.manager;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.PlayerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class LeaderboardManager {
    private final NightfallAutoQuest plugin;
    private List<PlayerData> topCompletions = new ArrayList<>();
    private List<PlayerData> topRate = new ArrayList<>();
    private long lastUpdate = 0;
    private static final long CACHE_TIME = TimeUnit.MINUTES.toMillis(10); // Cache for 10 minutes

    public LeaderboardManager(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
        updateCache();
    }

    public void updateCache() {
        Bukkit.getAsyncScheduler().runNow(plugin, (task) -> {
            this.topCompletions = plugin.getDatabaseManager().getTopPlayers(1, 10);
            this.topRate = plugin.getDatabaseManager().getTopRatePlayers(1, 10);
            this.lastUpdate = System.currentTimeMillis();
            plugin.getPluginLogger().info("Leaderboard cache updated.");
        });
    }

    public @NotNull List<PlayerData> getTopRate() {
        if (System.currentTimeMillis() - lastUpdate > CACHE_TIME) {
            updateCache();
        }
        return Collections.unmodifiableList(topRate);
    }

    public @NotNull List<PlayerData> getTopCompletions() {
        if (System.currentTimeMillis() - lastUpdate > CACHE_TIME) {
            updateCache();
        }
        return Collections.unmodifiableList(topCompletions);
    }
}
