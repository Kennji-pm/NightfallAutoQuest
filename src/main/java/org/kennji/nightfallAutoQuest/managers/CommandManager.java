package org.kennji.nightfallAutoQuest.managers;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.commands.subcommands.SubCommand;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandManager {

    private final NightfallAutoQuest plugin;
    private final Map<String, SubCommand> subCommands;
    private final Map<String, String> commandAliases; // alias -> actual command name
    private final Map<String, List<String>> subcommandAliases; // command.subcommand -> list of aliases
    private final Map<String, CommandInfo> commandData; // Stores name, permission, aliases from commands.yml

    public CommandManager(NightfallAutoQuest plugin) {
        this.plugin = plugin;
        this.subCommands = new HashMap<>();
        this.commandAliases = new HashMap<>();
        this.subcommandAliases = new HashMap<>();
        this.commandData = new HashMap<>();
        loadCommandData();
    }

    // Inner class to hold command information from commands.yml
    private static class CommandInfo {
        String name;
        String permission;
        List<String> aliases;
        Map<String, List<String>> subCommandAliases; // For commands like 'module'

        public CommandInfo(String name, String permission, List<String> aliases) {
            this.name = name;
            this.permission = permission;
            this.aliases = aliases;
            this.subCommandAliases = new HashMap<>();
        }
    }

    public void loadCommandData() {
        commandData.clear();
        ConfigurationSection commandsSection = plugin.getConfigManager().getCommands().getConfigurationSection("commands");
        if (commandsSection == null) {
            plugin.getPluginLogger().warning("Commands configuration section not found in commands.yml");
            return;
        }

        for (String commandName : commandsSection.getKeys(false)) {
            String permission = commandsSection.getString(commandName + ".permission", "nightfallautoquest." + commandName.toLowerCase());
            List<String> aliases = commandsSection.getStringList(commandName + ".aliases");
            CommandInfo info = new CommandInfo(commandName.toLowerCase(), permission, aliases);

            ConfigurationSection subcommandsSection = commandsSection.getConfigurationSection(commandName + ".subcommands");
            if (subcommandsSection != null) {
                for (String subCommandName : subcommandsSection.getKeys(false)) {
                    List<String> subAliases = subcommandsSection.getStringList(subCommandName + ".aliases");
                    subAliases.add(subCommandName.toLowerCase()); // Add the subcommand itself as an alias
                    info.subCommandAliases.put(subCommandName.toLowerCase(), subAliases);
                }
            }
            commandData.put(commandName.toLowerCase(), info);
        }
    }

    public void registerSubCommands(List<Class<? extends SubCommand>> subCommandClasses) {
        subCommands.clear();
        commandAliases.clear();
        subcommandAliases.clear();

        for (Class<? extends SubCommand> subCommandClass : subCommandClasses) {
            try {
                SubCommand subCommand = subCommandClass.getConstructor(NightfallAutoQuest.class).newInstance(plugin);
                String name = subCommand.getName().toLowerCase();
                CommandInfo info = commandData.get(name);

                if (info != null) {
                    subCommand.setPermission(info.permission);
                    subCommands.put(name, subCommand);

                    // Register main command aliases
                    commandAliases.put(name, name); // The command itself
                    for (String alias : info.aliases) {
                        commandAliases.put(alias.toLowerCase(), name);
                    }

                    // Register subcommand aliases
                    if (!info.subCommandAliases.isEmpty()) {
                        subcommandAliases.putAll(info.subCommandAliases.entrySet().stream()
                                .collect(Collectors.toMap(
                                        entry -> name + "." + entry.getKey(),
                                        Map.Entry::getValue
                                )));
                    }
                } else {
                    plugin.getPluginLogger().warning("Command '" + name + "' not found in commands.yml. Using default permission.");
                    subCommands.put(name, subCommand);
                    commandAliases.put(name, name);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                plugin.getPluginLogger().severe("Failed to register subcommand " + subCommandClass.getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    public boolean executeCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            SubCommand helpCommand = subCommands.get("help");
            if (helpCommand != null) {
                return helpCommand.execute(sender, args);
            }
            plugin.getMessageUtil().sendMessage(sender, "invalid-subcommand");
            return true;
        }

        String commandInput = args[0].toLowerCase();
        String resolvedCommandName = commandAliases.getOrDefault(commandInput, commandInput);

        SubCommand subCommand = subCommands.get(resolvedCommandName);

        if (subCommand == null) {
            plugin.getMessageUtil().sendMessage(sender, "invalid-subcommand");
            return true;
        }

        if (!sender.hasPermission(subCommand.getPermission())) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }

        String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.execute(sender, subCommandArgs);
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new java.util.ArrayList<>();

        if (args.length == 1) {
            completions.addAll(subCommands.keySet().stream()
                    .filter(name -> sender.hasPermission(subCommands.get(name).getPermission()))
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
            completions.addAll(commandAliases.keySet().stream()
                    .filter(alias -> {
                        String actualCommand = commandAliases.get(alias);
                        SubCommand actualSubCommand = subCommands.get(actualCommand);
                        return actualSubCommand != null && sender.hasPermission(actualSubCommand.getPermission());
                    })
                    .filter(alias -> alias.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
        } else if (args.length > 1) {
            String commandInput = args[0].toLowerCase();
            String resolvedCommandName = commandAliases.getOrDefault(commandInput, commandInput);
            SubCommand subCommand = subCommands.get(resolvedCommandName);

            if (subCommand != null && sender.hasPermission(subCommand.getPermission())) {
                String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
                completions.addAll(subCommand.onTabComplete(sender, subCommandArgs));
            }
        }
        return completions;
    }

    public Map<String, List<String>> getSubcommandAliases() {
        return subcommandAliases;
    }

    public Map<String, SubCommand> getSubCommands() {
        return subCommands;
    }
}
