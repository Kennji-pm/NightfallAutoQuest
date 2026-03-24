package org.kennji.nightfallAutoQuest.command.subcommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.command.base.AbstractCommand;
import org.kennji.nightfallAutoQuest.model.PlayerData;

import java.util.List;

public final class TopCommand extends AbstractCommand {
    public TopCommand(@NotNull NightfallAutoQuest plugin) {
        super(plugin, "top", "naq.use");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        int page = 1;
        if (args.length > 1) {
            try {
                page = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException ignored) {
            }
        }

        int pageSize = 10;
        int totalPlayers = plugin.getDatabaseManager().getTotalPlayers();
        int totalPages = (int) Math.ceil((double) totalPlayers / pageSize);

        plugin.getMessageUtil().sendRawMessage(sender,
                plugin.getConfigManager().getMessages().getString("top.header", "")
                        .replace("%page%", String.valueOf(page))
                        .replace("%total%", String.valueOf(totalPages)));

        List<PlayerData> top = plugin.getDatabaseManager().getTopPlayers(page, pageSize);
        String format = plugin.getConfigManager().getMessages().getString("top.format", "");

        for (int i = 0; i < top.size(); i++) {
            PlayerData data = top.get(i);
            int rank = (page - 1) * pageSize + i + 1;
            String player_name = org.bukkit.Bukkit.getOfflinePlayer(data.uuid()).getName();
            if (player_name == null)
                player_name = "Unknown";

            plugin.getMessageUtil().sendRawMessage(sender, format
                    .replace("%rank%", String.valueOf(rank))
                    .replace("%player%", player_name)
                    .replace("%completions%", String.valueOf(data.completions()))
                    .replace("%rate%", data.completionRate()));
        }

        plugin.getMessageUtil().sendRawMessage(sender,
                plugin.getConfigManager().getMessages().getString("top.footer", "")
                        .replace("%total%", String.valueOf(totalPlayers)));
    }
}
