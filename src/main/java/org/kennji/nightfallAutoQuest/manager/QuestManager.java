package org.kennji.nightfallAutoQuest.manager;

import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.Quest;
import org.kennji.nightfallAutoQuest.modules.QuestModule;
import org.kennji.nightfallAutoQuest.modules.impl.*;

import java.util.*;

public final class QuestManager {
    private final NightfallAutoQuest plugin;
    private final Map<String, Quest> questRegistry = new HashMap<>();
    private final Map<String, QuestModule> moduleRegistry = new HashMap<>();

    public QuestManager(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
        registerAllModules();
    }

    public void reload() {
        moduleRegistry.clear();
        registerAllModules();
    }

    private void registerAllModules() {
        registerIfEnabled(new MiningModule());
        registerIfEnabled(new PlacingModule());
        registerIfEnabled(new MobKillingModule());
        registerIfEnabled(new CraftingModule());
        registerIfEnabled(new FarmingModule());
        registerIfEnabled(new FishingModule());
        registerIfEnabled(new DealDamageModule());
        registerIfEnabled(new EnchantingModule());
        registerIfEnabled(new WalkingModule());
        registerIfEnabled(new SmeltingModule());
        registerIfEnabled(new PlaceholderModule());
    }

    private void registerIfEnabled(@NotNull QuestModule module) {
        if (plugin.getConfigManager().isModuleEnabled(module.getType())) {
            registerModule(module);
        }
    }

    public void registerModule(@NotNull QuestModule module) {
        moduleRegistry.put(module.getType().toLowerCase(), module);
    }

    public @NotNull Optional<QuestModule> getModule(@NotNull String type) {
        return Optional.ofNullable(moduleRegistry.get(type.toLowerCase()));
    }

    public void registerQuest(@NotNull Quest quest) {
        questRegistry.put(quest.id(), quest);
    }

    public void clearQuests() {
        questRegistry.clear();
    }

    public @NotNull Optional<Quest> getQuest(@NotNull String id) {
        return Optional.ofNullable(questRegistry.get(id));
    }

    public @NotNull Map<String, Quest> getQuests() {
        return Collections.unmodifiableMap(questRegistry);
    }

    public int getQuestCount() {
        return questRegistry.size();
    }
}
