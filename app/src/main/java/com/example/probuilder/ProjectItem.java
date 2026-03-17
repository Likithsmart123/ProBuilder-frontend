package com.example.probuilder;

public class ProjectItem {
    public int id;
    public String title;

    public ProjectItem() {
    }

    public ProjectItem(int id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
