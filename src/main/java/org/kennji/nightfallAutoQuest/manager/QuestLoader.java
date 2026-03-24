package org.kennji.nightfallAutoQuest.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.Quest;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class QuestLoader {
    private final NightfallAutoQuest plugin;

    public QuestLoader(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        File folder = new File(plugin.getDataFolder(), "quests");
        if (!folder.exists()) {
            folder.mkdirs();
            // Save default quests from jar
            plugin.saveResource("quests/ke_diet_nhen.yml", false);
            plugin.saveResource("quests/kien_truc_su_tre.yml", false);
            plugin.saveResource("quests/ngu_phu_cham_chi.yml", false);
            plugin.saveResource("quests/nha_tham_hiem.yml", false);
            plugin.saveResource("quests/nong_dan_thanh_thi.yml", false);
            plugin.saveResource("quests/tho_dao_tap_su.yml", false);
        }

        plugin.getQuestManager().clearQuests();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

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
        if (section == null) return;

        String type = section.getString("type", "mining");
        if (!plugin.getConfigManager().isModuleEnabled(type)) {
            plugin.getPluginLogger().debug("Skipping quest '" + id + "' because module '" + type + "' is disabled.");
            return;
        }

        List<String> tasks;
        if (section.isList("tasks")) {
            tasks = section.getStringList("tasks");
        } else if (section.isList("task")) {
            tasks = section.getStringList("task");
        } else {
            tasks = Collections.singletonList(section.getString("task", "ANY"));
        }

        Quest quest = new Quest(
                id,
                type,
                section.getString("display-name", id),
                section.getStringList("description"),
                tasks,
                section.getString("amount", "10"),
                section.getString("time-limit", "60"),
                section.getStringList("rewards")
        );
        plugin.getQuestManager().registerQuest(quest);
    }
}
