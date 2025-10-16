package org.kennji.nightfallAutoQuest.modules;

import org.bukkit.configuration.ConfigurationSection;

public interface QuestModule {
    String getType();
    Quest generateQuest(ConfigurationSection config);
}