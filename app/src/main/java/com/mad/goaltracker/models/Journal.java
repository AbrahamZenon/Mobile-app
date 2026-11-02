package com.mad.goaltracker.models;

import java.io.Serializable;

public class Journal implements Serializable {
    private int id;
    private int goalId;
    private String date;               // Ngày ghi nhận
    private String mood;               // Tuyệt vời, Tốt, Bình thường, Cần cố gắng
    private String content;            // Nội dung chi tiết về quá trình thực hiện
    private String strategyAdjustment; // Tự đánh giá & điều chỉnh chiến lược nếu cần
    private String createdAt;

    public Journal() {
    }

    public Journal(int id, int goalId, String date, String mood, String content, String strategyAdjustment, String createdAt) {
        this.id = id;
        this.goalId = goalId;
        this.date = date;
        this.mood = mood;
        this.content = content;
        this.strategyAdjustment = strategyAdjustment;
        this.createdAt = createdAt;
    }

    public Journal(int goalId, String date, String mood, String content, String strategyAdjustment) {
        this.goalId = goalId;
        this.date = date;
        this.mood = mood;
        this.content = content;
        this.strategyAdjustment = strategyAdjustment;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStrategyAdjustment() { return strategyAdjustment; }
    public void setStrategyAdjustment(String strategyAdjustment) { this.strategyAdjustment = strategyAdjustment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
