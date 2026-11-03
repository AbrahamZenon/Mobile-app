package com.mad.goaltracker.models;

import java.io.Serializable;

public class Reminder implements Serializable {
    private int id;
    private String type;        // MORNING, EVENING
    private String timeString;  // HH:mm (e.g. "08:00", "21:00")
    private boolean isEnabled;

    public Reminder() {
    }

    public Reminder(int id, String type, String timeString, boolean isEnabled) {
        this.id = id;
        this.type = type;
        this.timeString = timeString;
        this.isEnabled = isEnabled;
    }

    public Reminder(String type, String timeString, boolean isEnabled) {
        this.type = type;
        this.timeString = timeString;
        this.isEnabled = isEnabled;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTimeString() { return timeString; }
    public void setTimeString(String timeString) { this.timeString = timeString; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
}
