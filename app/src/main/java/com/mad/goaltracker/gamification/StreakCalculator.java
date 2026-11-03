package com.mad.goaltracker.gamification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Engine tính toán chuỗi ngày hoạt động liên tục (Streak).
 * Tương thích Android minSdk 24 và Java 8 (dùng Calendar, Date, SimpleDateFormat).
 */
public class StreakCalculator {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    /**
     * Tính toán đầy đủ current streak và longest streak dựa trên ngày hoạt động và ngày neo hôm nay.
     *
     * @param activityDates Danh sách các ngày hoạt động (định dạng yyyy-MM-dd)
     * @param todayDate     Ngày hôm nay để tính toán mốc ranh giới (yyyy-MM-dd)
     * @return StreakSummary chứa currentStreak và longestStreak
     */
    public StreakSummary calculate(List<String> activityDates, String todayDate) {
        String normalizedToday = resolveValidDate(todayDate);
        if (normalizedToday == null) {
            normalizedToday = getTodayString();
        }

        List<String> validSortedDates = sanitizeAndFilterDates(activityDates, normalizedToday);
        if (validSortedDates.isEmpty()) {
            return new StreakSummary(0, 0);
        }

        int longest = computeLongestStreak(validSortedDates);
        int current = computeCurrentStreak(validSortedDates, normalizedToday);

        // Bất biến: longestStreak luôn tối thiểu bằng currentStreak
        if (current > longest) {
            longest = current;
        }

        return new StreakSummary(current, longest);
    }

    /**
     * Tính toán streak với ngày hôm nay lấy theo giờ thiết bị.
     */
    public StreakSummary calculate(List<String> activityDates) {
        return calculate(activityDates, getTodayString());
    }

    /**
     * Lấy current streak.
     */
    public int getCurrentStreak(List<String> activityDates, String todayDate) {
        return calculate(activityDates, todayDate).getCurrentStreak();
    }

    public int getCurrentStreak(List<String> activityDates) {
        return calculate(activityDates, getTodayString()).getCurrentStreak();
    }

    /**
     * Lấy longest streak.
     */
    public int getLongestStreak(List<String> activityDates, String todayDate) {
        return calculate(activityDates, todayDate).getLongestStreak();
    }

    public int getLongestStreak(List<String> activityDates) {
        return calculate(activityDates, getTodayString()).getLongestStreak();
    }

    // =========================================================================
    // THUẬT TOÁN VÀ CÁC HÀM XỬ LÝ NỘI BỘ
    // =========================================================================

    /**
     * Lọc và chuẩn hóa dữ liệu ngày đầu vào:
     * - Loại bỏ null, rỗng, sai định dạng, ngày không hợp lệ (ví dụ 31/02)
     * - Loại bỏ ngày tương lai (lớn hơn normalizedToday)
     * - Loại bỏ ngày trùng lặp
     * - Sắp xếp tăng dần theo thời gian
     */
    private List<String> sanitizeAndFilterDates(List<String> rawDates, String normalizedToday) {
        if (rawDates == null || rawDates.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> uniqueSorted = new TreeSet<>();
        for (String raw : rawDates) {
            String valid = resolveValidDate(raw);
            if (valid != null && valid.compareTo(normalizedToday) <= 0) {
                uniqueSorted.add(valid);
            }
        }
        return new ArrayList<>(uniqueSorted);
    }

    /**
     * Tính chuỗi hoạt động dài nhất trong toàn bộ lịch sử đã sắp xếp.
     */
    private int computeLongestStreak(List<String> sortedDates) {
        if (sortedDates.isEmpty()) {
            return 0;
        }

        int maxRun = 1;
        int currentRun = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            if (isNextDay(sortedDates.get(i - 1), sortedDates.get(i))) {
                currentRun++;
                if (currentRun > maxRun) {
                    maxRun = currentRun;
                }
            } else {
                currentRun = 1;
            }
        }

        return maxRun;
    }

    /**
     * Tính chuỗi hoạt động hiện tại (Current Streak):
     * - Nếu hôm nay có hoạt động: bắt đầu đếm lùi từ hôm nay.
     * - Nếu hôm nay chưa có nhưng hôm qua có: chuỗi vẫn duy trì, đếm lùi từ hôm qua.
     * - Nếu ngày gần nhất cũ hơn hôm qua: chuỗi bằng 0.
     */
    private int computeCurrentStreak(List<String> sortedDates, String normalizedToday) {
        Set<String> dateSet = new HashSet<>(sortedDates);
        String normalizedYesterday = getPreviousDay(normalizedToday);

        String startAnchor = null;
        if (dateSet.contains(normalizedToday)) {
            startAnchor = normalizedToday;
        } else if (normalizedYesterday != null && dateSet.contains(normalizedYesterday)) {
            startAnchor = normalizedYesterday;
        }

        if (startAnchor == null) {
            return 0;
        }

        int streak = 0;
        String cur = startAnchor;
        while (cur != null && dateSet.contains(cur)) {
            streak++;
            cur = getPreviousDay(cur);
        }

        return streak;
    }

    /**
     * Kiểm tra xem dateStrB có phải là ngày liền sau của dateStrA không.
     */
    private boolean isNextDay(String dateStrA, String dateStrB) {
        String expectedNext = getNextDay(dateStrA);
        return expectedNext != null && expectedNext.equals(dateStrB);
    }

    /**
     * Lấy ngày liền sau dưới dạng chuỗi yyyy-MM-dd.
     */
    private String getNextDay(String dateStr) {
        Date d = parseStrictDate(dateStr);
        if (d == null) return null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(cal.getTime());
    }

    /**
     * Lấy ngày liền trước dưới dạng chuỗi yyyy-MM-dd.
     */
    private String getPreviousDay(String dateStr) {
        Date d = parseStrictDate(dateStr);
        if (d == null) return null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(cal.getTime());
    }

    /**
     * Kiểm tra và chuẩn hóa chuỗi ngày strict. Trả về null nếu không hợp lệ.
     */
    private String resolveValidDate(String dateStr) {
        if (dateStr == null) return null;
        String trimmed = dateStr.trim();
        if (!DATE_PATTERN.matcher(trimmed).matches()) {
            return null;
        }
        Date parsed = parseStrictDate(trimmed);
        if (parsed == null) {
            return null;
        }
        // Kiểm tra hai chiều để chặn trường hợp ngày vượt ngưỡng (30/02 -> 02/03)
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        sdf.setLenient(false);
        String formatted = sdf.format(parsed);
        return formatted.equals(trimmed) ? formatted : null;
    }

    private Date parseStrictDate(String dateStr) {
        if (dateStr == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        sdf.setLenient(false);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Lấy ngày hôm nay theo giờ địa phương của thiết bị dưới dạng yyyy-MM-dd.
     */
    public String getTodayString() {
        return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(new Date());
    }
}
