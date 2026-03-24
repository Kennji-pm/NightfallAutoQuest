package org.kennji.nightfallAutoQuest.service;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.model.Quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class QuestService {
    private final NightfallAutoQuest plugin;
    private final Random random = new Random();

    public QuestService(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void assignRandomQuest(@NotNull Player player) {
        if (!plugin.getConfigManager().isWorldAllowed(player.getWorld().getName())) return;

        List<Quest> available = new ArrayList<>(plugin.getQuestManager().getQuests().values());
        if (available.isEmpty()) return;

        PlayerData existingData = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (existingData.hasActiveQuest()) return;

        Quest quest = available.get(random.nextInt(available.size()));
        String task = quest.tasks().get(random.nextInt(quest.tasks().size()));
        
        int targetAmount = parseRange(quest.amount());
        int timeLimit = parseRange(quest.timeLimitMinutes());
        long expiration = System.currentTimeMillis() + (long) timeLimit * 60 * 1000;
        
        int startValue = 0;
        if (quest.type().equalsIgnoreCase("placeholder") && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                String value = PlaceholderAPI.setPlaceholders(player, task);
                startValue = (int) Double.parseDouble(value);
            } catch (Exception ignored) {}
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        plugin.getPlayerManager().updatePlayerData(player.getUniqueId(), data.withNewQuest(quest.id(), task, expiration, startValue, targetAmount));
        
        plugin.getMessageUtil().sendMessage(player, "quest.assigned", java.util.Map.of(
            "%name%", quest.displayName(),
            "%amount%", String.valueOf(targetAmount),
            "%task%", org.kennji.nightfallAutoQuest.util.StringUtil.formatEnumName(task)
        ));
        plugin.getSoundUtil().playSound(player, "assign");
    }

    public void handleEvent(@NotNull Player player, @NotNull Event event) {
        if (!plugin.getConfigManager().isWorldAllowed(player.getWorld().getName())) return;

        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getPlayerManager().getPlayerData(uuid);
        
        if (!data.hasActiveQuest()) return;
        
        // Check expiration
        if (System.currentTimeMillis() > data.questExpiration()) {
            failQuest(player, false);
            return;
        }

        plugin.getQuestManager().getQuest(data.activeQuestId()).ifPresent(quest -> {
            plugin.getQuestManager().getModule(quest.type()).ifPresent(module -> {
                int increment = module.processEvent(uuid, data, quest, event);
                if (increment > 0) {
                    processProgress(player, quest, data, increment);
                }
            });
        });
    }

    private void processProgress(@NotNull Player player, @NotNull Quest quest, @NotNull PlayerData data, int increment) {
        int newProgress = data.questProgress() + increment;
        UUID uuid = player.getUniqueId();

        if (newProgress >= data.targetAmount()) {
            completeQuest(player, quest, data);
        } else {
            plugin.getPlayerManager().updatePlayerData(uuid, data.withProgress(newProgress));
            plugin.getBossBarManager().update(player, quest, newProgress, data.questExpiration());
        }
    }

    private void completeQuest(@NotNull Player player, @NotNull Quest quest, @NotNull PlayerData data) {
        int oldStreak = data.questStreak();
        PlayerData newData = data.withCompletion();
        plugin.getPlayerManager().updatePlayerData(player.getUniqueId(), newData);
        plugin.getBossBarManager().remove(player);

        double multiplier = plugin.getConfigManager().getStreakMultiplier(newData.questStreak());

        plugin.getMessageUtil().sendMessage(player, "quest.completed", java.util.Map.of(
                "%name%", quest.displayName(),
                "%amount%", String.valueOf(data.targetAmount()),
                "%streak%", String.valueOf(newData.questStreak())
        ));

        if (multiplier > 1.0 && newData.questStreak() > oldStreak) {
            plugin.getMessageUtil().sendMessage(player, "quest.streak.milestone", java.util.Map.of(
                    "%streak%", String.valueOf(newData.questStreak()),
                    "%bonus%", String.valueOf((int) ((multiplier - 1.0) * 100))
            ));
        }

        plugin.getSoundUtil().playSound(player, "complete");

        // Rewards
        for (String reward : quest.rewards()) {
            String finalReward = multiplyReward(reward, multiplier);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalReward.replace("%player%", player.getName()));
        }
    }

    private String multiplyReward(String reward, double multiplier) {
        if (multiplier <= 1.0) return reward;
        String[] parts = reward.split(" ");
        for (int i = 0; i < parts.length; i++) {
            try {
                // Check if it looks like a number and not a material/ID
                if (parts[i].matches("\\d+")) {
                    int val = Integer.parseInt(parts[i]);
                    if (val > 0) {
                        parts[i] = String.valueOf((int) (val * multiplier));
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return String.join(" ", parts);
    }

    public void failQuest(@NotNull Player player, boolean isGiveUp) {
        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getPlayerManager().getPlayerData(uuid);

        if (data.activeQuestId() != null) {
            int oldStreak = data.questStreak();
            boolean reset = isGiveUp ? plugin.getConfigManager().streakResetOnGiveup() : plugin.getConfigManager().streakResetOnFail();
            
            PlayerData newData = reset ? data.withFailure() : data.withFailureStatsOnly();
            plugin.getPlayerManager().updatePlayerData(uuid, newData);
            plugin.getBossBarManager().remove(player);
            
            plugin.getMessageUtil().sendMessage(player, isGiveUp ? "quest.giveup" : "quest.failed", java.util.Map.of(
                "%name%", plugin.getQuestManager().getQuest(data.activeQuestId()).map(Quest::displayName).orElse("Unknown")
            ));
            
            if (reset && oldStreak > 0) {
                plugin.getMessageUtil().sendMessage(player, "quest.streak.lost", java.util.Map.of(
                    "%streak%", String.valueOf(oldStreak)
                ));
            }
            
            plugin.getSoundUtil().playSound(player, "fail");
        }
    }

    private int parseRange(@NotNull String value) {
        if (!value.contains("-")) {
            try {
                return (int) Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        String[] parts = value.split("-");
        if (parts.length < 2) return 0;
        try {
            int min = Integer.parseInt(parts[0].trim());
            int max = Integer.parseInt(parts[1].trim());
            if (min >= max) return min;
            return min + random.nextInt(max - min + 1);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
