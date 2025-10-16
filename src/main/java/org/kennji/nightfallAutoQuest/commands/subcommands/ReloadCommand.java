package org.kennji.nightfallAutoQuest.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

import java.util.Collections;
import java.util.List;

public class ReloadCommand extends SubCommand {

    public ReloadCommand(NightfallAutoQuest plugin) {
        super(plugin, "reload");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.getConfigManager().reload();
        plugin.getModuleManager().loadModules();
        plugin.getQuestManager().loadQuests();
        plugin.getCommandManager().loadCommandData(); // Reload command aliases
        plugin.getMessageUtil().sendMessage(sender, "reload-success",
                "%quest_count%", String.valueOf(plugin.getQuestManager().getQuestCount()),
                "%failed_count%", String.valueOf(plugin.getQuestManager().getFailedQuestCount()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
