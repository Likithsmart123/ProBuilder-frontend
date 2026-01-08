package com.example.probuilder;

public class PricePrediction {
    private final String materialName;
    private final String unit;
    private final String status;
    private final double currentAvgPrice;
    private final double previousPrice;
    private final double changePercentage;
    private final String recommendation;

    public PricePrediction(String materialName, String unit, String status, double currentAvgPrice, double previousPrice, double changePercentage, String recommendation) {
        this.materialName = materialName;
        this.unit = unit;
        this.status = status;
        this.currentAvgPrice = currentAvgPrice;
        this.previousPrice = previousPrice;
        this.changePercentage = changePercentage;
        this.recommendation = recommendation;
    }

    public String getMaterialName() {
        return materialName;
    }

    public String getUnit() {
        return unit;
    }

    public String getStatus() {
        return status;
    }

    public double getCurrentAvgPrice() {
        return currentAvgPrice;
    }

    public double getPreviousPrice() {
        return previousPrice;
    }

    public double getChangePercentage() {
        return changePercentage;
    }

    public String getRecommendation() {
        return recommendation;
    }
}