package org.kennji.nightfallAutoQuest.command.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.command.base.AbstractCommand;
import org.kennji.nightfallAutoQuest.model.PlayerData;

public final class StatsCommand extends AbstractCommand {
    public StatsCommand(@NotNull NightfallAutoQuest plugin) {
        super(plugin, "stats", "naq.use");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().sendMessage(sender, "command.player-only");
            return;
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        for (String line : plugin.getConfigManager().getMessages().getStringList("stats")) {
            plugin.getMessageUtil().sendRawMessage(sender, line
                    .replace("%completions%", String.valueOf(data.completions()))
                    .replace("%failures%", String.valueOf(data.failures()))
                    .replace("%streak%", String.valueOf(data.questStreak()))
                    .replace("%rate%", data.completionRate()));
        }
    }
}
