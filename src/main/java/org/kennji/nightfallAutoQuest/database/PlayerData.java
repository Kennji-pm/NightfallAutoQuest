package org.kennji.nightfallAutoQuest.database;

import java.util.UUID;

public class PlayerData {
    public String playerName;
    public String uuid;
    public int completions;
    public int failures;
    public String completionRate;
    public String activeQuest;
    public int questProgress;
    public long questExpiration;
    public int placeholderStartValue;

    public PlayerData(UUID uuid) {
        this.uuid = uuid.toString();
        this.playerName = null;
        this.completions = 0;
        this.failures = 0;
        this.completionRate = "0.00%";
        this.activeQuest = null;
        this.questProgress = 0;
        this.questExpiration = 0;
        this.placeholderStartValue = 0;
    }

    /**
     * Update the completion rate based on current completions and failures.
     * Call this after modifying completions or failures.
     */
    public void updateCompletionRate() {
        int total = completions + failures;
        if (total == 0) {
            this.completionRate = "0.00%";
        } else {
            double rate = (double) completions / total * 100;
            this.completionRate = String.format("%.2f%%", rate);
        }
    }

    /**
     * Increment completions and auto-update completion rate.
     */
    public void addCompletion() {
        this.completions++;
        updateCompletionRate();
    }

    /**
     * Increment failures and auto-update completion rate.
     */
    public void addFailure() {
        this.failures++;
        updateCompletionRate();
    }
}
