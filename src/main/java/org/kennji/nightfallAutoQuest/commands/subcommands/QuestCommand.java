package org.kennji.nightfallAutoQuest.commands.subcommands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.modules.Quest;
import org.kennji.nightfallAutoQuest.utils.Util;

import java.util.Collections;
import java.util.List;

public class QuestCommand extends SubCommand {

    public QuestCommand(NightfallAutoQuest plugin) {
        super(plugin, "quest");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().sendMessage(sender, "player-only");
            return true;
        }

        Quest quest = plugin.getQuestManager().getActiveQuest(player.getUniqueId());
        if (quest == null) {
            plugin.getMessageUtil().sendMessage(sender, "no-active-quest");
            return true;
        }

        Long expiration = plugin.getQuestManager().getActiveQuestExpiration(player.getUniqueId());
        String timeLeft = expiration != null ? Util.formatTime(expiration - System.currentTimeMillis()) : "Unknown";

        String questType = quest.getType();
        String taskPrefix = plugin.getMessageUtil().getMessage("quests_task." + questType);
        if (taskPrefix == null) {
            taskPrefix = questType; // Fallback if translation not found
        }

        String fullTask = quest.getTask();
        String itemNameRaw = "";
        if (fullTask.contains("_")) {
            itemNameRaw = fullTask.substring(fullTask.indexOf("_") + 1);
        }

        String displayItemName = "";
        if (!itemNameRaw.isEmpty()) {
            try {
                Material material = Material.valueOf(itemNameRaw.toUpperCase());
                displayItemName = Util.capitalizeFully(material.name().replace("_", " "));
            } catch (IllegalArgumentException e) {
                displayItemName = Util.capitalizeFully(itemNameRaw.replace("_", " "));
            }
        }

        String displayTask = taskPrefix;
        if (!displayItemName.isEmpty()) {
            displayTask += " " + displayItemName;
        }

        plugin.getMessageUtil().sendMessage(sender, "quest-info",
                "%quest_name%", quest.getName(),
                "%description%", quest.getDescription(),
                "%task%", displayTask,
                "%progress%", quest.getCurrentProgress() + "/" + quest.getAmount(),
                "%time%", timeLeft);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
