package org.kennji.nightfallAutoQuest.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

public class MessageUtil {
    private final NightfallAutoQuest plugin;
    private final YamlConfiguration messages;
    private final String prefix;
    private final boolean isMessagesLoaded;

    public MessageUtil(NightfallAutoQuest plugin) {
        this.plugin = plugin;
        this.messages = (YamlConfiguration) plugin.getConfigManager().getMessages();
        this.isMessagesLoaded = messages != null && !messages.getKeys(false).isEmpty();

        if (!isMessagesLoaded) {
            plugin.getPluginLogger().severe("Messages configuration failed to load properly!");
        }

        this.prefix = ColorUtils.colorize(plugin.getConfigManager().getConfig().getString("prefix"));
    }

    public void sendMessage(CommandSender sender, String key, String... placeholders) {
        if (!isMessagesLoaded) {
            sender.sendMessage(prefix + ChatColor.RED + "Error: Message system not properly initialized!");
            return;
        }

        String message = getMessage(key, placeholders);
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(prefix + message);
        } else {
            sender.sendMessage(prefix + ChatColor.RED + "Error: Message key '" + key + "' not found!");
            plugin.getPluginLogger().warning("Message key not found: " + key);
        }
    }

    public void sendMessageNoPrefix(CommandSender sender, String key, String... placeholders) {
        if (!isMessagesLoaded) {
            sender.sendMessage(prefix + ChatColor.RED + "Error: Message system not properly initialized!");
            return;
        }

        String message = getMessage(key, placeholders);
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(message);
        } else {
            sender.sendMessage(prefix + ChatColor.RED + "Error: Message key '" + key + "' not found!");
            plugin.getPluginLogger().warning("Message key not found: " + key);
        }
    }

    public String getMessage(String key, String... placeholders) {
        if (!isMessagesLoaded) {
            return ChatColor.RED + "Error: Message system not properly initialized!";
        }

        String message = messages.getString(key);
        if (message == null) {
            plugin.getPluginLogger().warning("Message key not found: " + key);
            return null;
        }
        
        // Replace placeholders
        if (placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        } else {
            plugin.getPluginLogger().warning("Invalid placeholder arguments for key: " + key);
        }

        return ColorUtils.colorize(message);
    }
}