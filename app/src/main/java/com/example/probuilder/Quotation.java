package com.example.probuilder;

import java.io.Serializable;

public class Quotation implements Serializable {
    private final String id;
    private final String title;
    private final String clientName;
    private final String projectName;
    private final String amount;
    private final String status;
    private final String createdAt;
    private final String description;
    private final String clientEmail;
    private final String clientPhone;
    private final String projectLocation;
    private final String projectStart;
    private final String projectEnd;

    public Quotation(String id, String title, String clientName, String projectName, String amount, String status, String createdAt, String description,
                     String clientEmail, String clientPhone, String projectLocation, String projectStart, String projectEnd) {
        this.id = id;
        this.title = title;
        this.clientName = clientName;
        this.projectName = projectName;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.description = description;
        this.clientEmail = clientEmail;
        this.clientPhone = clientPhone;
        this.projectLocation = projectLocation;
        this.projectStart = projectStart;
        this.projectEnd = projectEnd;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getClientName() { return clientName; }
    public String getProjectName() { return projectName; }
    public String getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getDescription() { return description; }

    public String getClientEmail() { return clientEmail; }
    public String getClientPhone() { return clientPhone; }
    public String getProjectLocation() { return projectLocation; }
    public String getProjectStart() { return projectStart; }
    public String getProjectEnd() { return projectEnd; }
}