package com.example.probuilder;

public class Supplier {
    private String id;
    private String name;
    private String materialType;
    private String contactNumber;
    private String status; // e.g., "Active", "Inactive"
    private float rating;

    public Supplier(String id, String name, String materialType, String contactNumber, String status, float rating) {
        this.id = id;
        this.name = name;
        this.materialType = materialType;
        this.contactNumber = contactNumber;
        this.status = status;
        this.rating = rating;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMaterialType() { return materialType; }
    public String getContactNumber() { return contactNumber; }
    public String getStatus() { return status; }
    public float getRating() { return rating; }
}
