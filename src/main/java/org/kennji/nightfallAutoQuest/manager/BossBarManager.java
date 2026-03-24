package org.kennji.nightfallAutoQuest.manager;

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

    public BossBarManager(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void update(@NotNull Player player, @NotNull Quest quest, int progress, long expiration) {
        UUID uuid = player.getUniqueId();
        BossBar bar = activeBars.computeIfAbsent(uuid, k -> {
            BarColor color = BarColor
                    .valueOf(plugin.getConfigManager().getConfig().getString("bossbar.color", "BLUE").toUpperCase());
            BarStyle style = BarStyle
                    .valueOf(plugin.getConfigManager().getConfig().getString("bossbar.style", "SOLID").toUpperCase());
            return Bukkit.createBossBar("", color, style);
        });

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
                .replace("%task%", data.activeTask() != null ? StringUtil.formatEnumName(data.activeTask()) : "")
                .replace("%time%", StringUtil.formatTime(timeLeft));

        bar.setTitle(ColorUtil.colorize(title));

        bar.setProgress(Math.min(1.0, (double) progress / data.targetAmount()));
        bar.addPlayer(player);
        bar.setVisible(true);
    }

    public void remove(@NotNull Player player) {
        BossBar bar = activeBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
            bar.setVisible(false);
        }
    }

    public void removeAll() {
        activeBars.values().forEach(BossBar::removeAll);
        activeBars.clear();
    }
}
