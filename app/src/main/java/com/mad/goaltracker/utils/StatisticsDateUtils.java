package com.mad.goaltracker.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Tiện ích xử lý ngày và định dạng số liệu cho màn hình Thống kê.
 * Tương thích minSdk 24 và Java 8 (dùng Calendar, Date, SimpleDateFormat).
 */
public class StatisticsDateUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Tạo danh sách 7 ngày liên tiếp từ (anchor - 6 ngày) đến anchor (hôm nay).
     * Thứ tự tăng dần theo thời gian.
     */
    public static List<String> getLast7Days(Calendar anchor) {
        if (anchor == null) {
            anchor = Calendar.getInstance();
        }

        List<String> last7Days = new ArrayList<>(7);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);

        Calendar cal = (Calendar) anchor.clone();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Lùi về 6 ngày trước
        cal.add(Calendar.DAY_OF_MONTH, -6);

        // Duyệt lần lượt 7 ngày liên tiếp
        for (int i = 0; i < 7; i++) {
            last7Days.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return last7Days;
    }

    /**
     * Tạo danh sách 7 ngày liên tiếp tính đến ngày hôm nay của thiết bị.
     */
    public static List<String> getLast7Days() {
        return getLast7Days(Calendar.getInstance());
    }

    /**
     * Chuyển đổi chuỗi ngày (yyyy-MM-dd) thành nhãn thứ tiếng Việt rút gọn:
     * T2, T3, T4, T5, T6, T7, CN
     */
    public static String getDayOfWeekLabel(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        sdf.setLenient(false);

        try {
            Date date = sdf.parse(dateString.trim());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            switch (dayOfWeek) {
                case Calendar.MONDAY:
                    return "T2";
                case Calendar.TUESDAY:
                    return "T3";
                case Calendar.WEDNESDAY:
                    return "T4";
                case Calendar.THURSDAY:
                    return "T5";
                case Calendar.FRIDAY:
                    return "T6";
                case Calendar.SATURDAY:
                    return "T7";
                case Calendar.SUNDAY:
                    return "CN";
                default:
                    return "";
            }
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Định dạng tiến độ phần trăm với 1 chữ số thập phân (ví dụ: 68.5%).
     * Giới hạn an toàn từ 0.0% đến 100.0%.
     */
    public static String formatProgress(float progress) {
        if (Float.isNaN(progress) || Float.isInfinite(progress)) {
            return "0.0%";
        }
        float clamped = Math.max(0.0f, Math.min(100.0f, progress));
        return String.format(Locale.US, "%.1f%%", clamped);
    }

    /**
     * Lấy chuỗi ngày hôm nay định dạng yyyy-MM-dd theo giờ hệ thống.
     */
    public static String getTodayDateString() {
        return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(new Date());
    }
}
