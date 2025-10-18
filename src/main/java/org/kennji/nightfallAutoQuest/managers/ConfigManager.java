package org.kennji.nightfallAutoQuest.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigManager {
    private final NightfallAutoQuest plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration commands;
    private static final String CONFIG_VERSION = "1.6";

    public ConfigManager(NightfallAutoQuest plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
            plugin.getPluginLogger().info("Created default config.yml");
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        String configVersion = config.getString("config-version", "0.0");
        if (!CONFIG_VERSION.equals(configVersion)) {
            plugin.getPluginLogger().warning("Config version mismatch! Expected " + CONFIG_VERSION + ", found " + configVersion);
            backupAndReplaceConfig(configFile);
            config = YamlConfiguration.loadConfiguration(configFile);
        } else {
            plugin.getPluginLogger().info("Config version " + configVersion + " matches expected version.");
        }
        plugin.getPluginLogger().info("Loaded config.yml");

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        if (messages.getKeys(false).isEmpty()) {
            plugin.getPluginLogger().severe("Messages.yml is empty or failed to load properly");
        } else {
            plugin.getPluginLogger().info("Loaded messages.yml");
        }

        File commandsFile = new File(plugin.getDataFolder(), "commands.yml");
        if (!commandsFile.exists()) {
            plugin.saveResource("commands.yml", false);
        }
        commands = YamlConfiguration.loadConfiguration(commandsFile);
        if (commands.getKeys(false).isEmpty()) {
            plugin.getPluginLogger().warning("Commands.yml is empty or failed to load properly");
        } else {
            plugin.getPluginLogger().info("Loaded commands.yml");
        }

        File questsFolder = new File(plugin.getDataFolder(), "quests");
        if (!questsFolder.exists()) {
            questsFolder.mkdirs();
            plugin.getPluginLogger().info("Created quests directory: quests");
        }
    }

    private void backupAndReplaceConfig(File configFile) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String backupName = "config_backup_" + sdf.format(new Date()) + ".yml";
            File backupFile = new File(plugin.getDataFolder(), backupName);
            if (configFile.renameTo(backupFile)) {
                plugin.getPluginLogger().info("Backed up old config to " + backupName);
            } else {
                plugin.getPluginLogger().warning("Failed to back up old config.yml");
            }
            plugin.saveResource("config.yml", false);
            plugin.getPluginLogger().info("Restored default config.yml");
        } catch (Exception e) {
            plugin.getPluginLogger().severe("Failed to backup or replace config.yml: " + e.getMessage());
        }
    }

    public void saveConfig() {
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
            plugin.getPluginLogger().info("Saved config.yml");
        } catch (IOException e) {
            plugin.getPluginLogger().severe("Failed to save config.yml: " + e.getMessage());
        }
    }

    public void reload() {
        loadConfigs();
        plugin.getPluginLogger().info("Configuration files reloaded");
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getCommands() {
        return commands;
    }
}
