package org.kennji.nightfallAutoQuest.managers;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.modules.Quest;
import org.bukkit.scheduler.BukkitRunnable;
import org.kennji.nightfallAutoQuest.utils.ColorUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BossBarManager {
    private final NightfallAutoQuest plugin;
    private final Map<UUID, BossBar> playerBossBars;
    private final Map<UUID, BukkitRunnable> updateTasks;
    private final Set<UUID> warnedPlayers; // Track players who already received warning

    public BossBarManager(NightfallAutoQuest plugin) {
        this.plugin = plugin;
        this.playerBossBars = new HashMap<>();
        this.updateTasks = new HashMap<>();
        this.warnedPlayers = new HashSet<>();
    }

    public void updateBossBar(Player player, Quest quest, long expiration) {
        if (!plugin.getConfigManager().getConfig().getBoolean("bossbar.enabled", true)) {
            return;
        }

        // Check if player's world is allowed for bossbar display
        java.util.List<String> allowedWorlds = plugin.getConfigManager().getConfig().getStringList("allowed_worlds");
        if (!allowedWorlds.isEmpty() && !org.kennji.nightfallAutoQuest.utils.Util
                .isWorldAllowed(player.getWorld().getName(), allowedWorlds)) {
            removeBossBar(player); // Remove bossbar if world is not allowed
            return;
        }

        UUID uuid = player.getUniqueId();
        cancelUpdateTask(uuid);
        warnedPlayers.remove(uuid); // Reset warning flag for new quest

        BossBar bar = playerBossBars.computeIfAbsent(uuid, k -> {
            String color = plugin.getConfigManager().getConfig().getString("bossbar.color", "BLUE");
            String style = plugin.getConfigManager().getConfig().getString("bossbar.style", "SOLID");
            return Bukkit.createBossBar("", BarColor.valueOf(color), BarStyle.valueOf(style));
        });

        BukkitRunnable updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                long timeLeft = expiration - System.currentTimeMillis();
                if (timeLeft <= 0) {
                    plugin.getQuestManager().failQuest(player);
                    cancel();
                    return;
                }

                String timeFormatted = formatTime(timeLeft);
                String title = plugin.getMessageUtil().getMessage("bossbar_title",
                        "%quest_name%", quest.getName(),
                        "%progress%", String.valueOf(quest.getCurrentProgress()),
                        "%amount%", String.valueOf(quest.getAmount()),
                        "%time%", timeFormatted);
                bar.setTitle(title);
                double progress = Math.min((double) quest.getCurrentProgress() / quest.getAmount(), 1.0);
                bar.setProgress(progress);
                bar.addPlayer(player);

                long warningTime = plugin.getConfigManager().getConfig().getLong("quest.warning-time-seconds", 60)
                        * 1000;
                if (timeLeft <= warningTime && !warnedPlayers.contains(uuid)) {
                    warnedPlayers.add(uuid); // Mark as warned
                    plugin.getMessageUtil().sendMessage(player, "quest-warning",
                            "%time%", formatTime(timeLeft));
                    plugin.getSoundUtil().playSound(player, "warning");
                }
            }
        };
        updateTask.runTaskTimer(plugin, 0L, 20L);
        updateTasks.put(uuid, updateTask);
    }

    public void showCompletionMessage(Player player, String questName) {
        if (!plugin.getConfigManager().getConfig().getBoolean("bossbar.enabled", true)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        cancelUpdateTask(uuid);

        BossBar bar = playerBossBars.computeIfAbsent(uuid, k -> {
            String color = plugin.getConfigManager().getConfig().getString("bossbar.color", "BLUE");
            String style = plugin.getConfigManager().getConfig().getString("bossbar.style", "SOLID");
            return Bukkit.createBossBar("", BarColor.valueOf(color), BarStyle.valueOf(style));
        });

        String message = ColorUtils
                .colorize(plugin.getMessageUtil().getMessage("quest-completed", "%quest_name%", questName));
        bar.setTitle(message);
        bar.setProgress(1.0);
        bar.addPlayer(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bar.removePlayer(player);
            playerBossBars.remove(uuid);
        }, 60L);
    }

    public void showFailureMessage(Player player, String questName) {
        if (!plugin.getConfigManager().getConfig().getBoolean("bossbar.enabled", true)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        cancelUpdateTask(uuid);

        BossBar bar = playerBossBars.computeIfAbsent(uuid, k -> {
            String color = plugin.getConfigManager().getConfig().getString("bossbar.color", "BLUE");
            String style = plugin.getConfigManager().getConfig().getString("bossbar.style", "SOLID");
            return Bukkit.createBossBar("", BarColor.valueOf(color), BarStyle.valueOf(style));
        });

        String message = ColorUtils
                .colorize(plugin.getMessageUtil().getMessage("quest-failed", "%quest_name%", questName));
        bar.setTitle(message);
        bar.setProgress(0.0);
        bar.addPlayer(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bar.removePlayer(player);
            playerBossBars.remove(uuid);
        }, 60L);
    }

    public void showGiveUpMessage(Player player) {
        if (!plugin.getConfigManager().getConfig().getBoolean("bossbar.enabled", true)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        cancelUpdateTask(uuid);

        BossBar bar = playerBossBars.computeIfAbsent(uuid, k -> {
            String color = plugin.getConfigManager().getConfig().getString("bossbar.color", "BLUE");
            String style = plugin.getConfigManager().getConfig().getString("bossbar.style", "SOLID");
            return Bukkit.createBossBar("", BarColor.valueOf(color), BarStyle.valueOf(style));
        });

        String message = ColorUtils.colorize(plugin.getMessageUtil().getMessage("quest-giveup"));
        bar.setTitle(message);
        bar.setProgress(0.0);
        bar.addPlayer(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bar.removePlayer(player);
            playerBossBars.remove(uuid);
        }, 60L);
    }

    public void removeBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        cancelUpdateTask(uuid);
        BossBar bar = playerBossBars.remove(uuid);
        if (bar != null) {
            bar.removePlayer(player);
        }
    }

    private void cancelUpdateTask(UUID uuid) {
        BukkitRunnable task = updateTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    private String formatTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
