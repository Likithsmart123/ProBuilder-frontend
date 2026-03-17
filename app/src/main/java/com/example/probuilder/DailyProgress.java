package com.example.probuilder;

import java.util.List;

public class DailyProgress {
    private int id;
    private String workDate;
    private String summary;
    private int progressUpdate;
    private List<String> images;

    public DailyProgress(int id, String workDate, String summary,
                         int progressUpdate, List<String> images) {
        this.id = id;
        this.workDate = workDate;
        this.summary = summary;
        this.progressUpdate = progressUpdate;
        this.images = images;
    }

    public int getId() { return id; }
    public String getWorkDate() { return workDate; }
    public String getSummary() { return summary; }
    public int getProgressUpdate() { return progressUpdate; }
    public List<String> getImages() { return images; }
}
