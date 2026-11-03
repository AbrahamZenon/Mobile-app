package com.mad.goaltracker.notification;

import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Tiện ích tính toán thời điểm kích hoạt nhắc nhở tiếp theo.
 * Tương thích Java 8 / minSdk 24, không phụ thuộc Android UI hay Activity.
 */
public class ReminderTimeCalculator {

    // Regex kiểm tra định dạng thời gian nghiêm ngặt HH:mm (00:00 -> 23:59)
    // Từ chối 24:00, 08:60, 8:00, v.v.
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01][0-9]|2[0-3]):[0-5][0-9]$");

    /**
     * Kiểm tra chuỗi thời gian có hợp lệ theo định dạng chuẩn HH:mm hay không.
     */
    public static boolean isValidTime(String timeString) {
        if (timeString == null) {
            return false;
        }
        return TIME_PATTERN.matcher(timeString.trim()).matches();
    }

    /**
     * Tính thời điểm kích hoạt kế tiếp (tính theo Unix timestamp milliseconds).
     *
     * Quy tắc:
     * - Nếu thời điểm trong ngày chưa qua: kích hoạt vào ngày hôm nay.
     * - Nếu thời điểm trong ngày đã qua hoặc bằng đúng thời điểm hiện tại: kích hoạt vào ngày mai.
     * - Tự động xử lý chuyển ngày, chuyển tháng, chuyển năm và năm nhuận nhờ Calendar.
     *
     * @param timeString Định dạng HH:mm
     * @param now        Mốc thời gian hiện tại
     * @return milliseconds timestamp kích hoạt tiếp theo
     * @throws IllegalArgumentException nếu timeString không hợp lệ hoặc now là null
     */
    public long calculateNextTriggerMillis(String timeString, Calendar now) {
        if (!isValidTime(timeString)) {
            throw new IllegalArgumentException("Định dạng thời gian không hợp lệ (yêu cầu HH:mm từ 00:00 đến 23:59): " + timeString);
        }
        if (now == null) {
            throw new IllegalArgumentException("Calendar 'now' không được để null");
        }

        String[] parts = timeString.trim().split(":");
        int targetHour = Integer.parseInt(parts[0]);
        int targetMinute = Integer.parseInt(parts[1]);

        Calendar target = (Calendar) now.clone();
        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, targetMinute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        // Nếu target nằm ở quá khứ hoặc trùng chính xác thời điểm hiện tại, chuyển sang ngày hôm sau
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        return target.getTimeInMillis();
    }

    /**
     * Tính thời điểm kích hoạt tiếp theo dựa trên mốc thời gian hiện tại của hệ thống.
     */
    public long calculateNextTriggerMillis(String timeString) {
        return calculateNextTriggerMillis(timeString, Calendar.getInstance());
    }
}
