package com.mad.goaltracker.models;

import java.io.Serializable;

public class Goal implements Serializable {
    private int id;
    private String title;
    private String description;
    private String category; // Sức khỏe, Công việc, Học tập, Tài chính, Sở thích
    private String type;     // Ngắn hạn (Short-term), Dài hạn (Long-term)
    private String difficulty; // Dễ, Trung bình, Khó
    private String deadline; // YYYY-MM-DD
    private int progress;    // 0 đến 100%
    private boolean isCompleted;
    private String createdAt;

    public Goal() {
    }

    public Goal(int id, String title, String description, String category, String type, 
                String difficulty, String deadline, int progress, boolean isCompleted, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.type = type;
        this.difficulty = difficulty;
        this.deadline = deadline;
        this.progress = progress;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    public Goal(String title, String description, String category, String type, 
                String difficulty, String deadline) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.type = type;
        this.difficulty = difficulty;
        this.deadline = deadline;
        this.progress = 0;
        this.isCompleted = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
