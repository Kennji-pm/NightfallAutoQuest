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
        this.playerName = null; // Will be set when loaded or first used
        this.completions = 0;
        this.failures = 0;
        this.completionRate = "0.00%";
        this.activeQuest = null;
        this.questProgress = 0;
        this.questExpiration = 0;
        this.placeholderStartValue = 0;
    }
}
