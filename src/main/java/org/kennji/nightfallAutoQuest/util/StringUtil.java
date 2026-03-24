package org.kennji.nightfallAutoQuest.util;

import org.jetbrains.annotations.NotNull;
import java.util.List;

public final class StringUtil {
    private StringUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static @NotNull String calculateCompletionRate(int completions, int failures) {
        int total = completions + failures;
        double rate = (total > 0) ? (completions * 100.0 / total) : 0.0;
        return String.format("%.2f%%", rate);
    }

    public static @NotNull String formatTime(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static @NotNull String capitalizeFully(@NotNull String text) {
        if (text.isEmpty()) return "";
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            result.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
        }
        return result.toString().trim();
    }

    public static @NotNull String formatEnumName(@NotNull String name) {
        return capitalizeFully(name.replace("_", " "));
    }

    public static boolean isWorldAllowed(@NotNull String worldName, @NotNull List<String> allowedWorlds) {
        return allowedWorlds.isEmpty() || allowedWorlds.contains(worldName);
    }
}
