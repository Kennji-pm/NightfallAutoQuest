package org.kennji.nightfallAutoQuest.command.subcommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.command.base.AbstractCommand;

public final class HelpCommand extends AbstractCommand {
    public HelpCommand(@NotNull NightfallAutoQuest plugin) {
        super(plugin, "help", "naq.use");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        plugin.getMessageUtil().sendMessage(sender, "command.help.header");

        plugin.getCommandManager().getRawSubCommands().values().forEach(cmd -> {
            if (cmd.getPermission().isEmpty() || sender.hasPermission(cmd.getPermission())) {
                plugin.getMessageUtil().sendRawMessage(sender, " <gradient:#ff9a9e:#fad0c4>»</gradient> <white>/naq "
                        + cmd.getName() + " <gray>- " + cmd.getDescription());
            }
        });

        plugin.getMessageUtil().sendMessage(sender, "command.help.footer");
    }
}
