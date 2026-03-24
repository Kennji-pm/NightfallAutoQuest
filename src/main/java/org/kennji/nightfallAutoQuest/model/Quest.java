package org.kennji.nightfallAutoQuest.model;

import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Represents a quest definition
 */
public record Quest(
    @NotNull String id,
    @NotNull String type,
    @NotNull String displayName,
    @NotNull List<String> description,
    @NotNull List<String> tasks,
    @NotNull String amount,
    @NotNull String timeLimitMinutes,
    @NotNull List<String> rewards
) {}
