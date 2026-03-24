package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.UUID;

public final class FarmingModule implements QuestModule {
    @Override
    public @NotNull String getType() {
        return "farming";
    }

    @Override
    public int processEvent(@NotNull UUID playerUID, @NotNull PlayerData data, @NotNull Quest quest, @NotNull Event event) {
        if (!(event instanceof BlockBreakEvent breakEvent)) return 0;
        
        Material material = breakEvent.getBlock().getType();
        String activeTask = data.activeTask();

        if (breakEvent.getBlock().getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() == ageable.getMaximumAge()) {
                if (activeTask == null || activeTask.equalsIgnoreCase("ANY") || activeTask.equalsIgnoreCase(material.name())) {
                    return 1;
                }
            }
        }
        return 0;
    }
}
