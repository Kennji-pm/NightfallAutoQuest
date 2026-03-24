package org.kennji.nightfallAutoQuest.modules.impl;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;

import java.util.UUID;

public final class SmeltingModule implements QuestModule {
    @Override
    public @NotNull String getType() {
        return "smelting";
    }

    @Override
    public int processEvent(@NotNull UUID playerUID, @NotNull PlayerData data, @NotNull Quest quest, @NotNull Event event) {
        if (!(event instanceof InventoryClickEvent clickEvent)) return 0;
        
        InventoryType type = clickEvent.getInventory().getType();
        if (type != InventoryType.FURNACE && type != InventoryType.BLAST_FURNACE && type != InventoryType.SMOKER) {
            return 0;
        }

        // Slot 2 is usually the result slot for furnaces
        if (clickEvent.getRawSlot() != 2) return 0;
        if (clickEvent.getCurrentItem() == null || clickEvent.getCurrentItem().getType() == Material.AIR) return 0;

        String material = clickEvent.getCurrentItem().getType().name();
        String activeTask = data.activeTask();

        if (activeTask == null || activeTask.equalsIgnoreCase("ANY") || activeTask.equalsIgnoreCase(material)) {
            int amount = clickEvent.getCurrentItem().getAmount();
            // Handle shift click separately if needed, but currentAmount works for simple pickup
            if (clickEvent.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                return amount;
            }
            return amount;
        }
        return 0;
    }
}
