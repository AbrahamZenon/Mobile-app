package com.mad.goaltracker.gamification;

import java.io.Serializable;
import java.util.Objects;

/**
 * Model chứa kết quả tổng hợp chuỗi ngày hoạt động liên tục (Streak).
 */
public class StreakSummary implements Serializable {

    private int currentStreak;
    private int longestStreak;

    public StreakSummary() {
        this.currentStreak = 0;
        this.longestStreak = 0;
    }

    public StreakSummary(int currentStreak, int longestStreak) {
        this.currentStreak = Math.max(0, currentStreak);
        this.longestStreak = Math.max(0, longestStreak);
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = Math.max(0, currentStreak);
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = Math.max(0, longestStreak);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreakSummary that = (StreakSummary) o;
        return currentStreak == that.currentStreak && longestStreak == that.longestStreak;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentStreak, longestStreak);
    }

    @Override
    public String toString() {
        return "StreakSummary{" +
                "currentStreak=" + currentStreak +
                ", longestStreak=" + longestStreak +
                '}';
    }
}
