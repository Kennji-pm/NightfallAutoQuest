package org.kennji.nightfallAutoQuest.command.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.command.base.AbstractCommand;

public final class GiveUpCommand extends AbstractCommand {
    public GiveUpCommand(@NotNull NightfallAutoQuest plugin) {
        super(plugin, "giveup", "naq.use");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().sendMessage(sender, "player-only");
            return;
        }

        plugin.getQuestService().failQuest(player, true);
    }
}
