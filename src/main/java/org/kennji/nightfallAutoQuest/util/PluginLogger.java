package org.kennji.nightfallAutoQuest.util;

import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

import java.util.logging.Level;

public final class PluginLogger {
    private final NightfallAutoQuest plugin;

    public PluginLogger(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void debug(@NotNull String message) {
        if (plugin.getConfigManager().getConfig().getBoolean("logging.debug", false)) {
            log(Level.INFO, "<gray>[DEBUG] " + message);
        }
    }

    public void info(@NotNull String message) {
        log(Level.INFO, message);
    }

    public void warn(@NotNull String message) {
        log(Level.WARNING, "<yellow>" + message);
    }

    public void error(@NotNull String message) {
        log(Level.SEVERE, "<red>" + message);
    }

    public void error(@NotNull String message, @NotNull Throwable throwable) {
        error(message);
        throwable.printStackTrace();
    }

    private void log(@NotNull Level level, @NotNull String message) {
        String configLevelName = plugin.getConfigManager().getConfig().getString("logging.level", "INFO").toUpperCase();
        Level configLevel = parseLevel(configLevelName);
        
        if (level.intValue() < configLevel.intValue()) return;

        String prefix = "<gradient:#5e4fa2:#f7941d>NightfallAutoQuest</gradient> <gray>»</gray> ";
        plugin.getServer().getConsoleSender().sendMessage(ColorUtil.parse(prefix + message));
    }

    private Level parseLevel(String name) {
        try {
            return Level.parse(name);
        } catch (Exception e) {
            return Level.INFO;
        }
    }
}
