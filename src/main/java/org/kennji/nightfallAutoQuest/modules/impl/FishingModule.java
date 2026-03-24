package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.UUID;

public final class FishingModule implements QuestModule {
    @Override
    public @NotNull String getType() {
        return "fishing";
    }

    @Override
    public int processEvent(@NotNull UUID playerUID, @NotNull PlayerData data, @NotNull Quest quest, @NotNull Event event) {
        if (!(event instanceof PlayerFishEvent fishEvent)) return 0;
        
        if (fishEvent.getState() == PlayerFishEvent.State.CAUGHT_FISH && fishEvent.getCaught() instanceof Item item) {
            String material = item.getItemStack().getType().name();
            String activeTask = data.activeTask();

            if (activeTask == null || activeTask.equalsIgnoreCase("ANY") || activeTask.equalsIgnoreCase(material)) {
                return 1;
            }
        }
        return 0;
    }
}
