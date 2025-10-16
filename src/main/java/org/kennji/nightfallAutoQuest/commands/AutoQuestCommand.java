package org.kennji.nightfallAutoQuest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.commands.subcommands.*;

import java.util.Arrays;
import java.util.List;

public class AutoQuestCommand implements CommandExecutor, TabCompleter {
    private final NightfallAutoQuest plugin;

    public AutoQuestCommand(NightfallAutoQuest plugin) {
        this.plugin = plugin;
        // Register subcommands
        plugin.getCommandManager().registerSubCommands(Arrays.asList(
                HelpCommand.class,
                StatsCommand.class,
                QuestCommand.class,
                TopCommand.class,
                GiveUpCommand.class,
                ModuleCommand.class,
                ReloadCommand.class,
                ReloadQuestsCommand.class,
                PurgeCommand.class,
                GetDataCommand.class
        ));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return plugin.getCommandManager().executeCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return plugin.getCommandManager().onTabComplete(sender, args);
    }
}
