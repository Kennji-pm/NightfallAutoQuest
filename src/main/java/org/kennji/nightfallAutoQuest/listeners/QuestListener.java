package org.kennji.nightfallAutoQuest.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.modules.Quest;
import org.kennji.nightfallAutoQuest.utils.*;

import org.kennji.nightfallAutoQuest.database.PlayerData;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class QuestListener implements Listener {
    private final NightfallAutoQuest plugin;
    private final Logger logger;

    // Cached allowed worlds list to avoid reading config on every event
    private List<String> allowedWorldsCache;

    public QuestListener(NightfallAutoQuest plugin) {
        this.plugin = plugin;
        this.logger = plugin.getPluginLogger();
        reloadCache();
    }

    /**
     * Reload the cached configuration values.
     * Should be called when plugin configuration is reloaded.
     */
    public void reloadCache() {
        this.allowedWorldsCache = plugin.getConfigManager().getConfig().getStringList("allowed_worlds");
    }

    /**
     * Check if the player's current world is allowed for quest progress.
     * Uses cached world list for better performance.
     */
    private boolean isWorldAllowed(Player player) {
        return allowedWorldsCache.isEmpty() || Util.isWorldAllowed(player.getWorld().getName(), allowedWorldsCache);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        PlayerData playerData = plugin.getPlayerCacheManager().getPlayerData(uuid);

        if (playerData != null && playerData.activeQuest != null) {
            Quest quest = plugin.getQuestManager().getActiveQuest(uuid);
            if (quest != null && System.currentTimeMillis() < playerData.questExpiration) {
                quest.setProgress(playerData.questProgress);
                plugin.getBossBarManager().updateBossBar(player, quest, playerData.questExpiration);

                String questType = quest.getType();
                String taskPrefix = plugin.getMessageUtil().getMessage("quests_task." + questType);
                if (taskPrefix == null) {
                    taskPrefix = questType; // Fallback if translation not found
                }

                String fullTask = quest.getTask();
                String itemNameRaw = "";
                if (fullTask.contains("_")) {
                    itemNameRaw = fullTask.substring(fullTask.indexOf("_") + 1);
                }

                String displayItemName = "";
                if (!itemNameRaw.isEmpty()) {
                    try {
                        Material material = Material.valueOf(itemNameRaw.toUpperCase());
                        displayItemName = Util.capitalizeFully(material.name().replace("_", " "));
                    } catch (IllegalArgumentException e) {
                        displayItemName = Util.capitalizeFully(itemNameRaw.replace("_", " "));
                    }
                }

                String displayTask = taskPrefix;
                if (!displayItemName.isEmpty()) {
                    displayTask += " " + displayItemName;
                }

                plugin.getMessageUtil().sendMessage(player, "quest-assigned",
                        "%quest_name%", quest.getName(),
                        "%description%", quest.getDescription(),
                        "%amount%", String.valueOf(quest.getAmount()),
                        "%task%", displayTask,
                        "%time%", String.valueOf(quest.getTimeLimit()));
                logger.log(Level.INFO,
                        "Loaded active quest for player " + player.getName() + " from cache: " + quest.getName());
            } else if (playerData.activeQuest != null) {
                // Clear expired quest from cache and database if it was still present
                playerData.activeQuest = null;
                playerData.questProgress = 0;
                playerData.questExpiration = 0;
                plugin.getPlayerCacheManager().updatePlayerData(uuid, playerData);
                // The PlayerCacheManager will handle flushing this change to the database.
                logger.log(Level.INFO, "Cleared expired active quest for player " + player.getName() + " from cache.");
            }
        } else {
            logger.log(Level.FINE, "No active quest found for player " + player.getName() + " on join.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getQuestManager().saveQuest(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        var player = event.getPlayer();
        if (!isWorldAllowed(player))
            return;
        var material = event.getBlock().getType().name().toLowerCase();
        var task = "mining_" + material;
        plugin.getQuestManager().updateProgress(player, task, 1);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        var player = event.getPlayer();
        if (!isWorldAllowed(player))
            return;
        var material = event.getBlock().getType().name().toLowerCase();
        var task = "placing_" + material;
        plugin.getQuestManager().updateProgress(player, task, 1);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.isCancelled())
            return;
        var whoClicked = event.getWhoClicked();
        if (!(whoClicked instanceof Player player))
            return;
        if (!isWorldAllowed(player))
            return;
        var item = event.getCurrentItem();
        if (item == null)
            return;
        var material = item.getType().name().toLowerCase();
        var task = "crafting_" + material;
        var amount = item.getAmount();
        plugin.getQuestManager().updateProgress(player, task, amount);
    }

    @EventHandler
    public void onBlockHarvest(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        if (!isWorldAllowed(player))
            return;
        String material = event.getBlock().getType().name().toLowerCase();
        if (material.contains("wheat") || material.contains("carrot") || material.contains("potato") ||
                material.contains("beetroot") || material.contains("melon") || material.contains("pumpkin")) {
            String task = "farming_" + material;
            plugin.getQuestManager().updateProgress(player, task, 1);
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.isCancelled() || event.getState() != PlayerFishEvent.State.CAUGHT_FISH)
            return;
        Player player = event.getPlayer();
        if (!isWorldAllowed(player))
            return;
        if (!(event.getCaught() instanceof org.bukkit.entity.Item item))
            return;
        String material = item.getItemStack().getType().name().toLowerCase();
        String task = "fishing_" + material;
        plugin.getQuestManager().updateProgress(player, task, 1);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled())
            return;
        if (!(event.getDamager() instanceof Player player))
            return;
        if (!isWorldAllowed(player))
            return;
        String entity = event.getEntityType().name().toLowerCase();
        String task = "dealdamage_" + entity;
        int damage = (int) Math.max(0, event.getFinalDamage());
        plugin.getQuestManager().updateProgress(player, task, damage);
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getEnchanter();
        if (!isWorldAllowed(player))
            return;
        String material = event.getItem().getType().name().toLowerCase();
        String task = "enchanting_" + material;
        plugin.getQuestManager().updateProgress(player, task, 1);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null)
            return;
        if (!isWorldAllowed(player))
            return;
        String entity = event.getEntityType().name().toLowerCase();
        String task = "mobkilling_" + entity;
        plugin.getQuestManager().updateProgress(player, task, 1);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            Player player = event.getPlayer();
            if (!isWorldAllowed(player))
                return;
            plugin.getQuestManager().updateProgress(player, "walking", 1);
        }
    }

    @EventHandler
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        if (!isWorldAllowed(player))
            return;
        String material = event.getItemType().name().toLowerCase();
        String task = "smelting_" + material;
        int amount = event.getItemAmount();
        plugin.getQuestManager().updateProgress(player, task, amount);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Quest quest = plugin.getQuestManager().getActiveQuest(uuid);
        Long expiration = plugin.getQuestManager().getActiveQuestExpiration(uuid);

        if (quest != null && expiration != null && System.currentTimeMillis() < expiration) {
            if (!isWorldAllowed(player)) {
                plugin.getBossBarManager().removeBossBar(player);
            } else {
                plugin.getBossBarManager().updateBossBar(player, quest, expiration);
            }
        }
    }
}
