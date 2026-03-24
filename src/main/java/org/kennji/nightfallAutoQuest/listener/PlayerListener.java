package org.kennji.nightfallAutoQuest.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

public final class PlayerListener implements Listener {
    private final NightfallAutoQuest plugin;

    public PlayerListener(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(@NotNull PlayerJoinEvent event) {
        plugin.getPlayerManager().loadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        plugin.getBossBarManager().remove(event.getPlayer());
        plugin.getPlayerManager().unloadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(@NotNull BlockBreakEvent event) {
        plugin.getQuestService().handleEvent(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(@NotNull BlockPlaceEvent event) {
        plugin.getBlockDataManager().markAsPlaced(event.getBlock());
        plugin.getQuestService().handleEvent(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(@NotNull ChunkUnloadEvent event) {
        // Purge data for unloaded chunks to prevent memory leaks
        plugin.getBlockDataManager().clearChunk(((long) event.getChunk().getX() << 32) | (event.getChunk().getZ() & 0xFFFFFFFFL));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(@NotNull CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            event.getEntity().setMetadata("naq-spawner", new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(@NotNull EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            plugin.getQuestService().handleEvent(killer, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(@NotNull CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            plugin.getQuestService().handleEvent(player, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(@NotNull PlayerFishEvent event) {
        plugin.getQuestService().handleEvent(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            plugin.getQuestService().handleEvent(player, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(@NotNull EnchantItemEvent event) {
        plugin.getQuestService().handleEvent(event.getEnchanter(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            plugin.getQuestService().handleEvent(player, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(@NotNull PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() || 
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            plugin.getQuestService().handleEvent(event.getPlayer(), event);
        }
    }
}
