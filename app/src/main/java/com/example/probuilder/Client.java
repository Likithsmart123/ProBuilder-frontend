package com.example.probuilder;

public class Client {
    private final int id;
    private final String name;
    private final String email;
    private final String phone;
    private final int isUsed;

    public Client(int id, String name, String email, String phone, int isUsed) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isUsed = isUsed;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public int isUsed() {
        return isUsed;
    }

    // This is a placeholder, as the backend doesn't provide this yet.
    public int getActiveProjects() {
        return 0;
    }
}