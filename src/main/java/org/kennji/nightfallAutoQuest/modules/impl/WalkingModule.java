package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.UUID;

public final class WalkingModule implements QuestModule {
    private final NightfallAutoQuest plugin;

    public WalkingModule(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }
    @Override
    public @NotNull String getType() {
        return "walking";
    }

    @Override
    public int processEvent(@NotNull UUID playerUID, @NotNull PlayerData data, @NotNull Quest quest, @NotNull Event event) {
        if (!(event instanceof PlayerMoveEvent moveEvent)) return 0;
        
        double distance = moveEvent.getFrom().distance(moveEvent.getTo());
        
        // Anti-Abuse: Speed check
        double maxSpeed = plugin.getConfigManager().getConfig().getDouble("anti-abuse.max-walking-speed", 1.5);
        if (distance > maxSpeed) {
            return 0;
        }

        return (int) distance; 
    }
}
