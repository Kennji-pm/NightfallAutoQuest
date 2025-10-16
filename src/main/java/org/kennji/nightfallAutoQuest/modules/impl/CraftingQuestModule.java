package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.kennji.nightfallAutoQuest.modules.QuestModule;
import org.kennji.nightfallAutoQuest.modules.Quest;

import java.util.List;
import java.util.Random;

public class CraftingQuestModule implements QuestModule {
    @Override
    public String getType() {
        return "crafting";
    }

    @Override
    public Quest generateQuest(ConfigurationSection config) {
        Random random = new Random();
        ConfigurationSection questSection = config.getConfigurationSection("quests");
        int amount = random.nextInt(questSection.getInt("max-amount", 64) - questSection.getInt("min-amount", 1)) +
                questSection.getInt("min-amount", 1);
        int timeLimit = random.nextInt(questSection.getInt("max-time", 5) - questSection.getInt("min-time", 3)) +
                questSection.getInt("min-time", 3);
        List<String> rewards = config.getConfigurationSection("rewards").getStringList("commands");
        String item = questSection.getStringList("items").get(random.nextInt(questSection.getStringList("items").size()));

        return new Quest(config.getString("display.name"), config.getString("display.description"),
                "crafting_" + item, amount, timeLimit, rewards);
    }
}