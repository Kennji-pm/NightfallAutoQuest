package org.kennji.nightfallAutoQuest.modules;

import java.util.UUID;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;

/**
 * Interface for quest type implementations
 */
public interface QuestModule {

    /**
     * @return The unique type identifier (e.g., "mining", "killing")
     */
    @NotNull
    String getType();

    /**
     * Process an event and return the progress increment
     * 
     * @param playerUID The player's UUID
     * @param data      The player's current data
     * @param quest     The active quest
     * @param event     The event to process
     * @return amount to increment progress by
     */
    int processEvent(@NotNull UUID playerUID, @NotNull PlayerData data, @NotNull Quest quest, @NotNull Event event);
}
