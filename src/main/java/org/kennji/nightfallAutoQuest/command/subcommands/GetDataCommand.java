package org.kennji.nightfallAutoQuest.command.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.command.base.AbstractCommand;
import org.kennji.nightfallAutoQuest.model.PlayerData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class GetDataCommand extends AbstractCommand {
    public GetDataCommand(@NotNull NightfallAutoQuest plugin) {
        super(plugin, "getdata", "naq.admin");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /naq getdata <player>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            plugin.getMessageUtil().sendMessage(sender, "player-not-found", Map.of("%player%", args[1]));
            return;
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(target.getUniqueId());
        
        plugin.getMessageUtil().sendMessage(sender, "getdata.header");
        plugin.getMessageUtil().sendMessage(sender, "getdata.title", Map.of("%player%", target.getName() != null ? target.getName() : "Unknown"));
        plugin.getMessageUtil().sendMessage(sender, "getdata.uuid", Map.of("%uuid%", target.getUniqueId().toString()));
        plugin.getMessageUtil().sendMessage(sender, "getdata.completions", Map.of("%completions%", String.valueOf(data.completions())));
        plugin.getMessageUtil().sendMessage(sender, "getdata.failures", Map.of("%failures%", String.valueOf(data.failures())));
        plugin.getMessageUtil().sendMessage(sender, "getdata.streak", Map.of("%streak%", String.valueOf(data.questStreak())));
        
        String activeQuest = data.activeQuestId() != null ? data.activeQuestId() : plugin.getConfigManager().getMessages().getString("getdata.no-active", "None");
        plugin.getMessageUtil().sendMessage(sender, "getdata.active", Map.of("%active%", activeQuest));
        
        if (data.activeQuestId() != null) {
            plugin.getMessageUtil().sendMessage(sender, "getdata.task", Map.of("%task%", data.activeTask() != null ? data.activeTask() : "Unknown"));
            plugin.getMessageUtil().sendMessage(sender, "getdata.progress", Map.of(
                "%progress%", String.valueOf(data.questProgress()),
                "%amount%", String.valueOf(data.targetAmount())
            ));
            long timeLeft = data.questExpiration() - System.currentTimeMillis();
            plugin.getMessageUtil().sendMessage(sender, "getdata.time", Map.of("%time%", timeLeft > 0 ? (timeLeft / 1000) + "s" : "Expired"));
        }
        plugin.getMessageUtil().sendMessage(sender, "getdata.footer");
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(org.bukkit.entity.Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
