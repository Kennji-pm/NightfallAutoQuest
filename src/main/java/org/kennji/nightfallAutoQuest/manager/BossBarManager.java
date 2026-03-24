package org.kennji.nightfallAutoQuest.manager;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.util.ColorUtil;
import org.kennji.nightfallAutoQuest.util.StringUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BossBarManager {
    private final NightfallAutoQuest plugin;
    private final Map<UUID, BossBar> activeBars = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> hideTasks = new ConcurrentHashMap<>();

    public BossBarManager(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void update(@NotNull Player player, @NotNull Quest quest, int progress, long expiration) {
        UUID uuid = player.getUniqueId();
        cancelHideTask(uuid);

        BossBar bar = getOrCreateBar(uuid);

        long timeLeft = expiration - System.currentTimeMillis();
        if (timeLeft <= 0) {
            remove(player);
            return;
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(uuid);
        String titleTemplate = plugin.getConfigManager().getMessages().getString("bossbar.title",
                "<gray>Quest: <white>%name% <gray>[<white>%progress%/%amount%<gray>] <gray>Time: <white>%time%");
        String title = titleTemplate
                .replace("%name%", quest.displayName())
                .replace("%progress%", String.valueOf(progress))
                .replace("%amount%", String.valueOf(data.targetAmount()))
                .replace("%task%", data.activeTask() != null ? plugin.getMessageUtil().translateTask(data.activeTask()) : "")
                .replace("%time%", StringUtil.formatTime(timeLeft));

        bar.setTitle(ColorUtil.colorize(title));

        bar.setProgress(Math.min(1.0, (double) progress / data.targetAmount()));
        bar.addPlayer(player);
        bar.setVisible(true);
    }

    public void showStatus(@NotNull Player player, @NotNull String message, @NotNull BarColor color, int seconds) {
        UUID uuid = player.getUniqueId();
        cancelHideTask(uuid);

        BossBar bar = getOrCreateBar(uuid);
        bar.setTitle(ColorUtil.colorize(message));
        bar.setColor(color);
        bar.setProgress(1.0);
        bar.addPlayer(player);
        bar.setVisible(true);

        ScheduledTask task = player.getScheduler().runDelayed(plugin, (t) -> {
            remove(player);
            hideTasks.remove(uuid);
        }, null, (long) seconds * 20);
        hideTasks.put(uuid, task);
    }

    private BossBar getOrCreateBar(UUID uuid) {
        return activeBars.computeIfAbsent(uuid, k -> {
            BarColor color = BarColor
                    .valueOf(plugin.getConfigManager().getConfig().getString("bossbar.color", "BLUE").toUpperCase());
            BarStyle style = BarStyle
                    .valueOf(plugin.getConfigManager().getConfig().getString("bossbar.style", "SOLID").toUpperCase());
            return Bukkit.createBossBar("", color, style);
        });
    }

    private void cancelHideTask(UUID uuid) {
        ScheduledTask task = hideTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    public void remove(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        cancelHideTask(uuid);
        BossBar bar = activeBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
            bar.setVisible(false);
        }
    }

    public void removeAll() {
        hideTasks.values().forEach(ScheduledTask::cancel);
        hideTasks.clear();
        activeBars.values().forEach(BossBar::removeAll);
        activeBars.clear();
    }
}
