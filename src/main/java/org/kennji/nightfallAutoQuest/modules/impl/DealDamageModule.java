package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.UUID;

public final class DealDamageModule implements QuestModule {
    @Override
    public @NotNull String getType() {
        return "dealdamage";
    }

    @Override
    public int processEvent(@NotNull UUID playerUID, @NotNull PlayerData data, @NotNull Quest quest, @NotNull Event event) {
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return 0;
        
        return (int) Math.round(damageEvent.getFinalDamage());
    }
}
