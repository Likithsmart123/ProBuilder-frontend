package com.example.probuilder;

// CORRECTED: This is the single, definitive data model for a schedule item.
public class ProjectSchedule {
    private final String projectName;
    private final String clientName;
    private final String status;
    private final int progress;
    private final String plannedStart;
    private final String plannedEnd;
    private final String expectedCompletion;
    private final String delay;
    private final int daysElapsed;
    private final int totalDays;
    private final String progressRate; // CORRECTED: Field name
    private final String analysisMessage; // CORRECTED: Field name

    public ProjectSchedule(String projectName, String clientName, String status, int progress, String plannedStart, String plannedEnd, String expectedCompletion, String delay, int daysElapsed, int totalDays, String progressRate, String analysisMessage) {
        this.projectName = projectName;
        this.clientName = clientName;
        this.status = status;
        this.progress = progress;
        this.plannedStart = plannedStart;
        this.plannedEnd = plannedEnd;
        this.expectedCompletion = expectedCompletion;
        this.delay = delay;
        this.daysElapsed = daysElapsed;
        this.totalDays = totalDays;
        this.progressRate = progressRate;
        this.analysisMessage = analysisMessage;
    }

    // Getters for all fields
    public String getProjectName() {
        return projectName;
    }

    public String getClientName() {
        return clientName;
    }

    public String getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    public String getPlannedStart() {
        return plannedStart;
    }

    public String getPlannedEnd() {
        return plannedEnd;
    }

    public String getExpectedCompletion() {
        return expectedCompletion;
    }

    public String getDelay() {
        return delay;
    }

    public int getDaysElapsed() {
        return daysElapsed;
    }

    public int getTotalDays() {
        return totalDays;
    }

    // CORRECTED: Getter name
    public String getProgressRate() {
        return progressRate;
    }

    // CORRECTED: Getter name
    public String getAnalysisMessage() {
        return analysisMessage;
    }
}
