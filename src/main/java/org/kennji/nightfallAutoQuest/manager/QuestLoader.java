package org.kennji.nightfallAutoQuest.manager;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.Quest;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class QuestLoader {
    private final NightfallAutoQuest plugin;

    public QuestLoader(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        File folder = new File(plugin.getDataFolder(), "quests");
        if (!folder.exists()) {
            folder.mkdirs();
            saveQuestsFromJar();
        }

        plugin.getQuestManager().clearQuests();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null)
            return;

        for (File file : files) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            // Support both multiple quests per file or single quest (id at root)
            if (yaml.contains("id")) {
                loadQuest(yaml.getRoot(), yaml.getString("id"));
            } else {
                for (String key : yaml.getKeys(false)) {
                    loadQuest(yaml.getConfigurationSection(key), key);
                }
            }
        }
        plugin.getPluginLogger().info("Loaded <green>" + plugin.getQuestManager().getQuestCount() + "</green> quests.");
    }

    private void loadQuest(ConfigurationSection section, String id) {
        if (section == null)
            return;

        String type = section.getString("type", "mining");
        if (!plugin.getConfigManager().isModuleEnabled(type)) {
            plugin.getPluginLogger().debug("Skipping quest '" + id + "' because module '" + type + "' is disabled.");
            return;
        }

        List<String> tasks;
        // Support both multiple quests per file or single quest (id at root)
        if (section.isList("tasks")) {
            tasks = section.getStringList("tasks");
        } else if (section.isList("task")) {
            tasks = section.getStringList("task");
        } else {
            tasks = Collections.singletonList(section.getString("task", "ANY"));
        }

        // --- NEW SAFETY VALIDATION ---
        if (!validateQuest(id, type, tasks)) return;
        // -----------------------------

        Quest quest = new Quest(
                id,
                type,
                section.getString("display-name", id),
                section.getStringList("description"),
                tasks,
                section.getString("amount", "10"),
                section.getString("time-limit", "60"),
                section.getStringList("rewards"));
        plugin.getQuestManager().registerQuest(quest);
    }

    private boolean validateQuest(String id, String type, List<String> tasks) {
        if (tasks.isEmpty()) {
            plugin.getPluginLogger().error("Quest '" + id + "' has no tasks defined. Skipping.");
            return false;
        }

        for (String task : tasks) {
            if (task.equalsIgnoreCase("ANY")) continue;

            if (type.equalsIgnoreCase("mobkilling")) {
                try {
                    EntityType.valueOf(task.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getPluginLogger().error("Quest '" + id + "' has invalid EntityType: " + task + ". Skipping.");
                    return false;
                }
            } else if (!type.equalsIgnoreCase("placeholder") && !type.equalsIgnoreCase("dealdamage") && !type.equalsIgnoreCase("walking")) {
                // Material-based modules
                if (Material.getMaterial(task.toUpperCase()) == null) {
                    plugin.getPluginLogger().error("Quest '" + id + "' has invalid Material: " + task + ". Skipping.");
                    return false;
                }
            }
        }
        return true;
    }

    private void saveQuestsFromJar() {
        try {
            File jarFile = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            if (jarFile.isFile()) {
                try (ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile))) {
                    ZipEntry entry;
                    while ((entry = zip.getNextEntry()) != null) {
                        String name = entry.getName();
                        if (name.startsWith("quests/") && name.endsWith(".yml") && !name.equals("quests/")) {
                            plugin.saveResource(name, false);
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getPluginLogger().error("Failed to auto-detect quests from JAR: " + e.getMessage());
        }
    }
}
