package org.kennji.nightfallAutoQuest.utils;

public class Util {

    public static String calculateCompletionRate(Integer completions, Integer failures) {
        int comp = (completions != null && completions >= 0) ? completions : 0;
        int fail = (failures != null && failures >= 0) ? failures : 0;
        double rate = (comp + fail > 0) ? (comp * 100.0 / (comp + fail)) : 0.0;
        return String.format("%.2f%%", rate);
    }

    public static String getRateColor(String completionRate) {
        double rateValue = Double.parseDouble(completionRate.replace("%", ""));
        if (rateValue == 0.0) {
            return "&7";
        } else if (rateValue > 50.0) {
            return "&a";
        } else {
            return "&c";
        }
    }

    public static String formatTime(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String capitalizeFully(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String[] words = text.split(" ");
        StringBuilder capitalizedText = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            capitalizedText.append(Character.toUpperCase(word.charAt(0)))
                           .append(word.substring(1).toLowerCase())
                           .append(" ");
        }
        return capitalizedText.toString().trim();
    }

    public static boolean isWorldAllowed(String worldName, java.util.List<String> allowedWorlds) {
        return allowedWorlds.contains(worldName);
    }
}
