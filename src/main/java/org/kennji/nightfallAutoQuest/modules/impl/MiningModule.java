package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.UUID;

public final class MiningModule implements QuestModule {
    @Override
    public @NotNull String getType() {
        return "mining";
    }

    @Override
    public int processEvent(@NotNull UUID playerUID, @NotNull PlayerData data, @NotNull Quest quest, @NotNull Event event) {
        if (!(event instanceof BlockBreakEvent breakEvent)) return 0;
        
        String material = breakEvent.getBlock().getType().name();
        String activeTask = data.activeTask();
        
        if (activeTask == null || activeTask.equalsIgnoreCase("ANY") || activeTask.equalsIgnoreCase(material)) {
            return 1;
        }
        return 0;
    }
}
