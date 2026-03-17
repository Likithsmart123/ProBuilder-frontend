package com.example.probuilder;

public class Material {
    // Strict Model Implementation
    private final int id;
    private final String name;
    private final double quantity; // Renamed from availableQty as per strict Step 3
    private final String unit;
    private double usedQty;

    // Legacy / Alert Support
    private int minStock = 0;

    // UI Selection Support
    private boolean selected = false;

    // Strict Constructor (4 args)
    public Material(int id, String name, double quantity, String unit) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Legacy Constructor (5 args) for Inventory
    public Material(int id, String name, double quantity, int minStock, String unit) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.minStock = minStock;
        this.unit = unit;
    }
    
    // Getter for ID (CRITICAL)
    public int getId() { return id; }
    
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    public double getUsedQty() { return usedQty; }
    public void setUsedQty(double usedQty) { this.usedQty = usedQty; }

    // --- Legacy / Compatibility Methods ---
    public int getCurrentStock() { return (int) quantity; }
    public int getMinStock() { return minStock; }
    
    public String getStatus() {
        if (minStock > 0 && quantity <= minStock * 0.5) { 
            return "Critical";
        } else if (minStock > 0 && quantity <= minStock) {
            return "Low";
        } else {
            return "Good";
        }
    }

    // --- Selection Support ---
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    // Check availability
    public boolean hasStock(double requested) {
        return requested <= quantity;
    }

    // Needed for ArrayAdapter to display the name in Spinner
    @Override
    public String toString() {
        return name;
    }
}
