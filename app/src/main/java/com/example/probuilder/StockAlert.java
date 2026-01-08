package com.example.probuilder;

public class StockAlert {
    private String itemName;
    private int currentStock;
    private int minStock;
    private String status; // "Critical" or "Low"
    private int imageResId;

    public StockAlert(String itemName, int currentStock, int minStock, String status, int imageResId) {
        this.itemName = itemName;
        this.currentStock = currentStock;
        this.minStock = minStock;
        this.status = status;
        this.imageResId = imageResId;
    }

    public String getItemName() { return itemName; }
    public int getCurrentStock() { return currentStock; }
    public int getMinStock() { return minStock; }
    public String getStatus() { return status; }
    public int getImageResId() { return imageResId; }
}
