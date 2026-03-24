package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.UUID;

public final class MiningModule implements QuestModule {
    private final NightfallAutoQuest plugin;

    public MiningModule(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }
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
            // Anti-Abuse: Don't count player-placed blocks for mining
            if (plugin.getBlockDataManager().isPlayerPlaced(breakEvent.getBlock())) {
                return 0;
            }
            return 1;
        }
        return 0;
    }
}
