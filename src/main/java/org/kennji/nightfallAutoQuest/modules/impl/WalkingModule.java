package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.UUID;

public final class WalkingModule implements QuestModule {
    @Override
    public @NotNull String getType() {
        return "walking";
    }

    @Override
    public int processEvent(@NotNull UUID playerUID, @NotNull PlayerData data, @NotNull Quest quest, @NotNull Event event) {
        if (!(event instanceof PlayerMoveEvent moveEvent)) return 0;
        
        double distance = moveEvent.getFrom().distance(moveEvent.getTo());
        // For distance-based quests, we might need floating point progress? 
        // But the model uses int. Let's contribute when full integer reached.
        // Actually, let's just count blocks moved.
        return (int) distance; 
    }
}
