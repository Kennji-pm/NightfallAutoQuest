package org.kennji.nightfallAutoQuest.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.database.PlayerData;
import org.kennji.nightfallAutoQuest.utils.ColorUtils;
import org.kennji.nightfallAutoQuest.utils.Util;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetDataCommand extends SubCommand {

    public GetDataCommand(NightfallAutoQuest plugin) {
        super(plugin, "getdata");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            plugin.getMessageUtil().sendMessage(sender, "getdata-usage");
            return true;
        }

        String playerName = args[0];

        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();

        PlayerData playerData = plugin.getPlayerCacheManager().getPlayerData(playerUUID);

        if (playerData == null) {
            plugin.getMessageUtil().sendMessage(sender, "player-not-found", "%player%", playerName);
            return true;
        }

        String header = plugin.getMessageUtil().getMessage("getdata-header", "%player%", playerName);
        sender.sendMessage(ColorUtils.colorize("&e" + header));
        sender.sendMessage(ColorUtils.colorize("&eUUID: &a" + playerData.uuid));
        sender.sendMessage(ColorUtils.colorize("&eCompleted Quests: &a" + playerData.completions));
        sender.sendMessage(ColorUtils.colorize("&eFailed Quests: &a" + playerData.failures));
        String ratePrefix = Util.getRateColor(Util.calculateCompletionRate(playerData.completions, playerData.failures));
        sender.sendMessage(ColorUtils.colorize("&eCompletion Rate: " + ratePrefix + Util.calculateCompletionRate(playerData.completions, playerData.failures)));

        if (playerData.activeQuest != null) {
            sender.sendMessage(ColorUtils.colorize("&eActive Quest: &a" + playerData.activeQuest));
            sender.sendMessage(ColorUtils.colorize("&eQuest Progress: &a" + playerData.questProgress));
            String timeLeft = Util.formatTime(playerData.questExpiration - System.currentTimeMillis());
            sender.sendMessage(ColorUtils.colorize("&eQuest Expiration: &a" + timeLeft));
        } else {
            sender.sendMessage(ColorUtils.colorize("&eActive Quest: &7None"));
        }
        sender.sendMessage(ColorUtils.colorize("&e============================="));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Suggest online player names
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
