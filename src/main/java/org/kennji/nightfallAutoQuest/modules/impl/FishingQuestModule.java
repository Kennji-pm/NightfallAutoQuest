package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.kennji.nightfallAutoQuest.modules.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.List;
import java.util.Random;

public class FishingQuestModule implements QuestModule {
    @Override
    public String getType() {
        return "fishing";
    }

    @Override
    public Quest generateQuest(ConfigurationSection config) {
        Random random = new Random();
        ConfigurationSection questSection = config.getConfigurationSection("quests");
        int amount = random.nextInt(questSection.getInt("max-amount", 20) - questSection.getInt("min-amount", 5)) +
                questSection.getInt("min-amount", 5);
        int timeLimit = random.nextInt(questSection.getInt("max-time", 5) - questSection.getInt("min-time", 3)) +
                questSection.getInt("min-time", 3);
        List<String> rewards = config.getConfigurationSection("rewards").getStringList("commands");
        String item = questSection.getStringList("items").get(random.nextInt(questSection.getStringList("items").size()));

        return new Quest(config.getString("display.name"), config.getString("display.description"),
                "fishing_" + item, amount, timeLimit, rewards);
    }
}