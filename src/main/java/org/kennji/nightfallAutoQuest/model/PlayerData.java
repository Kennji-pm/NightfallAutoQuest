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
        return new PlayerData(uuid, completions, failures, activeQuestId, activeTask, newProgress, questExpiration, placeholderStartValue, targetAmount);
    }

    public @NotNull PlayerData withCompletion() {
        return new PlayerData(uuid, completions + 1, failures, null, null, 0, 0, 0, 0);
    }

    public @NotNull PlayerData withFailure() {
        return new PlayerData(uuid, completions, failures + 1, null, null, 0, 0, 0, 0);
    }

    public @NotNull PlayerData withNewQuest(@NotNull String questId, @NotNull String task, long expiration, int startValue, int targetAmount) {
        return new PlayerData(uuid, completions, failures, questId, task, 0, expiration, startValue, targetAmount);
    }

    public @NotNull PlayerData withoutQuest() {
        return new PlayerData(uuid, completions, failures, null, null, 0, 0, 0, 0);
    }
}
