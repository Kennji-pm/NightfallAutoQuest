package org.kennji.nightfallAutoQuest.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

import java.util.Collections;
import java.util.List;

public class ReloadQuestsCommand extends SubCommand {

    public ReloadQuestsCommand(NightfallAutoQuest plugin) {
        super(plugin, "reloadquests");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.getQuestManager().loadQuests();
        plugin.getMessageUtil().sendMessage(sender, "quests-reloaded",
                "%quest_count%", String.valueOf(plugin.getQuestManager().getQuestCount()),
                "%failed_count%", String.valueOf(plugin.getQuestManager().getFailedQuestCount()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
