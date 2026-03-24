package org.kennji.nightfallAutoQuest.expansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import java.util.List;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.util.ColorUtil;
import org.kennji.nightfallAutoQuest.util.StringUtil;

public final class NightfallAutoQuestExpansion extends PlaceholderExpansion {
    private final NightfallAutoQuest plugin;

    public NightfallAutoQuestExpansion(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "nautoquest";
    }

    @Override
    public @NotNull String getAuthor() {
        return "kennji";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null)
            return "";

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        return switch (identifier.toLowerCase()) {
            case "completed" -> String.valueOf(data.completions());
            case "failed" -> String.valueOf(data.failures());
            case "rate" -> data.completionRate();
            case "current_name" ->
                plugin.getQuestManager().getQuest(data.activeQuestId() != null ? data.activeQuestId() : "")
                        .map(quest -> ColorUtil.strip(quest.displayName()))
                        .orElse("None");
            case "progress" -> String.valueOf(data.questProgress());
            case "progress_formatted" -> {
                if (data.activeQuestId() == null)
                    yield "0/0";
                yield plugin.getQuestManager().getQuest(data.activeQuestId())
                        .map(q -> data.questProgress() + "/" + q.amount())
                        .orElse("0/0");
            }
            case "time" -> {
                long timeLeft = data.questExpiration() - System.currentTimeMillis();
                yield timeLeft > 0 ? StringUtil.formatTime(timeLeft) : "00:00:00";
            }
            default -> {
                if (identifier.toLowerCase().startsWith("top_")) {
                    yield handleLeaderboard(identifier.toLowerCase());
                }
                yield null;
            }
        };
    }

    private String handleLeaderboard(String identifier) {
        String[] parts = identifier.split("_");
        if (parts.length < 3)
            return "";

        String type = parts[1];
        int index;
        try {
            index = Integer.parseInt(parts[2]) - 1; // 1-indexed to 0-indexed
        } catch (NumberFormatException e) {
            return "";
        }

        List<PlayerData> top;
        if (identifier.contains("_rate_")) {
            top = plugin.getLeaderboardManager().getTopRate();
        } else {
            top = plugin.getLeaderboardManager().getTopCompletions();
        }
        
        if (index < 0 || index >= top.size())
            return "---";

        PlayerData entry = top.get(index);
        return switch (type) {
            case "name" -> org.bukkit.Bukkit.getOfflinePlayer(entry.uuid()).getName();
            case "completions" -> String.valueOf(entry.completions());
            case "rate" -> entry.completionRate();
            case "value" -> identifier.contains("_rate_") ? entry.completionRate() : String.valueOf(entry.completions());
            default -> "";
        };
    }
}
