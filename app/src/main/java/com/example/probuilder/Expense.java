package com.example.probuilder;

import java.io.Serializable;

public class Expense implements Serializable {
    private String id;
    private String category;
    private double amount;
    private String date;
    private String projectId;
    private String projectName;
    private String description;
    private String invoiceNumber;
    private String location;

    public Expense(String id, String category, double amount, String date, String projectId, String projectName, String description, String invoiceNumber, String location) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.projectId = projectId;
        this.projectName = projectName;
        this.description = description;
        this.invoiceNumber = invoiceNumber;
        this.location = location;
    }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public String getDescription() { return description; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getLocation() { return location; }
}
