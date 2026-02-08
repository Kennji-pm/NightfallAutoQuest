package org.kennji.nightfallAutoQuest.managers;

import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.database.PlayerData;
import org.kennji.nightfallAutoQuest.modules.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;
import org.kennji.nightfallAutoQuest.modules.impl.PlaceholderQuestModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.kennji.nightfallAutoQuest.utils.*;

import java.io.File;
import java.sql.ResultSet; // Keep this import
import java.sql.SQLException; // Keep this import
import java.util.*;
import java.util.logging.Level;

public class QuestManager {
    private final NightfallAutoQuest plugin;
    private final Map<UUID, Quest> activeQuests;
    private final Map<UUID, Long> activeQuestExpirations;
    private final Map<String, Quest> availableQuests;
    private final Random random;
    private int failedQuestCount;
    private final Logger logger;

    public QuestManager(NightfallAutoQuest plugin) {
        this.plugin = plugin;
        this.activeQuests = new HashMap<>();
        this.activeQuestExpirations = new HashMap<>();
        this.availableQuests = new HashMap<>();
        this.random = new Random();
        this.failedQuestCount = 0;
        this.logger = plugin.getPluginLogger();
    }

    public void saveQuest(Player player) {
        UUID uuid = player.getUniqueId();
        Quest quest = activeQuests.get(uuid);
        if (quest != null) {
            Long expiration = activeQuestExpirations.get(uuid);
            if (expiration != null) {
                PlayerData playerData = plugin.getPlayerCacheManager().getPlayerData(uuid);
                playerData.activeQuest = quest.getName();
                playerData.questProgress = quest.getCurrentProgress();
                playerData.questExpiration = expiration;
                plugin.getPlayerCacheManager().updatePlayerData(uuid, playerData);
            }
        }
    }

    public void loadQuests() {
        availableQuests.clear();
        failedQuestCount = 0;
        File questsFolder = new File(plugin.getDataFolder(), "quests");
        if (!questsFolder.exists()) {
            questsFolder.mkdirs();
        }

        // Copy default quest files from resources if they don't exist
        String[] defaultQuests = {
                // Mobkilling quests
                "tieu_diet_zombie.yml", "san_skeleton.yml", "diet_tru_creeper.yml", "chien_binh_nhen.yml",
                // Mining quests
                "khai_thac_quang.yml", "tho_mo_kim_cuong.yml",
                // Other quests
                "nong_dan_cham_chi.yml", "ngu_dan_lao_luyen.yml", "tho_ren_tai_ba.yml",
                "luyen_kim_than_toc.yml", "kien_truc_su.yml", "phap_su_phu_phep.yml", "nha_tham_hiem.yml",
                // Placeholder quests
                "tich_luy_tien_te.yml", "tho_san_kinh_nghiem.yml"
        };
        for (String questFileName : defaultQuests) {
            File file = new File(questsFolder, questFileName);
            if (!file.exists()) {
                plugin.saveResource("quests/" + questFileName, false);
            }
        }

        for (File file : questsFolder.listFiles((dir, name) -> name.endsWith(".yml"))) {
            String questName = null;
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                ConfigurationSection questSection = config.getConfigurationSection("quests");
                questName = file.getName().replace(".yml", "");

                if (questSection == null) {
                    logger.log(Level.SEVERE, "Failed to load quest " + questName + " from " + file.getName()
                            + ": Missing quests section");
                    failedQuestCount++;
                    continue;
                }

                String type = questSection.getString("type");
                QuestModule module = plugin.getModuleManager().getModules().get(type);
                if (module == null || !plugin.getConfigManager().getConfig().getBoolean("modules." + type, true)) {
                    logger.log(Level.SEVERE, "Failed to load quest " + questName + " from " + file.getName()
                            + ": Invalid or disabled module: " + type);
                    failedQuestCount++;
                    continue;
                }

                Quest quest = module.generateQuest(config);
                availableQuests.put(questName, quest);
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "Failed to load quest " + questName + " from " + file.getName() + ": " + e.getMessage());
                failedQuestCount++;
            }
        }
    }

    public void loadActiveQuests() {
        // This method should load active quests from the database and populate the
        // cache.
        // The PlayerCacheManager.getPlayerData(UUID) method will handle loading from DB
        // if not in cache.
        // However, for initial loading of *all* active quests, we still need to query
        // the DB directly.
        // This is a special case for plugin startup.
        try (ResultSet rs = plugin.getDatabaseManager().getActiveQuests()) {
            while (rs.next()) {
                UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                String questName = rs.getString("quest_name");
                int progress = rs.getInt("progress");
                long expiration = rs.getLong("expiration");
                int placeholderStartValue = rs.getInt("placeholder_start_value");
                Quest quest = availableQuests.get(questName);

                PlayerData playerData = plugin.getPlayerCacheManager().getPlayerData(playerUUID); // Ensure player data
                                                                                                  // is in cache

                if (quest != null && System.currentTimeMillis() < expiration) {
                    quest.setProgress(progress);
                    activeQuests.put(playerUUID, quest);
                    activeQuestExpirations.put(playerUUID, expiration);
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player != null) {
                        plugin.getBossBarManager().updateBossBar(player, quest, expiration);
                    }
                    // Update cached player data with active quest details
                    playerData.activeQuest = quest.getName();
                    playerData.questProgress = quest.getCurrentProgress();
                    playerData.questExpiration = expiration;
                    playerData.placeholderStartValue = placeholderStartValue;
                    plugin.getPlayerCacheManager().updatePlayerData(playerUUID, playerData);
                } else {
                    // Clear expired or invalid quests from cache and database
                    playerData.activeQuest = null;
                    playerData.questProgress = 0;
                    playerData.questExpiration = 0;
                    playerData.placeholderStartValue = 0;
                    plugin.getPlayerCacheManager().updatePlayerData(playerUUID, playerData);
                    // The PlayerCacheManager will handle flushing this change to the database.
                    logger.log(Level.INFO,
                            "Removed expired or invalid active quest for UUID: " + playerUUID + " from cache.");
                }
            }
        } catch (SQLException e) {
            plugin.getPluginLogger().log(Level.SEVERE, "Failed to load active quests: " + e.getMessage());
        }
    }

    public void updateProgress(Player player, String task, int amount) {
        UUID uuid = player.getUniqueId();
        Quest quest = activeQuests.get(uuid);
        if (quest != null && quest.getTask().equals(task)) {
            quest.addProgress(amount);
            Long expiration = activeQuestExpirations.get(uuid);
            if (expiration != null) {
                PlayerData playerData = plugin.getPlayerCacheManager().getPlayerData(uuid);
                playerData.activeQuest = quest.getName();
                playerData.questProgress = quest.getCurrentProgress();
                playerData.questExpiration = expiration;
                plugin.getPlayerCacheManager().updatePlayerData(uuid, playerData);
                plugin.getBossBarManager().updateBossBar(player, quest, expiration);
                if (quest.isCompleted()) {
                    completeQuest(player, quest);
                }
            }
        }
    }

    public void completeQuest(Player player, Quest quest) {
        UUID uuid = player.getUniqueId();
        activeQuests.remove(uuid);
        activeQuestExpirations.remove(uuid);

        PlayerData playerData = plugin.getPlayerCacheManager().getPlayerData(uuid);
        playerData.activeQuest = null;
        playerData.questProgress = 0;
        playerData.questExpiration = 0;
        playerData.placeholderStartValue = 0;
        playerData.addCompletion(); // Increment completions and auto-update rate
        plugin.getPlayerCacheManager().updatePlayerData(uuid, playerData);

        plugin.getBossBarManager().showCompletionMessage(player, quest.getName());

        for (String command : quest.getRewards()) {
            // Check for percentage-based reward with [~XX%] or [~XX.X%] prefix
            double chance = 100.0;
            String actualCommand = command;
            if (command.matches("\\[~\\d+(\\.\\d+)?%].*")) {
                try {
                    String percentage = command.substring(2, command.indexOf("%"));
                    chance = Double.parseDouble(percentage);
                    if (chance < 0 || chance > 100) {
                        logger.log(Level.WARNING,
                                "Invalid percentage value in reward for quest " + quest.getName() + ": " + command);
                        continue;
                    }
                    actualCommand = command.substring(command.indexOf("]") + 1).trim();
                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING,
                            "Invalid percentage format in reward for quest " + quest.getName() + ": " + command);
                    continue;
                }
            }

            if (random.nextDouble() * 100 <= chance) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), actualCommand.replace("%player%", player.getName()));
            }
        }

        plugin.getMessageUtil().sendMessage(player, "quest-completed",
                "%quest_name%", quest.getName());

        plugin.getSoundUtil().playSound(player, "complete");
    }

    public void failQuest(Player player) {
        UUID uuid = player.getUniqueId();
        Quest quest = activeQuests.get(uuid);
        if (quest != null) {
            activeQuests.remove(uuid);
            activeQuestExpirations.remove(uuid);

            PlayerData playerData = plugin.getPlayerCacheManager().getPlayerData(uuid);
            playerData.activeQuest = null;
            playerData.questProgress = 0;
            playerData.questExpiration = 0;
            playerData.placeholderStartValue = 0;
            playerData.addFailure(); // Increment failures and auto-update rate
            plugin.getPlayerCacheManager().updatePlayerData(uuid, playerData);

            // Show failure message on BossBar
            plugin.getBossBarManager().showFailureMessage(player, quest.getName());

            plugin.getMessageUtil().sendMessage(player, "quest-failed",
                    "%quest_name%", quest.getName());
            logger.log(Level.INFO, "Player " + player.getName() + " failed quest: " + quest.getName());

            plugin.getSoundUtil().playSound(player, "fail");
        }
    }

    public void assignRandomQuest(Player player) {
        List<Quest> available = new ArrayList<>(availableQuests.values());
        if (available.isEmpty())
            return;

        // Check if player's world is allowed
        java.util.List<String> allowedWorlds = plugin.getConfigManager().getConfig().getStringList("allowed_worlds");
        if (!allowedWorlds.isEmpty() && !Util.isWorldAllowed(player.getWorld().getName(), allowedWorlds)) {
            return; // Do not assign quest if world is not allowed
        }

        UUID uuid = player.getUniqueId();
        Quest quest = available.get(random.nextInt(available.size()));
        quest.setProgress(0);
        activeQuests.put(uuid, quest);
        long expiration = System.currentTimeMillis() + ((long) quest.getTimeLimit() * 60 * 1000);
        activeQuestExpirations.put(uuid, expiration);

        PlayerData playerData = plugin.getPlayerCacheManager().getPlayerData(uuid);

        // Set placeholder start value when assigning quest
        if (quest.getTask().startsWith("placeholder_")) {
            String placeholder = quest.getTask().substring("placeholder_".length());
            playerData.placeholderStartValue = PlaceholderQuestModule.getPlaceholderValue(player, placeholder);
        } else {
            playerData.placeholderStartValue = 0;
        }

        playerData.activeQuest = quest.getName();
        playerData.questProgress = quest.getCurrentProgress();
        playerData.questExpiration = expiration;
        plugin.getPlayerCacheManager().updatePlayerData(uuid, playerData);

        plugin.getBossBarManager().updateBossBar(player, quest, expiration);
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

        plugin.getSoundUtil().playSound(player, "assign");
    }

    public void giveUpQuest(Player player) {
        UUID uuid = player.getUniqueId();
        Quest quest = activeQuests.get(uuid);
        if (quest != null) {
            activeQuests.remove(uuid);
            activeQuestExpirations.remove(uuid);

            PlayerData playerData = plugin.getPlayerCacheManager().getPlayerData(uuid);
            playerData.activeQuest = null;
            playerData.questProgress = 0;
            playerData.questExpiration = 0;
            playerData.placeholderStartValue = 0;
            playerData.addFailure(); // Increment failures and auto-update rate
            plugin.getPlayerCacheManager().updatePlayerData(uuid, playerData);

            plugin.getBossBarManager().showGiveUpMessage(player);
            plugin.getMessageUtil().sendMessage(player, "quest-giveup");
            logger.log(Level.INFO, "Player " + player.getName() + " gave up quest: " + quest.getName());
            plugin.getSoundUtil().playSound(player, "fail");
        }
    }

    public Quest getActiveQuest(UUID uuid) {
        return activeQuests.get(uuid);
    }

    public Long getActiveQuestExpiration(UUID uuid) {
        return activeQuestExpirations.get(uuid);
    }

    public int getQuestCount() {
        return availableQuests.size();
    }

    public int getFailedQuestCount() {
        return failedQuestCount;
    }
}
