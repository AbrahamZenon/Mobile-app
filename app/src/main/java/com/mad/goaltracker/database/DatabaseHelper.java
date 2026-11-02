package com.mad.goaltracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mad.goaltracker.models.Goal;
import com.mad.goaltracker.models.Journal;
import com.mad.goaltracker.models.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "goal_tracker.db";
    private static final int DATABASE_VERSION = 1;

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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Goals
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

        // Tạo bảng Tasks
        String createTasksTable = "CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TASK_GOAL_ID + " INTEGER, " +
                COLUMN_TASK_TITLE + " TEXT NOT NULL, " +
                COLUMN_TASK_PRIORITY + " TEXT, " +
                COLUMN_TASK_PLAN_TYPE + " TEXT, " +
                COLUMN_TASK_DUE_DATE + " TEXT, " +
                COLUMN_TASK_IS_COMPLETED + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COLUMN_TASK_GOAL_ID + ") REFERENCES " + TABLE_GOALS + "(" + COLUMN_GOAL_ID + ") ON DELETE CASCADE)";
        db.execSQL(createTasksTable);

        // Tạo bảng Journals
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

        // Tạo bảng Security
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECURITY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        onCreate(db);
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
        
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(COLUMN_GOAL_CREATED_AT, currentDate);

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
        // Xóa tasks và journals liên quan trước
        db.delete(TABLE_TASKS, COLUMN_TASK_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
        db.delete(TABLE_JOURNALS, COLUMN_JOURNAL_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
        // Xóa goal
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
    // CÁC THAO TÁC CHO TASK (KẾ HOẠCH HÀNG NGÀY/TUẦN)
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

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();

        // Tự động tính toán lại tiến độ mục tiêu
        recalculateGoalProgress(task.getGoalId());
        return id;
    }

    public void updateTaskCompletion(int taskId, boolean isCompleted, int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_IS_COMPLETED, isCompleted ? 1 : 0);
        db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();

        // Tự động tính lại tiến độ của mục tiêu cha
        recalculateGoalProgress(goalId);
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

        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
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
}
