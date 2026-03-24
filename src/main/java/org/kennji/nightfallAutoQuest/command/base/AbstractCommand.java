package org.kennji.nightfallAutoQuest.command.base;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

import java.util.List;

public abstract class AbstractCommand {
    protected final NightfallAutoQuest plugin;
    private final String name;
    private final String permission;
    private String description = "";

    public AbstractCommand(@NotNull NightfallAutoQuest plugin, @NotNull String name, @NotNull String permission) {
        this.plugin = plugin;
        this.name = name;
        this.permission = permission;
    }

    public abstract void execute(@NotNull CommandSender sender, @NotNull String[] args);

    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getPermission() {
        return permission;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    protected boolean hasPermission(@NotNull CommandSender sender) {
        if (permission.isEmpty()) return true;
        return sender.hasPermission(permission);
    }
}
