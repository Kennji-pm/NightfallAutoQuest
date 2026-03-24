package org.kennji.nightfallAutoQuest.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kennji.nightfallAutoQuest.util.StringUtil;
import java.util.UUID;

/**
 * Represents player quest data
 */
public record PlayerData(
    @NotNull UUID uuid,
    int completions,
    int failures,
    int questStreak,
    @Nullable String activeQuestId,
    @Nullable String activeTask,
    int questProgress,
    long questExpiration,
    int placeholderStartValue,
    int targetAmount
) {
    public @NotNull String completionRate() {
        return StringUtil.calculateCompletionRate(completions, failures);
    }

    public boolean hasActiveQuest() {
        return activeQuestId != null && System.currentTimeMillis() < questExpiration;
    }

    public @NotNull PlayerData withProgress(int newProgress) {
        return new PlayerData(uuid, completions, failures, questStreak, activeQuestId, activeTask, newProgress, questExpiration, placeholderStartValue, targetAmount);
    }

    public @NotNull PlayerData withCompletion() {
        return new PlayerData(uuid, completions + 1, failures, questStreak + 1, null, null, 0, 0, 0, 0);
    }

    public @NotNull PlayerData withFailure() {
        return new PlayerData(uuid, completions, failures + 1, 0, null, null, 0, 0, 0, 0);
    }
    
    public @NotNull PlayerData withStreakReset() {
        return new PlayerData(uuid, completions, failures, 0, activeQuestId, activeTask, questProgress, questExpiration, placeholderStartValue, targetAmount);
    }

    public @NotNull PlayerData withNewQuest(@NotNull String questId, @NotNull String task, long expiration, int startValue, int targetAmount) {
        return new PlayerData(uuid, completions, failures, questStreak, questId, task, 0, expiration, startValue, targetAmount);
    }

    public @NotNull PlayerData withFailureStatsOnly() {
        return new PlayerData(uuid, completions, failures + 1, questStreak, null, null, 0, 0, 0, 0);
    }

    public @NotNull PlayerData withoutQuest() {
        return new PlayerData(uuid, completions, failures, questStreak, null, null, 0, 0, 0, 0);
    }
}
