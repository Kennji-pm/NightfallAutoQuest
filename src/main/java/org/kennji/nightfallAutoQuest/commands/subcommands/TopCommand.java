package org.kennji.nightfallAutoQuest.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.database.PlayerData;
import org.kennji.nightfallAutoQuest.utils.ColorUtils;
import org.kennji.nightfallAutoQuest.utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TopCommand extends SubCommand {

    private static final int PLAYERS_PER_PAGE = 10;

    public TopCommand(NightfallAutoQuest plugin) {
        super(plugin, "top");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) {
                    plugin.getMessageUtil().sendMessage(sender, "invalid-page");
                    return true;
                }
            } catch (NumberFormatException e) {
                plugin.getMessageUtil().sendMessage(sender, "invalid-page");
                return true;
            }
        }

        int totalPlayers = plugin.getDatabaseManager().getTotalPlayersWithCompletions();
        int totalPages = (int) Math.ceil(totalPlayers / (double) PLAYERS_PER_PAGE);

        if (page > totalPages && totalPlayers > 0) {
            plugin.getMessageUtil().sendMessage(sender, "page-exceeds",
                    "%current_page%", String.valueOf(page),
                    "%total_pages%", String.valueOf(totalPages));
            return true;
        }

        List<String> topPlayers = plugin.getDatabaseManager().getTopPlayers(page, PLAYERS_PER_PAGE);
        if (topPlayers == null || topPlayers.isEmpty()) {
            plugin.getMessageUtil().sendMessage(sender, "no-data-available");
            return true;
        }

        // Send header
        plugin.getMessageUtil().sendMessageNoPrefix(sender, "top-header",
                "%current_page%", String.valueOf(page),
                "%total_pages%", String.valueOf(totalPages));

        int rank = (page - 1) * PLAYERS_PER_PAGE + 1;
        for (String playerData : topPlayers) {
            String[] data = playerData.split(":");
            if (data.length < 3) {
                plugin.getPluginLogger().warning("Invalid player data format: " + playerData);
                continue;
            }
            try {
                String playerName = data[0];
                Integer completions = Integer.parseInt(data[1]);
                Integer failures = Integer.parseInt(data[2]);
                if (completions == 0) {
                    continue;
                }
                String completionRate = Util.calculateCompletionRate(completions, failures);
                String rankPrefix = switch (rank) {
                    case 1 -> ColorUtils.colorize("&6&l");
                    case 2 -> ColorUtils.colorize("&7&l");
                    case 3 -> ColorUtils.colorize("&c&l");
                    default -> ColorUtils.colorize("&7");
                };
                String ratePrefix = Util.getRateColor(completionRate);
                String message = plugin.getMessageUtil().getMessage("top-player",
                        "%rank%", String.valueOf(rank),
                        "%player%", playerName,
                        "%completions%", String.valueOf(completions),
                        "%completion_rate%", ColorUtils.colorize(ratePrefix) + completionRate);
                sender.sendMessage(rankPrefix + message);
                rank++;
            } catch (NumberFormatException e) {
                plugin.getPluginLogger().warning("Failed to parse player data: " + playerData + " - " + e.getMessage());
            }
        }

        plugin.getMessageUtil().sendMessageNoPrefix(sender, "top-footer",
                "%total_players%", String.valueOf(totalPlayers));

        if (sender instanceof Player player) {
            // Use cache for player's own stats (leaderboard still uses database for
            // accuracy)
            PlayerData playerData = plugin.getPlayerCacheManager().getPlayerData(player.getUniqueId());
            int playerCompletions = playerData.completions;
            if (playerCompletions > 0) {
                int playerRank = plugin.getDatabaseManager().getPlayerRank(player.getUniqueId());
                String playerName = player.getName();
                int playerFailures = playerData.failures;
                String playerCompletionRate = Util.calculateCompletionRate(playerCompletions, playerFailures);
                String rankPrefix = switch (playerRank) {
                    case 1 -> ColorUtils.colorize("&6&l");
                    case 2 -> ColorUtils.colorize("&7&l");
                    case 3 -> ColorUtils.colorize("&c&l");
                    default -> ColorUtils.colorize("&7");
                };
                String ratePrefix = Util.getRateColor(playerCompletionRate);
                String message = plugin.getMessageUtil().getMessage("player-rank",
                        "%rank%", playerRank == -1 ? "N/A" : String.valueOf(playerRank),
                        "%player%", playerName,
                        "%completions%", String.valueOf(playerCompletions),
                        "%completion_rate%", ColorUtils.colorize(ratePrefix) + playerCompletionRate);
                sender.sendMessage(rankPrefix + message);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            int totalPlayers = plugin.getDatabaseManager().getTotalPlayersWithCompletions();
            int totalPages = (int) Math.ceil(totalPlayers / (double) PLAYERS_PER_PAGE);
            for (int i = 1; i <= totalPages; i++) {
                completions.add(String.valueOf(i));
            }
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
