package org.kennji.nightfallAutoQuest.util;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

import java.util.Map;

public final class MessageUtil {
    private final NightfallAutoQuest plugin;

    public MessageUtil(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void sendMessage(@NotNull CommandSender sender, @NotNull String key) {
        sendMessage(sender, key, Map.of());
    }

    public void sendMessage(@NotNull CommandSender sender, @NotNull String key,
            @NotNull Map<String, String> placeholders) {
        Object value = plugin.getConfigManager().getMessages().get(key);
        if (value == null) {
            plugin.getPluginLogger().warn("Missing message key: " + key);
            return;
        }

        String prefix = plugin.getConfigManager().getConfig().getString("prefix",
                "<gradient:#5e4fa2:#f7941d>Nightfall</gradient> <gray>»</gray> ");

        if (value instanceof java.util.List<?> list) {
            for (Object line : list) {
                String finalLine = prefix + line.toString();
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    finalLine = finalLine.replace(entry.getKey(), entry.getValue());
                }
                sender.sendMessage(ColorUtil.parse(finalLine));
            }
        } else {
            String finalMessage = prefix + value.toString();
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                finalMessage = finalMessage.replace(entry.getKey(), entry.getValue());
            }
            sender.sendMessage(ColorUtil.parse(finalMessage));
        }
    }

    public @NotNull String translateTask(@NotNull String type) {
        return plugin.getConfigManager().getMessages().getString("tasks." + type.toLowerCase(), type);
    }

    public void sendRawMessage(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(ColorUtil.parse(message));
    }

    public @NotNull String get(@NotNull String key, @NotNull String... replacements) {
        String message = plugin.getConfigManager().getMessages().getString(key, "");
        if (replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }
}
