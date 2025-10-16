package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.kennji.nightfallAutoQuest.modules.QuestModule;
import org.kennji.nightfallAutoQuest.modules.Quest;

import java.util.List;
import java.util.Random;

public class DealDamageQuestModule implements QuestModule {
    @Override
    public String getType() {
        return "dealdamage";
    }

    @Override
    public Quest generateQuest(ConfigurationSection config) {
        Random random = new Random();
        ConfigurationSection questSection = config.getConfigurationSection("quests");
        int amount = random.nextInt(questSection.getInt("max-amount", 100) - questSection.getInt("min-amount", 20)) +
                questSection.getInt("min-amount", 20);
        int timeLimit = random.nextInt(questSection.getInt("max-time", 5) - questSection.getInt("min-time", 3)) +
                questSection.getInt("min-time", 3);
        List<String> rewards = config.getConfigurationSection("rewards").getStringList("commands");
        String entity = questSection.getStringList("entities").get(random.nextInt(questSection.getStringList("entities").size()));

        return new Quest(config.getString("display.name"), config.getString("display.description"),
                "dealdamage_" + entity, amount, timeLimit, rewards);
    }
}