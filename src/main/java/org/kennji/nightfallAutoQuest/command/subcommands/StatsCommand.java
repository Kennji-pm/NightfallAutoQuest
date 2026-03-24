package org.kennji.nightfallAutoQuest.command.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.command.base.AbstractCommand;
import org.kennji.nightfallAutoQuest.model.PlayerData;

public final class StatsCommand extends AbstractCommand {
    public StatsCommand(@NotNull NightfallAutoQuest plugin) {
        super(plugin, "stats", "nightfallautoquest.player");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().sendMessage(sender, "command.player-only");
            return;
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        
        plugin.getMessageUtil().sendRawMessage(sender, plugin.getConfigManager().getMessages().getString("stats.header", ""));
        
        for (String line : plugin.getConfigManager().getMessages().getStringList("stats.format")) {
            plugin.getMessageUtil().sendRawMessage(sender, line
                    .replace("%completed%", String.valueOf(data.completions()))
                    .replace("%failed%", String.valueOf(data.failures()))
                    .replace("%rate%", data.completionRate()));
        }

        plugin.getMessageUtil().sendRawMessage(sender, plugin.getConfigManager().getMessages().getString("stats.footer", ""));
    }
}
