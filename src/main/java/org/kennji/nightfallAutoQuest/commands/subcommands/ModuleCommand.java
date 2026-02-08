package org.kennji.nightfallAutoQuest.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModuleCommand extends SubCommand {

    public ModuleCommand(NightfallAutoQuest plugin) {
        super(plugin, "module");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            plugin.getMessageUtil().sendMessage(sender, "module-usage");
            return true;
        }

        String subcommandInput = args[0].toLowerCase();
        Map<String, List<String>> subcommandAliases = plugin.getCommandManager().getSubcommandAliases();

        String resolvedSubcommand = subcommandAliases.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("module.") && entry.getValue().contains(subcommandInput))
                .map(entry -> entry.getKey().split("\\.")[1])
                .findFirst()
                .orElse(subcommandInput);

        switch (resolvedSubcommand) {
            case "list":
                List<String> modules = plugin.getModuleManager().getModules().keySet().stream()
                        .map(name -> {
                            boolean enabled = plugin.getConfigManager().getConfig().getBoolean("modules." + name, true);
                            String color = enabled ? "&a" : "&c";
                            return color + "- " + name;
                        })
                        .collect(Collectors.toList());
                plugin.getMessageUtil().sendMessage(sender, "module-list",
                        "%modules%", String.join("\n", modules));
                break;
            case "enable":
                if (args.length < 2) {
                    plugin.getMessageUtil().sendMessage(sender, "module-usage");
                    return true;
                }
                String moduleEnable = args[1].toLowerCase();
                plugin.getModuleManager().enableModule(sender, moduleEnable);
                break;
            case "disable":
                if (args.length < 2) {
                    plugin.getMessageUtil().sendMessage(sender, "module-usage");
                    return true;
                }
                String moduleDisable = args[1].toLowerCase();
                plugin.getModuleManager().disableModule(sender, moduleDisable);
                break;
            default:
                plugin.getMessageUtil().sendMessage(sender, "module-usage");
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(Arrays.asList("list", "enable", "disable"));
            plugin.getCommandManager().getSubcommandAliases().forEach((key, aliases) -> {
                if (key.startsWith("module.")) {
                    subcommands.addAll(aliases);
                }
            });
            completions.addAll(subcommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList());
        } else if (args.length == 2) {
            String subcommandInput = args[0].toLowerCase();
            Map<String, List<String>> subcommandAliases = plugin.getCommandManager().getSubcommandAliases();
            String resolvedSubcommand = subcommandAliases.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("module.") && entry.getValue().contains(subcommandInput))
                    .map(entry -> entry.getKey().split("\\.")[1])
                    .findFirst()
                    .orElse(subcommandInput);

            if (resolvedSubcommand.equals("enable") || resolvedSubcommand.equals("disable")) {
                completions.addAll(plugin.getModuleManager().getModules().keySet().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList());
            }
        }
        return completions;
    }
}
