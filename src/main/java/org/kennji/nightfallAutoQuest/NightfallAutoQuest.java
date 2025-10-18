package org.kennji.nightfallAutoQuest;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.kennji.nightfallAutoQuest.commands.AutoQuestCommand;
import org.kennji.nightfallAutoQuest.database.DatabaseManager;
import org.kennji.nightfallAutoQuest.expansion.NightfallAutoQuestExpansion;
import org.kennji.nightfallAutoQuest.listeners.QuestListener;
import org.kennji.nightfallAutoQuest.managers.*;
import org.kennji.nightfallAutoQuest.modules.Quest;
import org.kennji.nightfallAutoQuest.utils.*;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.kennji.nightfallAutoQuest.database.PlayerCacheManager;

import java.util.logging.Level;

/**
 * ╔═══════════════════════════════════════════════════════════╗
 * ║           NIGHTFALL AUTO QUEST PLUGIN                     ║
 * ║         Automatic Quest System for Minecraft              ║
 * ╠═══════════════════════════════════════════════════════════╣
 * ║  Author: _kennji                                          ║
 * ║  Description: Advanced quest management system            ║
 * ╚═══════════════════════════════════════════════════════════╝
 */
public final class NightfallAutoQuest extends JavaPlugin {

    // ═══════════════════════════════════════════════════════════
    //                    MANAGER INSTANCES
    // ═══════════════════════════════════════════════════════════
    
    private ConfigManager configManager;
    private MessageUtil messageUtil;
    private SoundUtil soundUtil;
    private QuestManager questManager;
    private DatabaseManager databaseManager;
    private PlayerCacheManager playerCacheManager;
    private ModuleManager moduleManager;
    private BossBarManager bossBarManager;
    private CommandManager commandManager;
    private Logger pluginLogger;

    private final Random random = new Random();

    // ═══════════════════════════════════════════════════════════
    //                    PLUGIN LIFECYCLE
    // ═══════════════════════════════════════════════════════════

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        // ┌─────────────────────────────────────────────────────┐
        // │              PLUGIN STARTUP BANNER                   │
        // └─────────────────────────────────────────────────────┘

        pluginLogger = new Logger(this);
        
        pluginLogger.log(Level.INFO, "════════════════════════════════════════════════════");
        pluginLogger.log(Level.INFO, "");
        pluginLogger.log(Level.INFO, "      🌙 NIGHTFALL AUTO QUEST 🌙");
        pluginLogger.log(Level.INFO, "          Starting up...");
        pluginLogger.log(Level.INFO, "");
        pluginLogger.log(Level.INFO, "════════════════════════════════════════════════════");
        pluginLogger.log(Level.INFO, "  👤 Author: _kennji");
        pluginLogger.log(Level.INFO, "  📦 Version: " + getDescription().getVersion());
        pluginLogger.log(Level.INFO, "════════════════════════════════════════════════════");

        // ┌─────────────────────────────────────────────────────┐
        // │           INITIALIZE CORE MANAGERS                   │
        // └─────────────────────────────────────────────────────┘
        configManager = new ConfigManager(this);
        messageUtil = new MessageUtil(this);
        soundUtil = new SoundUtil(this);
        questManager = new QuestManager(this);
        moduleManager = new ModuleManager(this);
        bossBarManager = new BossBarManager(this);
        commandManager = new CommandManager(this);
        databaseManager = new DatabaseManager(this, configManager.getConfig().getString("database.type", "sqlite"));
        playerCacheManager = new PlayerCacheManager(this, databaseManager, configManager.getConfig().getLong("cache.save-interval-seconds", 300));

        // ┌─────────────────────────────────────────────────────┐
        // │            LOAD MODULES AND QUESTS                   │
        // └─────────────────────────────────────────────────────┘
        try {
            moduleManager.loadModules();
            questManager.loadQuests();
            pluginLogger.log(Level.INFO, "✓ Loaded " + questManager.getQuestCount() + " quests successfully");
            if (questManager.getFailedQuestCount() > 0) {
                pluginLogger.log(Level.WARNING, "⚠ " + questManager.getFailedQuestCount() + " quests failed to load");
            }
        } catch (Exception e) {
            pluginLogger.log(Level.SEVERE, "✗ CRITICAL: Failed to load modules or quests - " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // ┌─────────────────────────────────────────────────────┐
        // │          SETUP DATABASE CONNECTION                   │
        // └─────────────────────────────────────────────────────┘
        databaseManager.initialize();

        // ┌─────────────────────────────────────────────────────┐
        // │         LOAD ACTIVE QUESTS FROM DATABASE             │
        // └─────────────────────────────────────────────────────┘
        questManager.loadActiveQuests();

        // ┌─────────────────────────────────────────────────────┐
        // │       REGISTER COMMANDS AND LISTENERS                │
        // └─────────────────────────────────────────────────────┘
        getCommand("nightfallautoquest").setExecutor(new AutoQuestCommand(this));
        getServer().getPluginManager().registerEvents(new QuestListener(this), this);

        // ┌─────────────────────────────────────────────────────┐
        // │         PLACEHOLDERAPI INTEGRATION                   │
        // └─────────────────────────────────────────────────────┘
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new NightfallAutoQuestExpansion(this).register();
            pluginLogger.log(Level.INFO, "✓ PlaceholderAPI hooked successfully");
        } else {
            pluginLogger.log(Level.WARNING, "⚠ PlaceholderAPI not found - Placeholders disabled");
        }

        // ┌─────────────────────────────────────────────────────┐
        // │        SCHEDULE AUTOMATIC QUEST ASSIGNMENT           │
        // └─────────────────────────────────────────────────────┘
        scheduleQuestAssignment();

        // ┌─────────────────────────────────────────────────────┐
        // │              STARTUP COMPLETE                        │
        // └─────────────────────────────────────────────────────┘
        pluginLogger.log(Level.INFO, "════════════════════════════════════════════════════");
        pluginLogger.log(Level.INFO, "  ✓ Plugin enabled successfully!");
        pluginLogger.log(Level.INFO, "  ⏱ Startup time: " + (System.currentTimeMillis() - start) + "ms");
        pluginLogger.log(Level.INFO, "════════════════════════════════════════════════════");
    }

    @Override
    public void onDisable() {
        pluginLogger.log(Level.INFO, "════════════════════════════════════════════════════");
        pluginLogger.log(Level.INFO, "     🌙 NIGHTFALL AUTO QUEST 🌙");
        pluginLogger.log(Level.INFO, "        Shutting down...");
        pluginLogger.log(Level.INFO, "════════════════════════════════════════════════════");

        if (playerCacheManager != null) {
            playerCacheManager.shutdown();
            pluginLogger.log(Level.INFO, "✓ Player cache flushed and saved");
        }
        if (databaseManager != null) {
            databaseManager.close();
            pluginLogger.log(Level.INFO, "✓ Database connection closed");
        }
        if (pluginLogger != null) {
            pluginLogger.log(Level.INFO, "✓ NightfallAutoQuest disabled successfully");
        }
    }

    // ═══════════════════════════════════════════════════════════
    //                 QUEST ASSIGNMENT SCHEDULER
    // ═══════════════════════════════════════════════════════════

    private void scheduleQuestAssignment() {
        FileConfiguration config = configManager.getConfig();
        long interval = config.getLong("quest.assign-interval", 3600) * 20L;
        double assignPercentage;

        try {
            assignPercentage = config.getDouble("quest.assign-percentage", 50.0);
            if (assignPercentage < 0 || assignPercentage > 100) {
                pluginLogger.log(Level.WARNING, "⚠ Invalid assign-percentage (" + assignPercentage + "%) - Defaulting to 50%");
                assignPercentage = 50.0;
            }
        } catch (Exception e) {
            pluginLogger.log(Level.WARNING, "⚠ Error parsing assign-percentage - Defaulting to 50%: " + e.getMessage());
            assignPercentage = 50.0;
        }

        if (interval <= 0) {
            pluginLogger.log(Level.WARNING, "⚠ Invalid assign-interval (" + interval + ") - Defaulting to 3600s");
            interval = 3600 * 20L;
        }

        final double finalAssignPercentage = assignPercentage;
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (onlinePlayers.isEmpty()) {
                return;
            }

            if (questManager.getQuestCount() == 0) {
                pluginLogger.log(Level.WARNING, "⚠ No quests available to assign");
                return;
            }

            int playersToAssign = (int) Math.ceil(onlinePlayers.size() * (finalAssignPercentage / 100.0));
            Collections.shuffle(onlinePlayers, random);
            List<Player> selectedPlayers = onlinePlayers.subList(0, Math.min(playersToAssign, onlinePlayers.size()));

            int assignedCount = 0;
            for (Player player : selectedPlayers) {
                Quest quest = questManager.getActiveQuest(player.getUniqueId());
                Long expiration = questManager.getActiveQuestExpiration(player.getUniqueId());
                boolean shouldAssign = quest == null || !quest.isPresent() || (expiration != null && System.currentTimeMillis() >= expiration);

                if (shouldAssign) {
                    questManager.assignRandomQuest(player);
                    assignedCount++;
                }
            }

            pluginLogger.log(Level.INFO, "════════════════════════════════════════════════════");
            pluginLogger.log(Level.INFO, "🎯 Quest Assignment Report:");
            pluginLogger.log(Level.INFO, "   • Assigned: " + assignedCount + "/" + selectedPlayers.size() + " selected players");
            pluginLogger.log(Level.INFO, "   • Online: " + onlinePlayers.size() + " total players");
            pluginLogger.log(Level.INFO, "   • Selection Rate: " + finalAssignPercentage + "%");
            pluginLogger.log(Level.INFO, "════════════════════════════════════════════════════");
        }, interval, interval);
    }

    // ═══════════════════════════════════════════════════════════
    //                    GETTER METHODS
    // ═══════════════════════════════════════════════════════════

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public SoundUtil getSoundUtil() {
        return soundUtil;
    }

    public Logger getPluginLogger() {
        return pluginLogger;
    }

    public PlayerCacheManager getPlayerCacheManager() {
        return playerCacheManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
