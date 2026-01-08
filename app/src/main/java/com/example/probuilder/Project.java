package com.example.probuilder;

public class Project {
    private final int id;
    private final String name;
    private final String location;
    private final String clientName;
    private final String clientPhone;
    private final String startDate;
    private final String endDate;
    private final String status;

    public Project(int id, String name, String location, String clientName, String clientPhone, String startDate, String endDate, String status) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
    }

    // A simple placeholder for progress calculation
    public int getProgress() {
        if ("Completed".equalsIgnoreCase(status)) {
            return 100;
        } else if ("Finishing".equalsIgnoreCase(status)) {
            return 90;
        } else if ("In Progress".equalsIgnoreCase(status)) {
            return 65;
        } else if ("Started".equalsIgnoreCase(status)) {
            return 30;
        }
        return 10; // For "Planning" or other statuses
    }
}
