package org.kennji.nightfallAutoQuest.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Handles automatic quest assignment and expiry warnings
 */
public final class QuestScheduler {
    private final NightfallAutoQuest plugin;
    private final List<UUID> warnedPlayers = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean running = false;

    public QuestScheduler(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (running)
            return;
        running = true;

        long assignInterval = plugin.getConfigManager().getConfig().getLong("quest.assign-interval", 500);

        // Scheduler for automatic assignment
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (task) -> {
            if (!running) {
                task.cancel();
                return;
            }
            assignQuests();
        }, assignInterval, assignInterval, TimeUnit.SECONDS);

        // Scheduler for check (every second)
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (task) -> {
            if (!running) {
                task.cancel();
                return;
            }
            check();
        }, 1, 1, TimeUnit.SECONDS);

        plugin.getPluginLogger().info("Quest Scheduler started (Assign: " + assignInterval + "s).");
    }

    public void stop() {
        running = false;
    }

    public void reload() {
        stop();
        start();
    }

    private void assignQuests() {
        double percentage = plugin.getConfigManager().getConfig().getDouble("quest.assign-percentage", 50.0);
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty())
            return;

        int targetCount = (int) Math.ceil(onlinePlayers.size() * (percentage / 100.0));

        List<Player> withQuests = onlinePlayers.stream()
                .filter(p -> plugin.getPlayerManager().getPlayerData(p.getUniqueId()).hasActiveQuest())
                .collect(Collectors.toList());

        int currentCount = withQuests.size();
        int needs = targetCount - currentCount;

        if (needs <= 0)
            return;

        List<Player> eligible = onlinePlayers.stream()
                .filter(p -> !plugin.getPlayerManager().getPlayerData(p.getUniqueId()).hasActiveQuest())
                .collect(Collectors.toList());

        Collections.shuffle(eligible);

        int toAssignCount = Math.min(needs, eligible.size());
        for (int i = 0; i < toAssignCount; i++) {
            Player p = eligible.get(i);
            // Run assignment on main thread since it might trigger events/sounds/messages
            Bukkit.getGlobalRegionScheduler().run(plugin, (t) -> plugin.getQuestService().assignRandomQuest(p));
        }

        if (toAssignCount > 0) {
            plugin.getPluginLogger().info("Auto-assigned quests to " + toAssignCount + " players.");
        }
    }

    private void check() {
        long warningSeconds = plugin.getConfigManager().getConfig().getLong("quest.warning-time-seconds", 60);
        long now = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            if (!data.hasActiveQuest()) {
                warnedPlayers.remove(player.getUniqueId());
                continue;
            }

            long timeLeft = (data.questExpiration() - now) / 1000;

            // Real-time BossBar update
            plugin.getQuestManager().getQuest(data.activeQuestId()).ifPresent(quest -> {
                // Using update method directly which handles title and progress
                // Run on main thread to be safe with Folia/Paper and ensure consistency
                player.getScheduler().run(plugin, (t) -> {
                    plugin.getBossBarManager().update(player, quest, data.questProgress(), data.questExpiration());
                }, null);
            });

            // Check if within warning window and not already warned for this quest
            if (timeLeft <= warningSeconds && timeLeft > 0 && !warnedPlayers.contains(player.getUniqueId())) {
                warnedPlayers.add(player.getUniqueId());

                Bukkit.getGlobalRegionScheduler().run(plugin, (t) -> {
                    plugin.getMessageUtil().sendMessage(player, "quest-warning", Map.of(
                            "%time%", String.valueOf(timeLeft) + "s"));
                    plugin.getSoundUtil().playSound(player, "warning");
                });
            }
        }
    }
}
