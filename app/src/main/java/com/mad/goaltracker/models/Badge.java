package com.mad.goaltracker.models;

import java.io.Serializable;

public class Badge implements Serializable {
    private int id;
    private String code;
    private String title;
    private String description;
    private String iconName;
    private boolean isUnlocked;
    private String unlockedAt;

    public Badge() {
    }

    public Badge(int id, String code, String title, String description, String iconName, boolean isUnlocked, String unlockedAt) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.description = description;
        this.iconName = iconName;
        this.isUnlocked = isUnlocked;
        this.unlockedAt = unlockedAt;
    }

    public Badge(String code, String title, String description, String iconName) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.iconName = iconName;
        this.isUnlocked = false;
        this.unlockedAt = null;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }

    public String getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(String unlockedAt) { this.unlockedAt = unlockedAt; }
}
