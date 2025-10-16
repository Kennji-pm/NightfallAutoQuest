package org.kennji.nightfallAutoQuest.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

import java.util.Collections;
import java.util.List;

public class PurgeCommand extends SubCommand {

    public PurgeCommand(NightfallAutoQuest plugin) {
        super(plugin, "purge");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1 || !args[0].equalsIgnoreCase("confirm")) {
            plugin.getMessageUtil().sendMessage(sender, "purge-confirm");
            return true;
        }

        plugin.getDatabaseManager().purgeDatabase();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.getQuestManager().giveUpQuest(player);
        }
        plugin.getMessageUtil().sendMessage(sender, "purge-success");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if ("confirm".startsWith(args[0].toLowerCase())) {
                return Collections.singletonList("confirm");
            }
        }
        return Collections.emptyList();
    }
}
