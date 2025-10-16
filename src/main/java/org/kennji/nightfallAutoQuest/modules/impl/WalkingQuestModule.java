package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.kennji.nightfallAutoQuest.modules.QuestModule;
import org.kennji.nightfallAutoQuest.modules.Quest;

import java.util.List;
import java.util.Random;

public class WalkingQuestModule implements QuestModule {
    @Override
    public String getType() {
        return "walking";
    }

    @Override
    public Quest generateQuest(ConfigurationSection config) {
        Random random = new Random();
        ConfigurationSection questSection = config.getConfigurationSection("quests");
        int amount = random.nextInt(questSection.getInt("max-amount", 1000) - questSection.getInt("min-amount", 100)) +
                questSection.getInt("min-amount", 100);
        int timeLimit = random.nextInt(questSection.getInt("max-time", 5) - questSection.getInt("min-time", 3)) +
                questSection.getInt("min-time", 3);
        List<String> rewards = config.getConfigurationSection("rewards").getStringList("commands");

        return new Quest(config.getString("display.name"), config.getString("display.description"),
                "walking", amount, timeLimit, rewards);
    }
}