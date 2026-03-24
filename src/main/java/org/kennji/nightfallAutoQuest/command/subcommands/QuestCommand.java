package org.kennji.nightfallAutoQuest.command.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.command.base.AbstractCommand;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.util.StringUtil;

public final class QuestCommand extends AbstractCommand {
    public QuestCommand(@NotNull NightfallAutoQuest plugin) {
        super(plugin, "quest", "naq.use");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().sendMessage(sender, "command.player-only");
            return;
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (!data.hasActiveQuest()) {
            plugin.getMessageUtil().sendMessage(player, "quest.no-active");
            return;
        }

        plugin.getQuestManager().getQuest(data.activeQuestId()).ifPresent(quest -> {
            String formattedTask = plugin.getMessageUtil().translateTask(data.activeTask());

            plugin.getMessageUtil().sendMessage(player, "quest.info.header");
            for (String line : plugin.getConfigManager().getMessages().getStringList("quest.info.format")) {
                if (line.contains("%description%")) {
                    for (String descLine : quest.description()) {
                        plugin.getMessageUtil().sendRawMessage(player, descLine
                                .replace("%amount%", String.valueOf(data.targetAmount()))
                                .replace("%task%", formattedTask));
                    }
                    continue;
                }

                String translated = line
                        .replace("%name%", quest.displayName())
                        .replace("%progress%", String.valueOf(data.questProgress()))
                        .replace("%amount%", String.valueOf(data.targetAmount()))
                        .replace("%task%", formattedTask)
                        .replace("%streak%", String.valueOf(data.questStreak()))
                        .replace("%time%", StringUtil.formatTime(data.questExpiration() - System.currentTimeMillis()));
                plugin.getMessageUtil().sendRawMessage(player, translated);
            }
            plugin.getMessageUtil().sendMessage(player, "quest.info.footer");
        });
    }
}
