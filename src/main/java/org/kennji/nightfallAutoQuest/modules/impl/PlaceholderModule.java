package org.kennji.nightfallAutoQuest.modules.impl;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.UUID;

public final class PlaceholderModule implements QuestModule {
    @Override
    public @NotNull String getType() {
        return "placeholder";
    }

    @Override
    public int processEvent(@NotNull UUID playerUID, @NotNull PlayerData data, @NotNull Quest quest, @NotNull Event event) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return 0;
        
        Player player = Bukkit.getPlayer(playerUID);
        if (player == null) return 0;

        String activeTask = data.activeTask();
        if (activeTask == null) return 0;

        try {
            String valueStr = PlaceholderAPI.setPlaceholders(player, activeTask);
            int currentValue = (int) Double.parseDouble(valueStr);
            
            // Calculate total progress from start
            int currentProgressFromStart = currentValue - data.placeholderStartValue();
            
            // Increment is (Total Progress) - (Already Recorded Progress)
            int increment = currentProgressFromStart - data.questProgress();
            
            return Math.max(0, increment);
        } catch (Exception e) {
            return 0;
        }
    }
}
