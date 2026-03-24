package org.kennji.nightfallAutoQuest;

import org.bukkit.plugin.java.JavaPlugin;
import org.kennji.nightfallAutoQuest.command.subcommands.*;
import org.kennji.nightfallAutoQuest.expansion.NightfallAutoQuestExpansion;
import org.kennji.nightfallAutoQuest.listener.PlayerListener;
import org.kennji.nightfallAutoQuest.manager.*;
import org.kennji.nightfallAutoQuest.repository.PlayerRepository;
import org.kennji.nightfallAutoQuest.service.QuestService;
import org.kennji.nightfallAutoQuest.util.MessageUtil;
import org.kennji.nightfallAutoQuest.util.PluginLogger;
import org.kennji.nightfallAutoQuest.util.SoundUtil;

import java.util.concurrent.TimeUnit;

public final class NightfallAutoQuest extends JavaPlugin {
    private PluginLogger pluginLogger;
    private ConfigManager configManager;
    private MessageUtil messageUtil;
    private SoundUtil soundUtil;
    private PlayerManager playerManager;
    private DatabaseManager databaseManager;
    private QuestManager questManager;
    private BossBarManager bossBarManager;
    private CommandManager commandManager;
    private LeaderboardManager leaderboardManager;
    private QuestScheduler questScheduler;
    private QuestService questService;
    private BlockDataManager blockDataManager;

    @Override
    public void onEnable() {
        // 1. Initialize Logging & Config
        this.pluginLogger = new PluginLogger(this);
        this.configManager = new ConfigManager(this);
        this.configManager.initialize();

        // 2. Initialize Data Layer
        this.databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        PlayerRepository playerRepository = new PlayerRepository(this);
        this.playerManager = new PlayerManager(playerRepository);
        this.questManager = new QuestManager(this);
        this.blockDataManager = new BlockDataManager(this);
        new QuestLoader(this).loadAll();
        this.questService = new QuestService(this);

        // 3. Initialize Utils
        this.messageUtil = new MessageUtil(this);
        this.soundUtil = new SoundUtil(this);
        this.bossBarManager = new BossBarManager(this);
        this.leaderboardManager = new LeaderboardManager(this);
        this.questScheduler = new QuestScheduler(this);

        // 4. Register Commands & Listeners
        this.commandManager = new CommandManager(this);
        this.commandManager.register(new HelpCommand(this));
        this.commandManager.register(new ReloadCommand(this));
        this.commandManager.register(new StatsCommand(this));
        this.commandManager.register(new QuestCommand(this));
        this.commandManager.register(new GiveUpCommand(this));
        this.commandManager.register(new PurgeCommand(this));
        this.commandManager.register(new TopCommand(this));
        this.commandManager.register(new GetDataCommand(this));

        getCommand("nightfallautoquest").setExecutor(commandManager);
        getCommand("nightfallautoquest").setTabCompleter(commandManager);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new NightfallAutoQuestExpansion(this).register();
        }

        // 5. Start Background Tasks
        long saveInterval = configManager.getConfig().getLong("cache.save-interval-seconds", 300);
        getServer().getAsyncScheduler().runAtFixedRate(this, (task) -> {
            playerManager.saveAll();
        }, 1, saveInterval, TimeUnit.SECONDS);
        
        // Start Scheduler
        this.questScheduler.start();

        pluginLogger.info("<green>Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (playerManager != null) {
            playerManager.saveAll();
        }
        if (bossBarManager != null) {
            bossBarManager.removeAll();
        }
        pluginLogger.info("<red>Plugin disabled.");
    }

    public PluginLogger getPluginLogger() {
        return pluginLogger;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public SoundUtil getSoundUtil() {
        return soundUtil;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public QuestScheduler getQuestScheduler() {
        return questScheduler;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public QuestService getQuestService() {
        return questService;
    }

    public BlockDataManager getBlockDataManager() {
        return blockDataManager;
    }
}
