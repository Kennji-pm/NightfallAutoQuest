package org.kennji.nightfallAutoQuest.manager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.command.base.AbstractCommand;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public final class CommandManager implements CommandExecutor, TabCompleter {
    private final NightfallAutoQuest plugin;
    private final Map<String, AbstractCommand> subCommands = new HashMap<>();
    private final Map<String, String> aliasMap = new HashMap<>();
    private YamlConfiguration config;

    public Map<String, AbstractCommand> getRawSubCommands() {
        return Collections.unmodifiableMap(subCommands);
    }

    public CommandManager(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File file = new File(plugin.getDataFolder(), "commands.yml");
        if (!file.exists()) {
            plugin.saveResource("commands.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void register(@NotNull AbstractCommand command) {
        String name = command.getName().toLowerCase();
        subCommands.put(name, command);
        
        // Load from config
        String path = "commands." + name;
        if (config.contains(path)) {
            command.setDescription(config.getString(path + ".description", ""));
            // Permission could also be overridden here if needed
        }

        List<String> aliases = config.getStringList(path + ".aliases");
        for (String alias : aliases) {
            aliasMap.put(alias.toLowerCase(), name);
        }
    }

    private AbstractCommand getSubCommand(String label) {
        String name = label.toLowerCase();
        if (subCommands.containsKey(name)) return subCommands.get(name);
        String realName = aliasMap.get(name);
        return realName != null ? subCommands.get(realName) : null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            AbstractCommand help = subCommands.get("help");
            if (help != null) help.execute(sender, args);
            return true;
        }

        AbstractCommand subCommand = getSubCommand(args[0]);
        if (subCommand == null) {
            plugin.getMessageUtil().sendMessage(sender, "command.unknown");
            return true;
        }

        if (!subCommand.getPermission().isEmpty() && !sender.hasPermission(subCommand.getPermission())) {
            plugin.getMessageUtil().sendMessage(sender, "command.no-permission");
            return true;
        }

        subCommand.execute(sender, args);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(subCommands.keySet());
            suggestions.addAll(aliasMap.keySet());
            
            return suggestions.stream()
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .filter(name -> {
                        AbstractCommand cmd = getSubCommand(name);
                        return cmd != null && (cmd.getPermission().isEmpty() || sender.hasPermission(cmd.getPermission()));
                    })
                    .collect(Collectors.toList());
        }

        if (args.length > 1) {
            AbstractCommand subCommand = getSubCommand(args[0]);
            if (subCommand != null) {
                return subCommand.tabComplete(sender, args);
            }
        }

        return List.of();
    }
}
