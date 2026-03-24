package org.kennji.nightfallAutoQuest.command.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.command.base.AbstractCommand;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;

public final class QuestCommand extends AbstractCommand {
    public QuestCommand(@NotNull NightfallAutoQuest plugin) {
        super(plugin, "quest", "nightfallautoquest.player");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().sendMessage(sender, "command.player-only");
            return;
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.activeQuestId() == null) {
            plugin.getMessageUtil().sendMessage(player, "quest.no-active");
            return;
        }

        Quest quest = plugin.getQuestManager().getQuest(data.activeQuestId()).orElse(null);
        if (quest == null) {
            plugin.getMessageUtil().sendMessage(player, "quest.invalid");
            return;
        }

        String formattedTask = org.kennji.nightfallAutoQuest.util.StringUtil.formatEnumName(data.activeTask());
        String timeLeft = org.kennji.nightfallAutoQuest.util.StringUtil.formatTime(data.questExpiration() - System.currentTimeMillis());

        plugin.getMessageUtil().sendRawMessage(player, plugin.getConfigManager().getMessages().getString("quest.info.header", ""));
        for (String line : plugin.getConfigManager().getMessages().getStringList("quest.info.format")) {
            if (line.contains("%description%")) {
                for (String descLine : quest.description()) {
                    plugin.getMessageUtil().sendRawMessage(player, descLine
                            .replace("%amount%", String.valueOf(data.targetAmount()))
                            .replace("%task%", formattedTask));
                }
                continue;
            }
            plugin.getMessageUtil().sendRawMessage(player, line
                    .replace("%name%", quest.displayName())
                    .replace("%progress%", String.valueOf(data.questProgress()))
                    .replace("%amount%", String.valueOf(data.targetAmount()))
                    .replace("%task%", formattedTask)
                    .replace("%time%", timeLeft));
        }
        plugin.getMessageUtil().sendRawMessage(player, plugin.getConfigManager().getMessages().getString("quest.info.footer", ""));
    }
}
