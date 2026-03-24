package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.CraftItemEvent;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.UUID;

public final class CraftingModule implements QuestModule {
    @Override
    public @NotNull String getType() {
        return "crafting";
    }

    @Override
    public int processEvent(@NotNull UUID playerUID, @NotNull PlayerData data, @NotNull Quest quest, @NotNull Event event) {
        if (!(event instanceof CraftItemEvent craftEvent)) return 0;
        
        String material = craftEvent.getRecipe().getResult().getType().name();
        String activeTask = data.activeTask();

        if (activeTask == null || activeTask.equalsIgnoreCase("ANY") || activeTask.equalsIgnoreCase(material)) {
            // Count all items in the result slot properly (shift click handled by Bukkit usually gives amount)
            return craftEvent.getRecipe().getResult().getAmount();
        }
        return 0;
    }
}
