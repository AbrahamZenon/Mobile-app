package com.mad.goaltracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mad.goaltracker.models.Badge;
import com.mad.goaltracker.models.Goal;
import com.mad.goaltracker.models.Journal;
import com.mad.goaltracker.models.Reminder;
import com.mad.goaltracker.models.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "goal_tracker.db";
    private static final int DATABASE_VERSION = 3;

    // Pattern kiểm tra định dạng thời gian HH:mm (00:00 -> 23:59)
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01][0-9]|2[0-3]):[0-5][0-9]$");

    // Bảng Goals
    public static final String TABLE_GOALS = "goals";
    public static final String COLUMN_GOAL_ID = "id";
    public static final String COLUMN_GOAL_TITLE = "title";
    public static final String COLUMN_GOAL_DESC = "description";
    public static final String COLUMN_GOAL_CATEGORY = "category";
    public static final String COLUMN_GOAL_TYPE = "type"; // Ngắn hạn / Dài hạn
    public static final String COLUMN_GOAL_DIFFICULTY = "difficulty"; // Dễ / Trung bình / Khó
    public static final String COLUMN_GOAL_DEADLINE = "deadline";
    public static final String COLUMN_GOAL_PROGRESS = "progress";
    public static final String COLUMN_GOAL_IS_COMPLETED = "is_completed";
    public static final String COLUMN_GOAL_CREATED_AT = "created_at";

    // Bảng Tasks
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_TASK_ID = "id";
    public static final String COLUMN_TASK_GOAL_ID = "goal_id";
    public static final String COLUMN_TASK_TITLE = "title";
    public static final String COLUMN_TASK_PRIORITY = "priority"; // Cao / Trung bình / Thấp
    public static final String COLUMN_TASK_PLAN_TYPE = "plan_type"; // Hàng ngày / Hàng tuần
    public static final String COLUMN_TASK_DUE_DATE = "due_date";
    public static final String COLUMN_TASK_IS_COMPLETED = "is_completed";
    public static final String COLUMN_TASK_COMPLETED_AT = "completed_at"; // yyyy-MM-dd HH:mm:ss (Mới tại v2)

    // Bảng Journals
    public static final String TABLE_JOURNALS = "journals";
    public static final String COLUMN_JOURNAL_ID = "id";
    public static final String COLUMN_JOURNAL_GOAL_ID = "goal_id";
    public static final String COLUMN_JOURNAL_DATE = "date";
    public static final String COLUMN_JOURNAL_MOOD = "mood";
    public static final String COLUMN_JOURNAL_CONTENT = "content";
    public static final String COLUMN_JOURNAL_STRATEGY = "strategy_adjustment";
    public static final String COLUMN_JOURNAL_CREATED_AT = "created_at";

    // Bảng Security
    public static final String TABLE_SECURITY = "security";
    public static final String COLUMN_SEC_ID = "id";
    public static final String COLUMN_SEC_PIN = "pin_code";
    public static final String COLUMN_SEC_IS_ENABLED = "is_enabled";

    // Bảng Badges (Mới tại v2)
    public static final String TABLE_BADGES = "badges";
    public static final String COLUMN_BADGE_ID = "id";
    public static final String COLUMN_BADGE_CODE = "code";
    public static final String COLUMN_BADGE_TITLE = "title";
    public static final String COLUMN_BADGE_DESC = "description";
    public static final String COLUMN_BADGE_ICON = "icon_name";
    public static final String COLUMN_BADGE_IS_UNLOCKED = "is_unlocked";
    public static final String COLUMN_BADGE_UNLOCKED_AT = "unlocked_at";

    // Bảng Reminders (Mới tại v2)
    public static final String TABLE_REMINDERS = "reminders";
    public static final String COLUMN_REMINDER_ID = "id";
    public static final String COLUMN_REMINDER_TYPE = "type"; // MORNING, EVENING
    public static final String COLUMN_REMINDER_TIME = "time_string"; // HH:mm
    public static final String COLUMN_REMINDER_IS_ENABLED = "is_enabled";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Tạo bảng Goals
        String createGoalsTable = "CREATE TABLE " + TABLE_GOALS + " (" +
                COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GOAL_TITLE + " TEXT NOT NULL, " +
                COLUMN_GOAL_DESC + " TEXT, " +
                COLUMN_GOAL_CATEGORY + " TEXT, " +
                COLUMN_GOAL_TYPE + " TEXT, " +
                COLUMN_GOAL_DIFFICULTY + " TEXT, " +
                COLUMN_GOAL_DEADLINE + " TEXT, " +
                COLUMN_GOAL_PROGRESS + " INTEGER DEFAULT 0, " +
                COLUMN_GOAL_IS_COMPLETED + " INTEGER DEFAULT 0, " +
                COLUMN_GOAL_CREATED_AT + " TEXT)";
        db.execSQL(createGoalsTable);

        // 2. Tạo bảng Tasks (Đã có cột completed_at)
        String createTasksTable = "CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TASK_GOAL_ID + " INTEGER, " +
                COLUMN_TASK_TITLE + " TEXT NOT NULL, " +
                COLUMN_TASK_PRIORITY + " TEXT, " +
                COLUMN_TASK_PLAN_TYPE + " TEXT, " +
                COLUMN_TASK_DUE_DATE + " TEXT, " +
                COLUMN_TASK_IS_COMPLETED + " INTEGER DEFAULT 0, " +
                COLUMN_TASK_COMPLETED_AT + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_TASK_GOAL_ID + ") REFERENCES " + TABLE_GOALS + "(" + COLUMN_GOAL_ID + ") ON DELETE CASCADE)";
        db.execSQL(createTasksTable);

        // 3. Tạo bảng Journals
        String createJournalsTable = "CREATE TABLE " + TABLE_JOURNALS + " (" +
                COLUMN_JOURNAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_JOURNAL_GOAL_ID + " INTEGER, " +
                COLUMN_JOURNAL_DATE + " TEXT, " +
                COLUMN_JOURNAL_MOOD + " TEXT, " +
                COLUMN_JOURNAL_CONTENT + " TEXT, " +
                COLUMN_JOURNAL_STRATEGY + " TEXT, " +
                COLUMN_JOURNAL_CREATED_AT + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_JOURNAL_GOAL_ID + ") REFERENCES " + TABLE_GOALS + "(" + COLUMN_GOAL_ID + ") ON DELETE CASCADE)";
        db.execSQL(createJournalsTable);

        // 4. Tạo bảng Security
        String createSecurityTable = "CREATE TABLE " + TABLE_SECURITY + " (" +
                COLUMN_SEC_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_SEC_PIN + " TEXT, " +
                COLUMN_SEC_IS_ENABLED + " INTEGER DEFAULT 0)";
        db.execSQL(createSecurityTable);

        // Khởi tạo bản ghi mặc định cho Security
        ContentValues secValues = new ContentValues();
        secValues.put(COLUMN_SEC_ID, 1);
        secValues.put(COLUMN_SEC_PIN, "");
        secValues.put(COLUMN_SEC_IS_ENABLED, 0);
        db.insert(TABLE_SECURITY, null, secValues);

        // 5. Tạo bảng Badges (v2)
        String createBadgesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_BADGES + " (" +
                COLUMN_BADGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BADGE_CODE + " TEXT UNIQUE NOT NULL, " +
                COLUMN_BADGE_TITLE + " TEXT NOT NULL, " +
                COLUMN_BADGE_DESC + " TEXT NOT NULL, " +
                COLUMN_BADGE_ICON + " TEXT NOT NULL, " +
                COLUMN_BADGE_IS_UNLOCKED + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_BADGE_UNLOCKED_AT + " TEXT)";
        db.execSQL(createBadgesTable);

        // 6. Tạo bảng Reminders (v2)
        String createRemindersTable = "CREATE TABLE IF NOT EXISTS " + TABLE_REMINDERS + " (" +
                COLUMN_REMINDER_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_REMINDER_TYPE + " TEXT NOT NULL, " +
                COLUMN_REMINDER_TIME + " TEXT NOT NULL, " +
                COLUMN_REMINDER_IS_ENABLED + " INTEGER NOT NULL DEFAULT 1)";
        db.execSQL(createRemindersTable);

        // 7. Tạo unique index cho reminders.type (v3)
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_reminders_type_unique ON reminders(type);");

        // Seed dữ liệu mặc định cho Badges và Reminders
        seedDefaultBadges(db);
        seedDefaultReminders(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Nâng cấp an toàn từ Version 1 lên Version 2
            // 1. Thêm cột completed_at vào bảng tasks nếu chưa tồn tại
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COLUMN_TASK_COMPLETED_AT + " TEXT;");

            // 2. Tạo bảng Badges mới
            String createBadgesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_BADGES + " (" +
                    COLUMN_BADGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_BADGE_CODE + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_BADGE_TITLE + " TEXT NOT NULL, " +
                    COLUMN_BADGE_DESC + " TEXT NOT NULL, " +
                    COLUMN_BADGE_ICON + " TEXT NOT NULL, " +
                    COLUMN_BADGE_IS_UNLOCKED + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_BADGE_UNLOCKED_AT + " TEXT)";
            db.execSQL(createBadgesTable);

            // 3. Tạo bảng Reminders mới
            String createRemindersTable = "CREATE TABLE IF NOT EXISTS " + TABLE_REMINDERS + " (" +
                    COLUMN_REMINDER_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_REMINDER_TYPE + " TEXT NOT NULL, " +
                    COLUMN_REMINDER_TIME + " TEXT NOT NULL, " +
                    COLUMN_REMINDER_IS_ENABLED + " INTEGER NOT NULL DEFAULT 1)";
            db.execSQL(createRemindersTable);

            // 4. Seed dữ liệu mặc định cho Badges và Reminders
            seedDefaultBadges(db);
            seedDefaultReminders(db);
        }

        if (oldVersion < 3) {
            // Kiểm tra bản ghi trùng type trong reminders trước khi tạo unique index
            Cursor dupCheck = db.rawQuery(
                    "SELECT type, COUNT(*) as cnt FROM reminders GROUP BY type HAVING COUNT(*) > 1",
                    null
            );
            boolean hasDuplicate = (dupCheck != null && dupCheck.moveToFirst());
            if (dupCheck != null) dupCheck.close();

            if (hasDuplicate) {
                // Không tự xóa dữ liệu người dùng — throw để caller biết
                throw new android.database.SQLException(
                        "Migration v2→v3 thất bại: Tồn tại bản ghi reminders trùng type. " +
                        "Cần xử lý thủ công trước khi tạo unique index."
                );
            }

            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_reminders_type_unique ON reminders(type);");
        }
    }

    /**
     * Khởi tạo sẵn 8 huy hiệu thành tích chuẩn vào bảng badges nếu chưa tồn tại
     */
    private void seedDefaultBadges(SQLiteDatabase db) {
        String[][] defaultBadges = {
                {"FIRST_GOAL", "Người Khởi Đầu", "Tạo mục tiêu đầu tiên", "ic_badge_start"},
                {"FIRST_TASK", "Hành Động Nhanh", "Hoàn thành nhiệm vụ đầu tiên", "ic_badge_task"},
                {"FIRST_JOURNAL", "Nhà Văn Kiên Định", "Viết nhật ký đầu tiên", "ic_badge_journal"},
                {"STREAK_3", "Kỷ Luật 3 Ngày", "Đạt chuỗi hoạt động 3 ngày liên tiếp", "ic_badge_streak3"},
                {"STREAK_7", "Kỷ Luật Thép", "Đạt chuỗi hoạt động 7 ngày liên tiếp", "ic_badge_streak7"},
                {"STREAK_30", "Chiến Binh 30 Ngày", "Đạt chuỗi hoạt động 30 ngày liên tiếp", "ic_badge_streak30"},
                {"GOAL_COMPLETED", "Người Chinh Phục", "Hoàn thành trọn vẹn một mục tiêu", "ic_badge_conquer"},
                {"PRODUCTIVITY_10", "Bậc Thầy Năng Suất", "Hoàn thành ít nhất 10 nhiệm vụ", "ic_badge_master"}
        };

        for (String[] badge : defaultBadges) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_BADGE_CODE, badge[0]);
            values.put(COLUMN_BADGE_TITLE, badge[1]);
            values.put(COLUMN_BADGE_DESC, badge[2]);
            values.put(COLUMN_BADGE_ICON, badge[3]);
            values.put(COLUMN_BADGE_IS_UNLOCKED, 0);
            db.insertWithOnConflict(TABLE_BADGES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    /**
     * Khởi tạo sẵn 2 lịch nhắc nhở mặc định (Morning & Evening) nếu chưa tồn tại
     */
    private void seedDefaultReminders(SQLiteDatabase db) {
        ContentValues morning = new ContentValues();
        morning.put(COLUMN_REMINDER_ID, 1);
        morning.put(COLUMN_REMINDER_TYPE, "MORNING");
        morning.put(COLUMN_REMINDER_TIME, "08:00");
        morning.put(COLUMN_REMINDER_IS_ENABLED, 1);
        db.insertWithOnConflict(TABLE_REMINDERS, null, morning, SQLiteDatabase.CONFLICT_IGNORE);

        ContentValues evening = new ContentValues();
        evening.put(COLUMN_REMINDER_ID, 2);
        evening.put(COLUMN_REMINDER_TYPE, "EVENING");
        evening.put(COLUMN_REMINDER_TIME, "21:00");
        evening.put(COLUMN_REMINDER_IS_ENABLED, 1);
        db.insertWithOnConflict(TABLE_REMINDERS, null, evening, SQLiteDatabase.CONFLICT_IGNORE);
    }

    // ==========================================
    // CÁC THAO TÁC CHO GOAL (MỤC TIÊU)
    // ==========================================

    public long insertGoal(Goal goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GOAL_TITLE, goal.getTitle());
        values.put(COLUMN_GOAL_DESC, goal.getDescription());
        values.put(COLUMN_GOAL_CATEGORY, goal.getCategory());
        values.put(COLUMN_GOAL_TYPE, goal.getType());
        values.put(COLUMN_GOAL_DIFFICULTY, goal.getDifficulty());
        values.put(COLUMN_GOAL_DEADLINE, goal.getDeadline());
        values.put(COLUMN_GOAL_PROGRESS, goal.getProgress());
        values.put(COLUMN_GOAL_IS_COMPLETED, goal.isCompleted() ? 1 : 0);

        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        values.put(COLUMN_GOAL_CREATED_AT, currentDateTime);

        long id = db.insert(TABLE_GOALS, null, values);
        db.close();
        return id;
    }

    public int updateGoal(Goal goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GOAL_TITLE, goal.getTitle());
        values.put(COLUMN_GOAL_DESC, goal.getDescription());
        values.put(COLUMN_GOAL_CATEGORY, goal.getCategory());
        values.put(COLUMN_GOAL_TYPE, goal.getType());
        values.put(COLUMN_GOAL_DIFFICULTY, goal.getDifficulty());
        values.put(COLUMN_GOAL_DEADLINE, goal.getDeadline());
        values.put(COLUMN_GOAL_PROGRESS, goal.getProgress());
        values.put(COLUMN_GOAL_IS_COMPLETED, goal.isCompleted() ? 1 : 0);

        int rows = db.update(TABLE_GOALS, values, COLUMN_GOAL_ID + " = ?", new String[]{String.valueOf(goal.getId())});
        db.close();
        return rows;
    }

    public void deleteGoal(int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_TASK_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
        db.delete(TABLE_JOURNALS, COLUMN_JOURNAL_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
        db.delete(TABLE_GOALS, COLUMN_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
        db.close();
    }

    public List<Goal> getAllGoals(String categoryFilter) {
        List<Goal> goalList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;
        if (categoryFilter == null || categoryFilter.equals("Tất cả")) {
            cursor = db.query(TABLE_GOALS, null, null, null, null, null, COLUMN_GOAL_ID + " DESC");
        } else {
            cursor = db.query(TABLE_GOALS, null, COLUMN_GOAL_CATEGORY + " = ?",
                    new String[]{categoryFilter}, null, null, COLUMN_GOAL_ID + " DESC");
        }

        if (cursor.moveToFirst()) {
            do {
                Goal goal = new Goal(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_DESC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_DIFFICULTY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_DEADLINE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL_PROGRESS)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL_IS_COMPLETED)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_CREATED_AT))
                );
                goalList.add(goal);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return goalList;
    }

    // ==========================================
    // CÁC THAO TÁC CHO TASK (KẾ HOẠCH HÀNH ĐỘNG)
    // ==========================================

    public long insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_GOAL_ID, task.getGoalId());
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_PRIORITY, task.getPriority());
        values.put(COLUMN_TASK_PLAN_TYPE, task.getPlanType());
        values.put(COLUMN_TASK_DUE_DATE, task.getDueDate());
        values.put(COLUMN_TASK_IS_COMPLETED, task.isCompleted() ? 1 : 0);

        if (task.isCompleted()) {
            if (task.getCompletedAt() != null && !task.getCompletedAt().isEmpty()) {
                values.put(COLUMN_TASK_COMPLETED_AT, task.getCompletedAt());
            } else {
                String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                values.put(COLUMN_TASK_COMPLETED_AT, now);
            }
        } else {
            values.putNull(COLUMN_TASK_COMPLETED_AT);
        }

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();

        // Tự động tính toán lại tiến độ mục tiêu
        recalculateGoalProgress(task.getGoalId());
        return id;
    }

    public boolean updateTaskCompletion(int taskId, boolean isCompleted, int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Đọc trạng thái cũ trước khi quyết định ghi timestamp
        Cursor cursor = db.query(
                TABLE_TASKS,
                new String[]{COLUMN_TASK_IS_COMPLETED, COLUMN_TASK_COMPLETED_AT},
                COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)},
                null, null, null
        );

        if (cursor == null || !cursor.moveToFirst()) {
            if (cursor != null) cursor.close();
            db.close();
            return false; // Task không tồn tại
        }

        int oldIsCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_IS_COMPLETED));
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_IS_COMPLETED, isCompleted ? 1 : 0);

        if (isCompleted) {
            if (oldIsCompleted == 0) {
                // Transition 0→1: Ghi timestamp hiện tại
                String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                values.put(COLUMN_TASK_COMPLETED_AT, now);
            }
            // Nếu oldIsCompleted == 1 (1→1): KHÔNG đưa completed_at vào ContentValues
            // → SQLite giữ nguyên giá trị cũ, kể cả NULL (đúng cho legacy task từ DB v1)
        } else {
            // Uncomplete: completed_at = NULL
            values.putNull(COLUMN_TASK_COMPLETED_AT);
        }

        int rows = db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();

        // Tự động tính lại tiến độ của mục tiêu cha
        recalculateGoalProgress(goalId);

        return rows > 0 && oldIsCompleted == 0 && isCompleted;
    }

    public void deleteTask(int taskId, int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();

        recalculateGoalProgress(goalId);
    }

    public List<Task> getTasksByGoal(int goalId) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, COLUMN_TASK_GOAL_ID + " = ?",
                new String[]{String.valueOf(goalId)}, null, null, COLUMN_TASK_IS_COMPLETED + " ASC, " + COLUMN_TASK_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_GOAL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_PRIORITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_PLAN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DUE_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_IS_COMPLETED)) == 1
                );
                int completedAtIdx = cursor.getColumnIndex(COLUMN_TASK_COMPLETED_AT);
                if (completedAtIdx != -1) {
                    task.setCompletedAt(cursor.getString(completedAtIdx));
                }
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    private void recalculateGoalProgress(int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor totalCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COLUMN_TASK_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
        int total = 0;
        if (totalCursor.moveToFirst()) total = totalCursor.getInt(0);
        totalCursor.close();

        if (total == 0) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_GOAL_PROGRESS, 0);
            cv.put(COLUMN_GOAL_IS_COMPLETED, 0);
            db.update(TABLE_GOALS, cv, COLUMN_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
            db.close();
            return;
        }

        Cursor doneCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COLUMN_TASK_GOAL_ID + " = ? AND " + COLUMN_TASK_IS_COMPLETED + " = 1", new String[]{String.valueOf(goalId)});
        int done = 0;
        if (doneCursor.moveToFirst()) done = doneCursor.getInt(0);
        doneCursor.close();

        int progress = (int) (((float) done / total) * 100);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GOAL_PROGRESS, progress);
        cv.put(COLUMN_GOAL_IS_COMPLETED, progress == 100 ? 1 : 0);
        db.update(TABLE_GOALS, cv, COLUMN_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
        db.close();
    }

    // ==========================================
    // CÁC THAO TÁC CHO JOURNAL (NHẬT KÝ HÀNH TRÌNH)
    // ==========================================

    public long insertJournal(Journal journal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_JOURNAL_GOAL_ID, journal.getGoalId());
        values.put(COLUMN_JOURNAL_DATE, journal.getDate());
        values.put(COLUMN_JOURNAL_MOOD, journal.getMood());
        values.put(COLUMN_JOURNAL_CONTENT, journal.getContent());
        values.put(COLUMN_JOURNAL_STRATEGY, journal.getStrategyAdjustment());

        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        values.put(COLUMN_JOURNAL_CREATED_AT, currentDateTime);

        long id = db.insert(TABLE_JOURNALS, null, values);
        db.close();
        return id;
    }

    public List<Journal> getJournalsByGoal(int goalId) {
        List<Journal> journals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_JOURNALS, null, COLUMN_JOURNAL_GOAL_ID + " = ?",
                new String[]{String.valueOf(goalId)}, null, null, COLUMN_JOURNAL_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Journal j = new Journal(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JOURNAL_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JOURNAL_GOAL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JOURNAL_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JOURNAL_MOOD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JOURNAL_CONTENT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JOURNAL_STRATEGY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JOURNAL_CREATED_AT))
                );
                journals.add(j);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return journals;
    }

    // ==========================================
    // BẢO MẬT MÃ PIN (SECURITY & PRIVACY)
    // ==========================================

    public boolean isSecurityEnabled() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SECURITY, new String[]{COLUMN_SEC_IS_ENABLED},
                COLUMN_SEC_ID + " = 1", null, null, null, null);
        boolean enabled = false;
        if (cursor.moveToFirst()) {
            enabled = cursor.getInt(0) == 1;
        }
        cursor.close();
        db.close();
        return enabled;
    }

    public String getPinCode() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SECURITY, new String[]{COLUMN_SEC_PIN},
                COLUMN_SEC_ID + " = 1", null, null, null, null);
        String pin = "";
        if (cursor.moveToFirst()) {
            pin = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return pin;
    }

    public void setSecurity(String pin, boolean isEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SEC_PIN, pin);
        values.put(COLUMN_SEC_IS_ENABLED, isEnabled ? 1 : 0);
        db.update(TABLE_SECURITY, values, COLUMN_SEC_ID + " = 1", null);
        db.close();
    }

    // ==========================================
    // CÁC THAO TÁC CHO THỐNG KÊ (STATISTICS APIs)
    // ==========================================

    /**
     * Lấy phân bố số lượng mục tiêu theo 5 danh mục chuẩn
     */
    public Map<String, Integer> getGoalCountByCategory() {
        Map<String, Integer> categoryMap = new LinkedHashMap<>();
        categoryMap.put("Sức khỏe", 0);
        categoryMap.put("Công việc", 0);
        categoryMap.put("Học tập", 0);
        categoryMap.put("Tài chính", 0);
        categoryMap.put("Sở thích", 0);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_GOAL_CATEGORY + ", COUNT(*) FROM " + TABLE_GOALS + " GROUP BY " + COLUMN_GOAL_CATEGORY, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String cat = cursor.getString(0);
                int count = cursor.getInt(1);
                if (cat != null && categoryMap.containsKey(cat)) {
                    categoryMap.put(cat, count);
                }
            }
            cursor.close();
        }
        db.close();
        return categoryMap;
    }

    /**
     * Lấy số lượng nhiệm vụ hoàn thành theo từng ngày trong danh sách 7 ngày gần nhất
     */
    public Map<String, Integer> getWeeklyCompletedTaskCount(List<String> last7Days) {
        Map<String, Integer> weeklyMap = new LinkedHashMap<>();
        if (last7Days == null || last7Days.isEmpty()) {
            return weeklyMap;
        }

        // Khởi tạo trước 0 cho toàn bộ 7 ngày để giữ đúng thứ tự
        for (String dateStr : last7Days) {
            weeklyMap.put(dateStr, 0);
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT substr(" + COLUMN_TASK_COMPLETED_AT + ", 1, 10) as c_date, COUNT(*) FROM " + TABLE_TASKS +
                " WHERE " + COLUMN_TASK_IS_COMPLETED + " = 1 AND " + COLUMN_TASK_COMPLETED_AT + " IS NOT NULL GROUP BY c_date", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String cDate = cursor.getString(0);
                int count = cursor.getInt(1);
                if (cDate != null && weeklyMap.containsKey(cDate)) {
                    weeklyMap.put(cDate, count);
                }
            }
            cursor.close();
        }
        db.close();
        return weeklyMap;
    }

    public int getTotalGoalsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_GOALS, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }

    public int getCompletedGoalsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_GOALS + " WHERE " + COLUMN_GOAL_IS_COMPLETED + " = 1", null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }

    public int getOverdueGoalsCount(String currentDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_GOALS +
                " WHERE " + COLUMN_GOAL_IS_COMPLETED + " = 0 AND " + COLUMN_GOAL_DEADLINE + " IS NOT NULL AND " + COLUMN_GOAL_DEADLINE + " != '' AND " + COLUMN_GOAL_DEADLINE + " < ?",
                new String[]{currentDate});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }

    public int getTotalTasksCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }

    public int getCompletedTasksCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COLUMN_TASK_IS_COMPLETED + " = 1", null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }

    public int getTotalJournalsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_JOURNALS, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }

    public float getOverallAverageProgress() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT AVG(" + COLUMN_GOAL_PROGRESS + ") FROM " + TABLE_GOALS, null);
        float avg = 0.0f;
        if (cursor != null) {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                avg = cursor.getFloat(0);
            }
            cursor.close();
        }
        db.close();
        return avg;
    }

    /**
     * Lấy danh sách hợp nhất tất cả các ngày có hoạt động thực tế (Journal hoặc Task hoàn thành)
     * Định dạng yyyy-MM-dd, sắp xếp giảm dần
     */
    public List<String> getAllActivityDates() {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT DISTINCT d FROM (" +
                "  SELECT " + COLUMN_JOURNAL_DATE + " AS d FROM " + TABLE_JOURNALS + " WHERE " + COLUMN_JOURNAL_DATE + " IS NOT NULL AND " + COLUMN_JOURNAL_DATE + " != ''" +
                "  UNION" +
                "  SELECT substr(" + COLUMN_TASK_COMPLETED_AT + ", 1, 10) AS d FROM " + TABLE_TASKS + " WHERE " + COLUMN_TASK_IS_COMPLETED + " = 1 AND " + COLUMN_TASK_COMPLETED_AT + " IS NOT NULL AND " + COLUMN_TASK_COMPLETED_AT + " != ''" +
                ") ORDER BY d DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                dates.add(cursor.getString(0));
            }
            cursor.close();
        }
        db.close();
        return dates;
    }

    // ==========================================
    // CÁC THAO TÁC CHO HUY HIỆU (BADGES APIs)
    // ==========================================

    public List<Badge> getAllBadges() {
        List<Badge> badges = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BADGES, null, null, null, null, null, COLUMN_BADGE_ID + " ASC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Badge b = new Badge(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BADGE_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BADGE_CODE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BADGE_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BADGE_DESC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BADGE_ICON)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BADGE_IS_UNLOCKED)) == 1,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BADGE_UNLOCKED_AT))
                    );
                    badges.add(b);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return badges;
    }

    public Badge getBadgeByCode(String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BADGES, null, COLUMN_BADGE_CODE + " = ?",
                new String[]{code}, null, null, null);
        Badge badge = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                badge = new Badge(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BADGE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BADGE_CODE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BADGE_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BADGE_DESC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BADGE_ICON)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BADGE_IS_UNLOCKED)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BADGE_UNLOCKED_AT))
                );
            }
            cursor.close();
        }
        db.close();
        return badge;
    }

    /**
     * Mở khóa huy hiệu theo code. Chỉ mở khi chưa mở khóa và cập nhật thời điểm mở khóa.
     * Trả về true nếu thực sự mở khóa lần đầu, false nếu đã mở từ trước.
     */
    public boolean unlockBadge(String code, String unlockedAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BADGE_IS_UNLOCKED, 1);
        values.put(COLUMN_BADGE_UNLOCKED_AT, unlockedAt);

        int rows = db.update(TABLE_BADGES, values,
                COLUMN_BADGE_CODE + " = ? AND " + COLUMN_BADGE_IS_UNLOCKED + " = 0",
                new String[]{code});
        db.close();
        return rows > 0;
    }

    public int getUnlockedBadgesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BADGES + " WHERE " + COLUMN_BADGE_IS_UNLOCKED + " = 1", null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }

    // ==========================================
    // CÁC THAO TÁC CHO NHẮC NHỞ (REMINDERS APIs)
    // ==========================================

    public List<Reminder> getAllReminders() {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REMINDERS, null, null, null, null, null, COLUMN_REMINDER_ID + " ASC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Reminder r = new Reminder(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TIME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_IS_ENABLED)) == 1
                    );
                    reminders.add(r);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return reminders;
    }

    public Reminder getReminderById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REMINDERS, null, COLUMN_REMINDER_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Reminder reminder = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                reminder = new Reminder(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_IS_ENABLED)) == 1
                );
            }
            cursor.close();
        }
        db.close();
        return reminder;
    }

    /**
     * Cập nhật thời gian nhắc nhở (Định dạng HH:mm, ví dụ 08:30)
     */
    public boolean updateReminderTime(int id, String timeString) {
        if (timeString == null || !TIME_PATTERN.matcher(timeString).matches()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REMINDER_TIME, timeString);
        int rows = db.update(TABLE_REMINDERS, values, COLUMN_REMINDER_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    /**
     * Bật hoặc tắt trạng thái nhắc nhở
     */
    public boolean setReminderEnabled(int id, boolean isEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REMINDER_IS_ENABLED, isEnabled ? 1 : 0);
        int rows = db.update(TABLE_REMINDERS, values, COLUMN_REMINDER_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }
}
