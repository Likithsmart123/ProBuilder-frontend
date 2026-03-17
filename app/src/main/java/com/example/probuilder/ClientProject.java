package com.example.probuilder;

public class ClientProject {
    public int projectId;
    public String title;
    public String location;
    public String status;
    public int overallProgress;
    public String lastActivityDate;
    public boolean hasPhotos;

    public ClientProject(int projectId, String title, String location, String status, int overallProgress, String lastActivityDate, boolean hasPhotos) {
        this.projectId = projectId;
        this.title = title;
        this.location = location;
        this.status = status;
        this.overallProgress = overallProgress;
        this.lastActivityDate = lastActivityDate;
        this.hasPhotos = hasPhotos;
    }
}
