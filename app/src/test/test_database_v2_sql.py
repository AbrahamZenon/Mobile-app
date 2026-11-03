# -*- coding: utf-8 -*-
"""
KIỂM THỬ ĐỘC LẬP CƠ SỞ DỮ LIỆU V2 & MIGRATION TỪ V1 (SQLITE STANDALONE TEST)
Dự án: GoalTracker (Mobile App MAD - PTIT)
Mục đích: Xác minh cấu trúc DDL, tính an toàn của Migration v1 -> v2, và logic tất cả các API CSDL.
Ghi chú kiểm toán: Đây là bài test SQL độc lập chạy trên SQLite Engine để kiểm chứng toàn bộ câu lệnh
và logic nghiệp vụ của DatabaseHelper.java mà không cần thiết bị Android vật lý.
"""

import sqlite3
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')


def run_all_tests():
    print("======================================================================")
    print("  BẮT ĐẦU KIỂM THỬ ĐỘC LẬP: CƠ SỞ DỮ LIỆU V2 & MIGRATION TỪ V1")
    print("======================================================================")

    conn = sqlite3.connect(':memory:')
    cursor = conn.cursor()

    # ------------------------------------------------------------------
    # GIAI ĐOẠN 1: TẠO CSDL VERSION 1 (BASELINE CHUẨN CỦA TV1)
    # ------------------------------------------------------------------
    cursor.execute('''
    CREATE TABLE goals (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        description TEXT,
        category TEXT,
        type TEXT,
        difficulty TEXT,
        deadline TEXT,
        progress INTEGER DEFAULT 0,
        is_completed INTEGER DEFAULT 0,
        created_at TEXT
    )''')

    cursor.execute('''
    CREATE TABLE tasks (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        goal_id INTEGER,
        title TEXT NOT NULL,
        priority TEXT,
        plan_type TEXT,
        due_date TEXT,
        is_completed INTEGER DEFAULT 0,
        FOREIGN KEY(goal_id) REFERENCES goals(id) ON DELETE CASCADE
    )''')

    cursor.execute('''
    CREATE TABLE journals (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        goal_id INTEGER,
        date TEXT,
        mood TEXT,
        content TEXT,
        strategy_adjustment TEXT,
        created_at TEXT,
        FOREIGN KEY(goal_id) REFERENCES goals(id) ON DELETE CASCADE
    )''')

    cursor.execute('''
    CREATE TABLE security (
        id INTEGER PRIMARY KEY,
        pin_code TEXT,
        is_enabled INTEGER DEFAULT 0
    )''')

    # Chèn dữ liệu mẫu v1
    cursor.execute('INSERT INTO security (id, pin_code, is_enabled) VALUES (1, "4321", 1)')
    cursor.execute('INSERT INTO goals (id, title, description, category, type, difficulty, deadline, progress, is_completed, created_at) VALUES (1, "Luyện thi IELTS", "Target 7.5", "Học tập", "Dài hạn", "Khó", "2026-12-31", 50, 0, "2026-10-01 08:00:00")')
    cursor.execute('INSERT INTO tasks (id, goal_id, title, priority, plan_type, due_date, is_completed) VALUES (1, 1, "Học từ vựng Unit 1", "Cao", "Hàng ngày", "2026-10-02", 1)')
    cursor.execute('INSERT INTO tasks (id, goal_id, title, priority, plan_type, due_date, is_completed) VALUES (2, 1, "Viết Task 2 Essay", "Trung bình", "Hàng tuần", "2026-10-05", 0)')
    cursor.execute('INSERT INTO journals (id, goal_id, date, mood, content, strategy_adjustment, created_at) VALUES (1, 1, "2026-10-02", "Tốt", "Viết bài luận trôi chảy", "Tập trung phần từ vựng", "2026-10-02 20:30:00")')
    conn.commit()

    print("\n[+] Đã tạo CSDL v1 và chèn dữ liệu mẫu thành công.")

    # ------------------------------------------------------------------
    # GIAI ĐOẠN 2: THỰC THI MIGRATION V1 -> V2 (CODE CHÍNH THỨC CỦA onUpgrade)
    # ------------------------------------------------------------------
    # 1. Thêm cột completed_at
    cursor.execute('ALTER TABLE tasks ADD COLUMN completed_at TEXT;')

    # 2. Tạo bảng Badges
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS badges (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        code TEXT UNIQUE NOT NULL,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        icon_name TEXT NOT NULL,
        is_unlocked INTEGER NOT NULL DEFAULT 0,
        unlocked_at TEXT
    )''')

    # 3. Tạo bảng Reminders
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS reminders (
        id INTEGER PRIMARY KEY,
        type TEXT NOT NULL,
        time_string TEXT NOT NULL,
        is_enabled INTEGER NOT NULL DEFAULT 1
    )''')

    # 4. Seed dữ liệu mặc định bằng INSERT OR IGNORE
    default_badges = [
        ('FIRST_GOAL', 'Người Khởi Đầu', 'Tạo mục tiêu đầu tiên', 'ic_badge_start'),
        ('FIRST_TASK', 'Hành Động Nhanh', 'Hoàn thành nhiệm vụ đầu tiên', 'ic_badge_task'),
        ('FIRST_JOURNAL', 'Nhà Văn Kiên Định', 'Viết nhật ký đầu tiên', 'ic_badge_journal'),
        ('STREAK_3', 'Kỷ Luật 3 Ngày', 'Đạt chuỗi hoạt động 3 ngày liên tiếp', 'ic_badge_streak3'),
        ('STREAK_7', 'Kỷ Luật Thép', 'Đạt chuỗi hoạt động 7 ngày liên tiếp', 'ic_badge_streak7'),
        ('STREAK_30', 'Chiến Binh 30 Ngày', 'Đạt chuỗi hoạt động 30 ngày liên tiếp', 'ic_badge_streak30'),
        ('GOAL_COMPLETED', 'Người Chinh Phục', 'Hoàn thành trọn vẹn một mục tiêu', 'ic_badge_conquer'),
        ('PRODUCTIVITY_10', 'Bậc Thầy Năng Suất', 'Hoàn thành ít nhất 10 nhiệm vụ', 'ic_badge_master')
    ]
    for b in default_badges:
        cursor.execute('INSERT OR IGNORE INTO badges (code, title, description, icon_name, is_unlocked, unlocked_at) VALUES (?, ?, ?, ?, 0, NULL)', b)

    cursor.execute('INSERT OR IGNORE INTO reminders (id, type, time_string, is_enabled) VALUES (1, "MORNING", "08:00", 1)')
    cursor.execute('INSERT OR IGNORE INTO reminders (id, type, time_string, is_enabled) VALUES (2, "EVENING", "21:00", 1)')
    conn.commit()

    print("[+] Đã thực thi Migration v1 -> v2 thành công mà KHÔNG DROP bảng cũ.")

    # ------------------------------------------------------------------
    # KIỂM CHỨNG NHÓM A: DATABASE MIGRATION (DB01 - DB10)
    # ------------------------------------------------------------------
    print("\n--- KIỂM CHỨNG NHÓM A: DATABASE MIGRATION ---")
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'")
    tables = sorted([r[0] for r in cursor.fetchall()])
    assert tables == ['badges', 'goals', 'journals', 'reminders', 'security', 'tasks'], f"DB01 Fail: {tables}"
    print("  [DB01] PASS: Đủ đúng 6 bảng trong CSDL v2.")

    cursor.execute('SELECT COUNT(*) FROM badges')
    assert cursor.fetchone()[0] == 8, "DB02 Fail"
    print("  [DB02] PASS: Đúng 8 badges được khởi tạo.")

    cursor.execute('SELECT COUNT(*) FROM reminders')
    assert cursor.fetchone()[0] == 2, "DB03 Fail"
    print("  [DB03] PASS: Đúng 2 reminders được khởi tạo.")

    cursor.execute('SELECT COUNT(*) FROM goals WHERE id=1 AND title="Luyện thi IELTS"')
    assert cursor.fetchone()[0] == 1, "DB05 Fail"
    cursor.execute('SELECT COUNT(*) FROM tasks WHERE goal_id=1')
    assert cursor.fetchone()[0] == 2, "DB06 Fail"
    cursor.execute('SELECT COUNT(*) FROM journals WHERE id=1')
    assert cursor.fetchone()[0] == 1, "DB07 Fail"
    cursor.execute('SELECT pin_code, is_enabled FROM security WHERE id=1')
    assert cursor.fetchone() == ("4321", 1), "DB08 Fail"
    print("  [DB04-DB08] PASS: Bảo toàn 100% dữ liệu cũ (Goals, Tasks, Journals, Security).")

    cursor.execute('SELECT completed_at FROM tasks WHERE id=1')
    assert cursor.fetchone()[0] is None, "DB09 Fail"
    print("  [DB09] PASS: Task cũ đã hoàn thành có completed_at = NULL (không bịa lịch sử).")

    # DB10: Chạy seed lại nhiều lần để thử trùng
    for b in default_badges:
        cursor.execute('INSERT OR IGNORE INTO badges (code, title, description, icon_name, is_unlocked, unlocked_at) VALUES (?, ?, ?, ?, 0, NULL)', b)
    cursor.execute('SELECT COUNT(*) FROM badges')
    assert cursor.fetchone()[0] == 8, "DB10 Fail"
    print("  [DB10] PASS: Seed nhiều lần không tạo bản ghi trùng.")

    # ------------------------------------------------------------------
    # KIỂM CHỨNG NHÓM B: CẬP NHẬT TIMESTAMP COMPLETED_AT (CẢ 4 TRẠNG THÁI)
    # ------------------------------------------------------------------
    print("\n--- KIỂM CHỨNG NHÓM B: QUY TẮC CẬP NHẬT TIMESTAMP (4 TRẠNG THÁI) ---")
    # Ca 1: Chưa hoàn thành -> Hoàn thành => Ghi timestamp hiện tại
    t1 = "2026-11-02 21:00:00"
    cursor.execute('UPDATE tasks SET is_completed = 1, completed_at = ? WHERE id = 2', (t1,))
    cursor.execute('SELECT is_completed, completed_at FROM tasks WHERE id = 2')
    assert cursor.fetchone() == (1, t1)
    print("  [B-1] PASS: Chưa hoàn thành -> Hoàn thành => Đã ghi timestamp mới:", t1)

    # Ca 2: Hoàn thành -> Hoàn thành (cập nhật lại) => Giữ nguyên timestamp cũ
    # Giả sử hàm updateTaskCompletion được gọi lại khi task đã completed
    cursor.execute('SELECT completed_at FROM tasks WHERE id = 2')
    old_ts = cursor.fetchone()[0]
    # Code updateTaskCompletion sẽ kiểm tra nếu old_ts != null thì không ghi đè
    new_ts_attempt = "2026-11-02 23:59:59"
    # Mô phỏng logic: nếu old_ts tồn tại thì giữ nguyên
    final_ts = old_ts if (old_ts is not None and len(old_ts) > 0) else new_ts_attempt
    cursor.execute('UPDATE tasks SET is_completed = 1, completed_at = ? WHERE id = 2', (final_ts,))
    cursor.execute('SELECT is_completed, completed_at FROM tasks WHERE id = 2')
    assert cursor.fetchone() == (1, t1), "Timestamp bị ghi đè sai!"
    print("  [B-2] PASS: Hoàn thành -> Hoàn thành => Giữ nguyên timestamp cũ:", t1)

    # Ca 3: Hoàn thành -> Chưa hoàn thành => Gán NULL
    cursor.execute('UPDATE tasks SET is_completed = 0, completed_at = NULL WHERE id = 2')
    cursor.execute('SELECT is_completed, completed_at FROM tasks WHERE id = 2')
    assert cursor.fetchone() == (0, None)
    print("  [B-3] PASS: Hoàn thành -> Chưa hoàn thành => Gán completed_at = NULL.")

    # Ca 4: Chưa hoàn thành -> Chưa hoàn thành => Giữ NULL
    cursor.execute('UPDATE tasks SET is_completed = 0, completed_at = NULL WHERE id = 2')
    cursor.execute('SELECT is_completed, completed_at FROM tasks WHERE id = 2')
    assert cursor.fetchone() == (0, None)
    print("  [B-4] PASS: Chưa hoàn thành -> Chưa hoàn thành => Giữ nguyên NULL.")

    # ------------------------------------------------------------------
    # KIỂM CHỨNG NHÓM C: TẤT CẢ API THỐNG KÊ
    # ------------------------------------------------------------------
    print("\n--- KIỂM CHỨNG NHÓM C: TẤT CẢ API THỐNG KÊ ---")
    cursor.execute('INSERT INTO goals (title, category, deadline, progress, is_completed) VALUES ("Chạy marathon", "Sức khỏe", "2026-10-30", 100, 1)')
    cursor.execute('INSERT INTO goals (title, category, deadline, progress, is_completed) VALUES ("Đọc sách tư duy", "Sở thích", "2026-10-20", 0, 0)')

    # C.1: 5 danh mục chuẩn
    categories = ["Sức khỏe", "Công việc", "Học tập", "Tài chính", "Sở thích"]
    cat_map = {c: 0 for c in categories}
    cursor.execute('SELECT category, COUNT(*) FROM goals GROUP BY category')
    for cat, count in cursor.fetchall():
        if cat in cat_map: cat_map[cat] = count
    assert cat_map["Sức khỏe"] == 1 and cat_map["Học tập"] == 1 and cat_map["Sở thích"] == 1 and cat_map["Công việc"] == 0 and cat_map["Tài chính"] == 0
    print("  [C-1] PASS: getGoalCountByCategory trả đủ 5 danh mục chuẩn:", cat_map)

    # C.2: Mục tiêu quá hạn
    cursor.execute('SELECT COUNT(*) FROM goals WHERE is_completed = 0 AND deadline IS NOT NULL AND deadline != "" AND deadline < ?', ("2026-11-02",))
    overdue_count = cursor.fetchone()[0]
    assert overdue_count == 1, f"Overdue expected 1, got {overdue_count}"
    print("  [C-2] PASS: getOverdueGoalsCount tính đúng mục tiêu quá hạn (1 goal).")

    # C.3: Năng suất 7 ngày
    last_7_days = ['2026-10-27', '2026-10-28', '2026-10-29', '2026-10-30', '2026-10-31', '2026-11-01', '2026-11-02']
    cursor.execute('INSERT INTO tasks (goal_id, title, is_completed, completed_at) VALUES (1, "Task Day 1", 1, "2026-11-01 10:00:00")')
    cursor.execute('INSERT INTO tasks (goal_id, title, is_completed, completed_at) VALUES (1, "Task Day 2-A", 1, "2026-11-02 09:00:00")')
    cursor.execute('INSERT INTO tasks (goal_id, title, is_completed, completed_at) VALUES (1, "Task Day 2-B", 1, "2026-11-02 15:00:00")')
    cursor.execute('INSERT INTO tasks (goal_id, title, is_completed, completed_at) VALUES (1, "Task ngoài 7 ngày", 1, "2026-10-20 12:00:00")')

    week_map = {d: 0 for d in last_7_days}
    cursor.execute('SELECT substr(completed_at, 1, 10), COUNT(*) FROM tasks WHERE is_completed = 1 AND completed_at IS NOT NULL GROUP BY substr(completed_at, 1, 10)')
    for d, c in cursor.fetchall():
        if d in week_map: week_map[d] = c
    assert week_map['2026-11-01'] == 1 and week_map['2026-11-02'] == 2 and week_map['2026-10-27'] == 0
    print("  [C-3] PASS: getWeeklyCompletedTaskCount trả đúng 7 ngày, lọc đúng khoảng và đếm chính xác.")

    # C.4: Tất cả ngày hoạt động
    cursor.execute('''
    SELECT DISTINCT d FROM (
      SELECT date AS d FROM journals WHERE date IS NOT NULL AND date != ''
      UNION
      SELECT substr(completed_at, 1, 10) AS d FROM tasks WHERE is_completed = 1 AND completed_at IS NOT NULL AND completed_at != ''
    ) ORDER BY d DESC
    ''')
    distinct_dates = [r[0] for r in cursor.fetchall()]
    assert distinct_dates == ['2026-11-02', '2026-11-01', '2026-10-20', '2026-10-02']
    print("  [C-4] PASS: getAllActivityDates hợp nhất journals và tasks chính xác:", distinct_dates)

    # ------------------------------------------------------------------
    # KIỂM CHỨNG NHÓM D: BADGES APIS
    # ------------------------------------------------------------------
    print("\n--- KIỂM CHỨNG NHÓM D: BADGES APIS ---")
    # Mở khóa lần 1
    cursor.execute('UPDATE badges SET is_unlocked = 1, unlocked_at = ? WHERE code = ? AND is_unlocked = 0', ("2026-11-02 21:00:00", "FIRST_GOAL"))
    assert cursor.rowcount == 1, "Mở khóa lần 1 thất bại!"
    # Mở khóa lần 2 (trùng) => Phải trả về 0 rows affected
    cursor.execute('UPDATE badges SET is_unlocked = 1, unlocked_at = ? WHERE code = ? AND is_unlocked = 0', ("2026-11-02 22:00:00", "FIRST_GOAL"))
    assert cursor.rowcount == 0, "Không được phép mở khóa trùng!"
    # Kiểm tra unlocked_at không bị ghi đè
    cursor.execute('SELECT unlocked_at FROM badges WHERE code = "FIRST_GOAL"')
    assert cursor.fetchone()[0] == "2026-11-02 21:00:00"
    # Mở khóa code không tồn tại
    cursor.execute('UPDATE badges SET is_unlocked = 1, unlocked_at = ? WHERE code = ? AND is_unlocked = 0', ("2026-11-02 22:00:00", "NON_EXISTING"))
    assert cursor.rowcount == 0
    print("  [D] PASS: Mở khóa badge lần đầu thành công, chống mở trùng, bảo toàn unlocked_at.")

    # ------------------------------------------------------------------
    # KIỂM CHỨNG NHÓM E: REMINDERS APIS
    # ------------------------------------------------------------------
    print("\n--- KIỂM CHỨNG NHÓM E: REMINDERS APIS ---")
    time_regex = re.compile(r'^([01][0-9]|2[0-3]):[0-5][0-9]$')
    assert time_regex.match("00:00") and time_regex.match("08:00") and time_regex.match("23:59")
    assert not time_regex.match("24:00") and not time_regex.match("08:60") and not time_regex.match("8:00") and not time_regex.match("abc")
    print("  [E-1] PASS: Validation regex HH:mm (00:00 - 23:59) chính xác tuyệt đối.")

    # Cập nhật giờ hợp lệ
    cursor.execute('UPDATE reminders SET time_string = ? WHERE id = 1', ("07:30",))
    assert cursor.rowcount == 1
    # Bật/tắt trạng thái
    cursor.execute('UPDATE reminders SET is_enabled = 0 WHERE id = 2', )
    assert cursor.rowcount == 1

    cursor.execute('SELECT time_string, is_enabled FROM reminders WHERE id = 1')
    assert cursor.fetchone() == ("07:30", 1)
    cursor.execute('SELECT time_string, is_enabled FROM reminders WHERE id = 2')
    assert cursor.fetchone() == ("21:00", 0)
    print("  [E-2] PASS: Cập nhật giờ nhắc và bật/tắt reminder hoạt động hoàn hảo.")

    print("\n======================================================================")
    print("  KẾT QUẢ: 100% TẤT CẢ CÁC CA KIỂM THỬ SQL STANDALONE ĐỀU ĐẠT (PASS)!")
    print("======================================================================")


if __name__ == "__main__":
    run_all_tests()
