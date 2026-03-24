package org.kennji.nightfallAutoQuest.command.subcommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.command.base.AbstractCommand;
import org.kennji.nightfallAutoQuest.manager.QuestLoader;

public final class ReloadCommand extends AbstractCommand {
    public ReloadCommand(@NotNull NightfallAutoQuest plugin) {
        super(plugin, "reload", "nightfallautoquest.admin");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        // 1. Reload Configuration
        plugin.getConfigManager().reload();

        // 2. Reload Modules (re-enable/disable based on config)
        plugin.getQuestManager().reload();

        // 3. Reload Quests from files
        new QuestLoader(plugin).loadAll();

        // 4. Reload Schedulers (apply new intervals)
        plugin.getQuestScheduler().reload();

        plugin.getMessageUtil().sendMessage(sender, "reload-success");
    }
}
