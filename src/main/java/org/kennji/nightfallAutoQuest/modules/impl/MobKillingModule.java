package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.UUID;

public final class MobKillingModule implements QuestModule {
    private final NightfallAutoQuest plugin;

    public MobKillingModule(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }
    @Override
    public @NotNull String getType() {
        return "mobkilling";
    }

    @Override
    public int processEvent(@NotNull UUID playerUID, @NotNull PlayerData data, @NotNull Quest quest, @NotNull Event event) {
        if (!(event instanceof EntityDeathEvent deathEvent)) return 0;
        
        EntityType type = deathEvent.getEntityType();
        String activeTask = data.activeTask();

        if (activeTask == null || activeTask.equalsIgnoreCase("ANY") || activeTask.equalsIgnoreCase(type.name())) {
            // Anti-Abuse: Ignore spawner-born mobs if configured
            if (plugin.getConfigManager().getConfig().getBoolean("anti-abuse.ignore-spawner-mobs", true)) {
                if (deathEvent.getEntity().hasMetadata("naq-spawner")) {
                    return 0;
                }
            }
            return 1;
        }
        return 0;
    }
}
