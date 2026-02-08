package org.kennji.nightfallAutoQuest.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

import java.util.List;

public abstract class SubCommand {

    protected final NightfallAutoQuest plugin;
    protected final String name;
    protected String permission; // Make permission mutable
    // Aliases will be managed by CommandManager

    public SubCommand(NightfallAutoQuest plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        // Permission will be set by CommandManager
        this.permission = "nightfallautoquest." + name.toLowerCase(); // Default permission
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
