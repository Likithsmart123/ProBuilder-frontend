package com.example.probuilder;

public class Material {
    private final int id;
    private final String name;
    private final int currentStock;
    private final int minStock;
    private final String unit;

    public Material(int id, String name, int currentStock, int minStock, String unit) {
        this.id = id;
        this.name = name;
        this.currentStock = currentStock;
        this.minStock = minStock;
        this.unit = unit;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public int getMinStock() {
        return minStock;
    }

    public String getUnit() {
        return unit;
    }

    public String getStatus() {
        if (currentStock <= minStock * 0.5) { // 50% or less
            return "Critical";
        } else if (currentStock <= minStock) { // Between 50% and 100%
            return "Low";
        } else { // Above min stock
            return "Good";
        }
    }
}
