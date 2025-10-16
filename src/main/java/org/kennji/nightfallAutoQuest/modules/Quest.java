package org.kennji.nightfallAutoQuest.modules;

import java.util.List;

public class Quest {
    private final String name;
    private final String description;
    private final String task;
    private final int amount;
    private final int timeLimit;
    private final List<String> rewards;
    private int currentProgress;

    public Quest(String name, String description, String task, int amount, int timeLimit, List<String> rewards) {
        this.name = name;
        this.description = description;
        this.task = task;
        this.amount = amount;
        this.timeLimit = timeLimit;
        this.rewards = rewards;
        this.currentProgress = 0;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTask() {
        return task;
    }

    public int getAmount() {
        return amount;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void addProgress(int amount) {
        this.currentProgress += amount;
    }

    public void setProgress(int progress) {
        this.currentProgress = progress;
    }

    public boolean isCompleted() {
        return currentProgress >= amount;
    }

    public String getType() {
        return task.split("_")[0];
    }

    public boolean isPresent() {
        return name != null && !name.isEmpty() && amount > 0 && timeLimit > 0;
    }
}