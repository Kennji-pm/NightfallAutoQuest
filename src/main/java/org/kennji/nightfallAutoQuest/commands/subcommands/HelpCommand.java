package org.kennji.nightfallAutoQuest.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

import java.util.Collections;
import java.util.List;

public class HelpCommand extends SubCommand {

    public HelpCommand(NightfallAutoQuest plugin) {
        super(plugin, "help");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        List<String> helpMessages = plugin.getConfigManager().getMessages().getStringList("help");
        if (helpMessages.isEmpty()) {
            plugin.getMessageUtil().sendMessage(sender, "help");
        } else {
            for (String message : helpMessages) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
