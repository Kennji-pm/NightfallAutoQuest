package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.kennji.nightfallAutoQuest.modules.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.List;
import java.util.Random;

public class SmeltingQuestModule implements QuestModule {
    @Override
    public String getType() {
        return "smelting";
    }

    @Override
    public Quest generateQuest(ConfigurationSection config) {
        Random random = new Random();
        ConfigurationSection questSection = config.getConfigurationSection("quests");
        int amount = random.nextInt(questSection.getInt("max-amount", 64) - questSection.getInt("min-amount", 10)) +
                questSection.getInt("min-amount", 10);
        int timeLimit = random.nextInt(questSection.getInt("max-time", 5) - questSection.getInt("min-time", 3)) +
                questSection.getInt("min-time", 3);
        List<String> rewards = config.getConfigurationSection("rewards").getStringList("commands");
        String material = questSection.getStringList("materials").get(random.nextInt(questSection.getStringList("materials").size()));

        return new Quest(config.getString("display.name"), config.getString("display.description"),
                "smelting_" + material, amount, timeLimit, rewards);
    }
}