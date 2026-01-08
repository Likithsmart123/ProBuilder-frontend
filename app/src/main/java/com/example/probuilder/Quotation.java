package com.example.probuilder;

public class Quotation {
    private final int id;
    private final String title;
    private final String clientName;
    private final String projectName;
    private final String amount;
    private final String status;
    private final String createdAt;

    public Quotation(int id, String title, String clientName, String projectName, String amount, String status, String createdAt) {
        this.id = id;
        this.title = title;
        this.clientName = clientName;
        this.projectName = projectName;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getClientName() {
        return clientName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}