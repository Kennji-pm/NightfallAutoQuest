package org.kennji.nightfallAutoQuest.expansion;

import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.modules.Quest;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.kennji.nightfallAutoQuest.database.PlayerData;
import org.kennji.nightfallAutoQuest.utils.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NightfallAutoQuestExpansion extends PlaceholderExpansion {
    private final NightfallAutoQuest plugin;
    private static final int PLAYERS_PER_PAGE = 10;
    private final Logger logger;

    public NightfallAutoQuestExpansion(NightfallAutoQuest plugin) {
        this.plugin = plugin;
        this.logger = plugin.getPluginLogger();
    }

    @Override
    public String getIdentifier() {
        return "nautoquest";
    }

    @Override
    public String getAuthor() {
        return "_kennji";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null && !identifier.startsWith("top_") && !identifier.equals("total_players") && !identifier.equals("total_pages")) {
            return "";
        }

        PlayerData playerData = null;
        if (player != null) {
            playerData = plugin.getPlayerCacheManager().getPlayerData(player.getUniqueId());
        }

        switch (identifier) {
            case "completed" -> {
                return playerData != null ? String.valueOf(playerData.completions) : "0";
            }
            case "failed" -> {
                return playerData != null ? String.valueOf(playerData.failures) : "0";
            }
            case "completion_rate" -> {
                return playerData != null ? playerData.completionRate : "0.00%";
            }
            case "current" -> {
                return playerData != null && playerData.activeQuest != null ? playerData.activeQuest : "None";
            }
            case "progress" -> {
                if (playerData != null && playerData.activeQuest != null) {
                    Quest quest = plugin.getQuestManager().getActiveQuest(player.getUniqueId());
                    return quest != null ? playerData.questProgress + "/" + quest.getAmount() : "-/-";
                }
                return "-/-";
            }
            case "time_remaining" -> {
                if (playerData != null && playerData.activeQuest != null && playerData.questExpiration > 0) {
                    long timeLeft = playerData.questExpiration - System.currentTimeMillis();
                    if (timeLeft > 0) {
                        return formatTime(timeLeft);
                    }
                }
                return "00:00:00";
            }
            case "total_players" -> {
                return String.valueOf(plugin.getDatabaseManager().getTotalPlayersWithCompletions());
            }
            case "total_pages" -> {
                int totalPlayers = plugin.getDatabaseManager().getTotalPlayersWithCompletions();
                return String.valueOf((int) Math.ceil(totalPlayers / (double) PLAYERS_PER_PAGE));
            }
        }

        if (identifier.startsWith("top_name_")) {
            try {
                int slot = Integer.parseInt(identifier.replace("top_name_", ""));
                int page = (slot - 1) / PLAYERS_PER_PAGE + 1;
                int slotInPage = (slot - 1) % PLAYERS_PER_PAGE + 1;
                List<String> top = plugin.getDatabaseManager().getTopPlayers(page, PLAYERS_PER_PAGE);
                if (top != null && slotInPage <= top.size()) {
                    return top.get(slotInPage - 1).split(":")[0];
                }
                return "";
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Invalid top_name placeholder: " + identifier);
                return "Error";
            }
        }

        if (identifier.startsWith("top_quest_")) {
            try {
                int slot = Integer.parseInt(identifier.replace("top_quest_", ""));
                int page = (slot - 1) / PLAYERS_PER_PAGE + 1;
                int slotInPage = (slot - 1) % PLAYERS_PER_PAGE + 1;
                List<String> top = plugin.getDatabaseManager().getTopPlayers(page, PLAYERS_PER_PAGE);
                if (top != null && slotInPage <= top.size()) {
                    return top.get(slotInPage - 1).split(":")[1];
                }
                return "";
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Invalid top_quest placeholder: " + identifier);
                return "Error";
            }
        }

        if (identifier.startsWith("top_rate_")) {
            try {
                int slot = Integer.parseInt(identifier.replace("top_rate_", ""));
                int page = (slot - 1) / PLAYERS_PER_PAGE + 1;
                int slotInPage = (slot - 1) % PLAYERS_PER_PAGE + 1;
                List<String> top = plugin.getDatabaseManager().getTopPlayers(page, PLAYERS_PER_PAGE);
                if (top != null && slotInPage <= top.size()) {
                    String[] data = top.get(slotInPage - 1).split(":");
                    if (data.length < 3) {
                        logger.log(Level.WARNING, "Invalid player data format in top_rate: " + top.get(slotInPage - 1));
                        return "0.00%";
                    }
                    Integer completions = Integer.parseInt(data[1]);
                    Integer failures = Integer.parseInt(data[2]);
                    return Util.calculateCompletionRate(completions, failures);
                }
                return "0.00%";
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Invalid top_rate placeholder: " + identifier);
                return "Error";
            }
        }

        return null;
    }

    private String formatTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
