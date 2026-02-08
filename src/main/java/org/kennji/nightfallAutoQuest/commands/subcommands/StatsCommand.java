package org.kennji.nightfallAutoQuest.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.database.PlayerData;
import org.kennji.nightfallAutoQuest.utils.ColorUtils;
import org.kennji.nightfallAutoQuest.utils.Util;

import java.util.Collections;
import java.util.List;

public class StatsCommand extends SubCommand {

    public StatsCommand(NightfallAutoQuest plugin) {
        super(plugin, "stats");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().sendMessage(sender, "player-only");
            return true;
        }

        // Use cache instead of database for better performance
        PlayerData playerData = plugin.getPlayerCacheManager().getPlayerData(player.getUniqueId());
        int completions = playerData.completions;
        int failures = playerData.failures;
        String completionRate = Util.calculateCompletionRate(completions, failures);
        String ratePrefix = Util.getRateColor(completionRate);
        String message = plugin.getMessageUtil().getMessage("stats",
                "%completions%", String.valueOf(completions),
                "%failures%", String.valueOf(failures),
                "%completion_rate%", ColorUtils.colorize(ratePrefix) + completionRate);
        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
