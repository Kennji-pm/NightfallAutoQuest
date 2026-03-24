package org.kennji.nightfallAutoQuest.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public final class ColorUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    private ColorUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Parse MiniMessage string to Component
     */
    public static @NotNull Component parse(@NotNull String text) {
        return MINI_MESSAGE.deserialize(text);
    }

    /**
     * Convert MiniMessage string to Legacy colored string (for BossBar/Old API)
     */
    public static @NotNull String colorize(@NotNull String text) {
        if (text.isEmpty())
            return "";
        // Convert legacy & codes to MiniMessage if any (optional, but good for
        // transition)
        String processed = text.replace("&", "§");
        // Note: Paper-api handles MiniMessage better if we keep it as Component
        // But for some Bukkit APIs, we need legacy string.
        return LEGACY_SERIALIZER.serialize(MINI_MESSAGE.deserialize(toMiniMessage(processed)));
    }

    /**
     * Strip all formatting
     */
    public static @NotNull String strip(@NotNull String text) {
        return PLAIN_SERIALIZER.serialize(MINI_MESSAGE.deserialize(text));
    }

    /**
     * Simple helper to convert legacy section symbols to MiniMessage tags
     * Very basic, prefer using MiniMessage directly in config
     */
    private static String toMiniMessage(String text) {
        return text.replace("§0", "<black>")
                .replace("§1", "<dark_blue>")
                .replace("§2", "<dark_green>")
                .replace("§3", "<dark_aqua>")
                .replace("§4", "<dark_red>")
                .replace("§5", "<dark_purple>")
                .replace("§6", "<gold>")
                .replace("§7", "<gray>")
                .replace("§8", "<dark_gray>")
                .replace("§9", "<blue>")
                .replace("§a", "<green>")
                .replace("§b", "<aqua>")
                .replace("§c", "<red>")
                .replace("§d", "<light_purple>")
                .replace("§e", "<yellow>")
                .replace("§f", "<white>")
                .replace("§l", "<bold>")
                .replace("§m", "<strikethrough>")
                .replace("§n", "<underlined>")
                .replace("§o", "<italic>")
                .replace("§r", "<reset>");
    }
}
