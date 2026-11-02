package com.mad.goaltracker.models;

import java.io.Serializable;

public class Task implements Serializable {
    private int id;
    private int goalId;
    private String title;
    private String priority; // Cao (High), Trung bình (Medium), Thấp (Low)
    private String planType; // Hàng ngày (Daily), Hàng tuần (Weekly)
    private String dueDate;  // YYYY-MM-DD
    private boolean isCompleted;

    public Task() {
    }

    public Task(int id, int goalId, String title, String priority, String planType, String dueDate, boolean isCompleted) {
        this.id = id;
        this.goalId = goalId;
        this.title = title;
        this.priority = priority;
        this.planType = planType;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
    }

    public Task(int goalId, String title, String priority, String planType, String dueDate) {
        this.goalId = goalId;
        this.title = title;
        this.priority = priority;
        this.planType = planType;
        this.dueDate = dueDate;
        this.isCompleted = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
