package org.kennji.nightfallAutoQuest.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINI_MESSAGE_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("(?i)&[0-9A-FK-OR]");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)&#[0-9A-F]{6}");
    private static final Pattern SECTION_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final PlainTextComponentSerializer plainTextSerializer = PlainTextComponentSerializer.plainText();

    /**
     * Translates color codes, hex colors, and MiniMessage formatting in a string
     * Supports both legacy color codes (&, &#RRGGBB) and MiniMessage format
     *
     * @param text The text to colorize
     * @return The colorized text
     */
    public static String colorize(String text) {
        if (text == null || text.isEmpty()) return "";

        // First handle legacy color codes
        String result = text;

        // Convert legacy hex colors (&#RRGGBB) to MiniMessage format (<#RRGGBB>)
        Matcher hexMatcher = HEX_PATTERN.matcher(result);
        StringBuilder hexBuffer = new StringBuilder();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            hexMatcher.appendReplacement(hexBuffer, "<#" + hex + ">");
        }
        hexMatcher.appendTail(hexBuffer);
        result = hexBuffer.toString();

        // Convert standard color codes to MiniMessage format
        result = ChatColor.translateAlternateColorCodes('&', result);
        result = convertLegacyToMiniMessage(result);

        // If the text contains any MiniMessage formatting, process it
        if (containsMiniMessageFormat(result)) {
            Component component = miniMessage.deserialize(result);
            return legacySerializer.serialize(component);
        }

        return result;
    }

    /**
     * Removes all color codes from a string, returning plain text
     * Supports legacy color codes (&, §), hex colors (&#RRGGBB), and MiniMessage formatting
     *
     * @param text The text to strip colors from
     * @return The text without any color formatting
     */
    public static String stripColor(String text) {
        if (text == null || text.isEmpty()) return "";

        String result = text;

        // First try to handle it as a component if it contains MiniMessage formatting
        if (containsMiniMessageFormat(result) || result.contains("§")) {
            try {
                // Convert to component and then to plain text
                Component component;

                // If it contains MiniMessage format, deserialize it
                if (containsMiniMessageFormat(result)) {
                    // First convert legacy codes to MiniMessage format
                    result = convertLegacyHexToMiniMessage(result);
                    result = ChatColor.translateAlternateColorCodes('&', result);
                    result = convertLegacyToMiniMessage(result);
                    component = miniMessage.deserialize(result);
                } else {
                    // Handle legacy format
                    result = ChatColor.translateAlternateColorCodes('&', result);
                    component = legacySerializer.deserialize(result);
                }

                return plainTextSerializer.serialize(component);
            } catch (Exception e) {
                // If component parsing fails, fall back to regex stripping
            }
        }

        // Fallback: Use regex to strip all known color patterns
        result = stripColorWithRegex(result);

        return result;
    }

    /**
     * Strip colors using regex patterns as fallback method
     *
     * @param text The text to strip
     * @return Text with colors removed
     */
    private static String stripColorWithRegex(String text) {
        String result = text;

        // Remove MiniMessage tags
        result = MINI_MESSAGE_PATTERN.matcher(result).replaceAll("");

        // Remove legacy & color codes
        result = LEGACY_COLOR_PATTERN.matcher(result).replaceAll("");

        // Remove hex color codes
        result = HEX_COLOR_PATTERN.matcher(result).replaceAll("");

        // Remove § color codes
        result = SECTION_COLOR_PATTERN.matcher(result).replaceAll("");

        return result;
    }

    /**
     * Checks if the text contains any MiniMessage formatting
     *
     * @param text The text to check
     * @return true if the text contains MiniMessage formatting
     */
    private static boolean containsMiniMessageFormat(String text) {
        return MINI_MESSAGE_PATTERN.matcher(text).find();
    }

    /**
     * Convert legacy hex colors to MiniMessage format for processing
     *
     * @param text The text to convert
     * @return Text with hex colors converted
     */
    private static String convertLegacyHexToMiniMessage(String text) {
        Matcher hexMatcher = HEX_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            hexMatcher.appendReplacement(buffer, "<#" + hex + ">");
        }
        hexMatcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Converts legacy color codes to MiniMessage format
     *
     * @param text The text to convert
     * @return The converted text
     */
    private static String convertLegacyToMiniMessage(String text) {
        return text
                .replace("§0", "<black>")
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
