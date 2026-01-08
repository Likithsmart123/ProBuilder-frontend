package com.example.probuilder;

public class CostItem {
    private String name;
    private double amount;
    private double totalContextAmount; // To calculate percentage

    public CostItem(String name, double amount, double totalContextAmount) {
        this.name = name;
        this.amount = amount;
        this.totalContextAmount = totalContextAmount;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }
    
    public int getPercentage() {
        if (totalContextAmount == 0) return 0;
        return (int) ((amount / totalContextAmount) * 100);
    }
}
