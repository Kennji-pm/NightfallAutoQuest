package org.kennji.nightfallAutoQuest.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

import java.io.File;
import java.util.List;

public final class ConfigManager {
    private final NightfallAutoQuest plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private List<String> allowedWorlds;

    public ConfigManager(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
        this.messages = loadYaml("messages.yml");
        loadValues();
    }

    private @NotNull FileConfiguration loadYaml(@NotNull String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        this.messages = loadYaml("messages.yml");
        loadValues();
        plugin.getPluginLogger().info("Configuration reloaded.");
    }

    private void loadValues() {
        this.allowedWorlds = config.getStringList("allowed_worlds");
    }

    public boolean isWorldAllowed(@NotNull String worldName) {
        return allowedWorlds == null || allowedWorlds.isEmpty() || allowedWorlds.contains(worldName);
    }

    public boolean isModuleEnabled(@NotNull String type) {
        return config.getBoolean("modules." + type.toLowerCase(), true);
    }

    public @NotNull FileConfiguration getConfig() {
        return config;
    }

    public @NotNull FileConfiguration getMessages() {
        return messages;
    }

    public void saveConfig() {
        plugin.saveConfig();
    }
}
