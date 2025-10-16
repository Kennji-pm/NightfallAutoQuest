package org.kennji.nightfallAutoQuest.modules.impl;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.kennji.nightfallAutoQuest.modules.QuestModule;
import org.kennji.nightfallAutoQuest.modules.Quest;

import java.util.List;
import java.util.Random;

public class PlaceholderQuestModule implements QuestModule {
    @Override
    public String getType() {
        return "placeholder";
    }

    @Override
    public Quest generateQuest(ConfigurationSection config) {
        Random random = new Random();
        ConfigurationSection questSection = config.getConfigurationSection("quests");
        int amount = random.nextInt(questSection.getInt("max-amount", 100) - questSection.getInt("min-amount", 10)) +
                questSection.getInt("min-amount", 10);
        int timeLimit = random.nextInt(questSection.getInt("max-time", 5) - questSection.getInt("min-time", 3)) +
                questSection.getInt("min-time", 3);
        List<String> rewards = config.getConfigurationSection("rewards").getStringList("commands");

        // Get placeholder configurations
        ConfigurationSection placeholderSection = questSection.getConfigurationSection("placeholders");
        String placeholder = "";
        if (placeholderSection != null) {
            List<String> placeholders = placeholderSection.getStringList("values");
            if (!placeholders.isEmpty()) {
                placeholder = placeholders.get(random.nextInt(placeholders.size()));
            }
        }

        return new Quest(config.getString("display.name"), config.getString("display.description"),
                "placeholder_" + placeholder, amount, timeLimit, rewards);
    }

    /**
     * Gets the current placeholder value for a player
     * @param player The player
     * @param placeholder The placeholder string (without %)
     * @return The parsed integer value, or 0 if invalid
     */
    public static int getPlaceholderValue(Player player, String placeholder) {
        try {
            String rawValue = PlaceholderAPI.setPlaceholders(player, "%" + placeholder + "%");
            if (rawValue == null || rawValue.equals("%" + placeholder + "%")) {
                // Placeholder not found or not registered
                return 0;
            }
            return Integer.parseInt(rawValue.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            // If not a number, can't track progress
            return 0;
        }
    }

    /**
     * Calculates placeholder progress for a quest
     * @param player The player
     * @param placeholder The placeholder identifier
     * @param startValue The starting value when quest was assigned
     * @param requiredAmount The required progress amount
     * @return The progress made (between 0 and requiredAmount)
     */
    public static int calculatePlaceholderProgress(Player player, String placeholder, int startValue, int requiredAmount) {
        int currentValue = getPlaceholderValue(player, placeholder);
        int progress = Math.max(0, currentValue - startValue);
        return Math.min(progress, requiredAmount);
    }
}
