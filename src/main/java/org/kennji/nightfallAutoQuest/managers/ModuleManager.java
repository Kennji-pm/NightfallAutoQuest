package org.kennji.nightfallAutoQuest.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.modules.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;
import org.kennji.nightfallAutoQuest.modules.impl.*;

import java.util.HashMap;
import java.util.Map;

public class ModuleManager {
    private final NightfallAutoQuest plugin;
    private final Map<String, QuestModule> modules;
    private final Map<String, Class<? extends QuestModule>> availableModules;

    public ModuleManager(NightfallAutoQuest plugin) {
        this.plugin = plugin;
        this.modules = new HashMap<>();
        this.availableModules = new HashMap<>();
        availableModules.put("mining", MiningQuestModule.class);
        availableModules.put("placing", PlacingQuestModule.class);
        availableModules.put("crafting", CraftingQuestModule.class);
        availableModules.put("farming", FarmingQuestModule.class);
        availableModules.put("fishing", FishingQuestModule.class);
        availableModules.put("dealdamage", DealDamageQuestModule.class);
        availableModules.put("enchanting", EnchantingQuestModule.class);
        availableModules.put("mobkilling", MobKillingQuestModule.class);
        availableModules.put("walking", WalkingQuestModule.class);
        availableModules.put("smelting", SmeltingQuestModule.class);
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            availableModules.put("placeholder", PlaceholderQuestModule.class);
        }
    }

    public void loadModules() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        modules.clear();
        int loadedCount = 0;

        for (Map.Entry<String, Class<? extends QuestModule>> entry : availableModules.entrySet()) {
            String moduleName = entry.getKey();
            try {
                if (config.getBoolean("modules." + moduleName, true)) {
                    QuestModule module = entry.getValue().getDeclaredConstructor().newInstance();
                    modules.put(moduleName, module);
                    plugin.getPluginLogger().info("Loaded module: " + moduleName);
                    loadedCount++;
                }
            } catch (Exception e) {
                plugin.getPluginLogger().warning("Failed to load module " + moduleName + ": " + e.getMessage());
                plugin.getMessageUtil().sendMessage(null, "module-error",
                        "%module%", moduleName,
                        "%error%", e.getMessage());
            }
        }

        plugin.getPluginLogger().info("Successfully loaded " + loadedCount + " quest modules");
    }

    public void enableModule(CommandSender sender, String moduleName) {
        if (!availableModules.containsKey(moduleName)) {
            plugin.getMessageUtil().sendMessage(sender, "invalid-module",
                    "%module%", moduleName);
            plugin.getPluginLogger().warning("Attempted to enable invalid module: " + moduleName + " by " + sender.getName());
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (config.getBoolean("modules." + moduleName, true)) {
            plugin.getMessageUtil().sendMessage(sender, "module-already-enabled",
                    "%module%", moduleName);
            return;
        }

        try {
            QuestModule module = availableModules.get(moduleName).getDeclaredConstructor().newInstance();
            modules.put(moduleName, module);
            config.set("modules." + moduleName, true);
            plugin.getConfigManager().saveConfig();
            plugin.getQuestManager().loadQuests(); // Reload quests to reflect new module
            plugin.getMessageUtil().sendMessage(sender, "module-enabled",
                    "%module%", moduleName);
            plugin.getPluginLogger().info("Module " + moduleName + " enabled by " + sender.getName());
        } catch (Exception e) {
            plugin.getMessageUtil().sendMessage(sender, "module-error",
                    "%module%", moduleName,
                    "%error%", e.getMessage());
            plugin.getPluginLogger().warning("Failed to enable module " + moduleName + ": " + e.getMessage());
        }
    }

    public void disableModule(CommandSender sender, String moduleName) {
        if (!availableModules.containsKey(moduleName)) {
            plugin.getMessageUtil().sendMessage(sender, "invalid-module",
                    "%module%", moduleName);
            plugin.getPluginLogger().warning("Attempted to disable invalid module: " + moduleName + " by " + sender.getName());
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (!config.getBoolean("modules." + moduleName, true)) {
            plugin.getMessageUtil().sendMessage(sender, "module-already-disabled",
                    "%module%", moduleName);
            return;
        }

        modules.remove(moduleName);
        config.set("modules." + moduleName, false);
        plugin.getConfigManager().saveConfig();

        // Fail active quests associated with the disabled module
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            Quest activeQuest = plugin.getQuestManager().getActiveQuest(player.getUniqueId());
            if (activeQuest != null && activeQuest.getType().equalsIgnoreCase(moduleName)) {
                plugin.getQuestManager().failQuest(player);
                plugin.getMessageUtil().sendMessage(player, "quest-failed-module-disabled",
                        "%module%", moduleName,
                        "%quest_name%", activeQuest.getName());
            }
        });

        plugin.getQuestManager().loadQuests(); // Reload quests to reflect disabled module
        plugin.getMessageUtil().sendMessage(sender, "module-disabled",
                "%module%", moduleName);
        plugin.getPluginLogger().info("Module " + moduleName + " disabled by " + sender.getName());
    }

    public Map<String, QuestModule> getModules() {
        return modules;
    }

    public String getModuleList() {
        return String.join(", ", availableModules.keySet());
    }
}
