package org.kennji.nightfallAutoQuest.command.subcommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.command.base.AbstractCommand;

public final class PurgeCommand extends AbstractCommand {
    public PurgeCommand(@NotNull NightfallAutoQuest plugin) {
        super(plugin, "purge", "nightfallautoquest.admin");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            plugin.getMessageUtil().sendRawMessage(sender, "<red>CẢNH BÁO: Thao tác này sẽ xóa sạch dữ liệu! Dùng <yellow>/naq purge confirm <red>để xác nhận.");
            return;
        }

        // Simple implementation for now - could be more robust
        plugin.getDatabaseManager().shutdown();
        java.io.File dbFile = new java.io.File(plugin.getDataFolder(), "database.db");
        if (dbFile.exists()) dbFile.delete();
        
        plugin.getDatabaseManager().initialize();
        plugin.getMessageUtil().sendMessage(sender, "purge-success");
    }
}
