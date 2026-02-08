package org.kennji.nightfallAutoQuest.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.modules.Quest;

import java.util.Collections;
import java.util.List;

public class GiveUpCommand extends SubCommand {

    public GiveUpCommand(NightfallAutoQuest plugin) {
        super(plugin, "giveup");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().sendMessage(sender, "player-only");
            return true;
        }

        Quest quest = plugin.getQuestManager().getActiveQuest(player.getUniqueId());
        if (quest == null) {
            plugin.getMessageUtil().sendMessage(sender, "no-active-quest");
            return true;
        }

        plugin.getQuestManager().giveUpQuest(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
