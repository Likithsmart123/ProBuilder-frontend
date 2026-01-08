package com.example.probuilder;

public class Payment {
    private String clientName;
    private String projectName;
    private double amount;
    private String date;
    private String status;
    private String paymentMethod;

    public Payment(String clientName, String projectName, double amount, String date, String status, String paymentMethod) {
        this.clientName = clientName;
        this.projectName = projectName;
        this.amount = amount;
        this.date = date;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public String getClientName() { return clientName; }
    public String getProjectName() { return projectName; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
}
