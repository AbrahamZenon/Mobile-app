# -*- coding: utf-8 -*-
"""
test_prompt22_regression.py

Kiểm thử hồi quy cho Prompt 2.2 — GoalTracker Database V3
Ghi chú: Đây là kiểm thử SQL standalone mô phỏng logic của DatabaseHelper.java,
KHÔNG phải Android Instrumentation Test.

Tác giả: Thành viên 2 — Nguyễn Hoàng Anh Đức
"""
import sqlite3
import sys
from datetime import datetime

sys.stdout.reconfigure(encoding='utf-8')

PASS_COUNT = 0
FAIL_COUNT = 0


def assert_equal(test_name, actual, expected, msg=""):
    global PASS_COUNT, FAIL_COUNT
    if actual == expected:
        print(f"  [PASS] {test_name}")
        PASS_COUNT += 1
    else:
        print(f"  [FAIL] {test_name}: expected={expected!r}, actual={actual!r}. {msg}")
        FAIL_COUNT += 1


def assert_not_none(test_name, actual, msg=""):
    global PASS_COUNT, FAIL_COUNT
    if actual is not None:
        print(f"  [PASS] {test_name}")
        PASS_COUNT += 1
    else:
        print(f"  [FAIL] {test_name}: expected NOT NULL, got None. {msg}")
        FAIL_COUNT += 1


def assert_raises(test_name, exc_type, func, *args, **kwargs):
    global PASS_COUNT, FAIL_COUNT
    try:
        func(*args, **kwargs)
        print(f"  [FAIL] {test_name}: expected {exc_type.__name__} but no exception raised.")
        FAIL_COUNT += 1
    except exc_type as e:
        print(f"  [PASS] {test_name} (raised {exc_type.__name__}: {e})")
        PASS_COUNT += 1
    except Exception as e:
        print(f"  [FAIL] {test_name}: expected {exc_type.__name__} but got {type(e).__name__}: {e}")
        FAIL_COUNT += 1


def create_tasks_table(conn):
    conn.execute("""
        CREATE TABLE IF NOT EXISTS tasks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            goal_id INTEGER,
            title TEXT NOT NULL,
            is_completed INTEGER DEFAULT 0,
            completed_at TEXT
        )
    """)
    conn.commit()


def simulate_update_task_completion(conn, task_id, is_completed):
    """
    Mô phỏng logic updateTaskCompletion() trong DatabaseHelper.java (đã sửa theo Prompt 2.2).
    Chỉ ghi timestamp khi transition 0→1.
    """
    cur = conn.cursor()
    cur.execute("SELECT is_completed, completed_at FROM tasks WHERE id = ?", (task_id,))
    row = cur.fetchone()
    if row is None:
        return
    old_is_completed, old_completed_at = row

    if is_completed:
        if old_is_completed == 0:
            # Transition 0→1: ghi timestamp
            now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            conn.execute(
                "UPDATE tasks SET is_completed = 1, completed_at = ? WHERE id = ?",
                (now, task_id)
            )
        else:
            # 1→1: chỉ cập nhật is_completed, KHÔNG thay đổi completed_at
            conn.execute(
                "UPDATE tasks SET is_completed = 1 WHERE id = ?",
                (task_id,)
            )
    else:
        # → 0: completed_at = NULL
        conn.execute(
            "UPDATE tasks SET is_completed = 0, completed_at = NULL WHERE id = ?",
            (task_id,)
        )
    conn.commit()


def get_task(conn, task_id):
    cur = conn.cursor()
    cur.execute("SELECT is_completed, completed_at FROM tasks WHERE id = ?", (task_id,))
    return cur.fetchone()


# ==========================================
# NHÓM T — Timestamp Logic
# ==========================================

def test_T1():
    """T1: 0,NULL → complete → is_completed=1, completed_at IS NOT NULL"""
    print("\n[T1] Task(is_completed=0, completed_at=NULL) → updateTaskCompletion(id, true)")
    conn = sqlite3.connect(":memory:")
    create_tasks_table(conn)
    conn.execute("INSERT INTO tasks (id, goal_id, title, is_completed, completed_at) VALUES (1, 1, 'Task T1', 0, NULL)")
    conn.commit()

    simulate_update_task_completion(conn, 1, True)
    is_completed, completed_at = get_task(conn, 1)

    assert_equal("T1-a: is_completed=1", is_completed, 1)
    assert_not_none("T1-b: completed_at IS NOT NULL (timestamp mới)", completed_at,
                    "Expected a new timestamp to be written")
    conn.close()


def test_T2():
    """T2: 1,'2026-11-01 10:20:30' → complete again → completed_at giữ nguyên"""
    print("\n[T2] Task(is_completed=1, completed_at='2026-11-01 10:20:30') → updateTaskCompletion(id, true)")
    conn = sqlite3.connect(":memory:")
    create_tasks_table(conn)
    conn.execute(
        "INSERT INTO tasks (id, goal_id, title, is_completed, completed_at) VALUES (1, 1, 'Task T2', 1, '2026-11-01 10:20:30')"
    )
    conn.commit()

    simulate_update_task_completion(conn, 1, True)
    is_completed, completed_at = get_task(conn, 1)

    assert_equal("T2-a: is_completed=1", is_completed, 1)
    assert_equal("T2-b: completed_at giữ nguyên '2026-11-01 10:20:30'",
                 completed_at, "2026-11-01 10:20:30",
                 "Timestamp must NOT be overwritten when task was already completed")
    conn.close()


def test_T3():
    """T3 (CRITICAL): Legacy DB v1 — is_completed=1, completed_at=NULL → complete → completed_at vẫn NULL"""
    print("\n[T3-CRITICAL] Legacy Task(is_completed=1, completed_at=NULL) → updateTaskCompletion(id, true)")
    conn = sqlite3.connect(":memory:")
    create_tasks_table(conn)
    conn.execute(
        "INSERT INTO tasks (id, goal_id, title, is_completed, completed_at) VALUES (1, 1, 'Legacy Task T3', 1, NULL)"
    )
    conn.commit()

    simulate_update_task_completion(conn, 1, True)
    is_completed, completed_at = get_task(conn, 1)

    assert_equal("T3-a: is_completed=1", is_completed, 1)
    assert_equal("T3-b: completed_at vẫn là NULL (KHÔNG tạo timestamp mới cho legacy task)",
                 completed_at, None,
                 "CRITICAL: Must NOT create new timestamp for legacy task that was already completed with NULL completed_at")
    conn.close()


def test_T4():
    """T4: 1,'2026-11-01 10:20:30' → uncomplete → is_completed=0, completed_at=NULL"""
    print("\n[T4] Task(is_completed=1, completed_at='2026-11-01 10:20:30') → updateTaskCompletion(id, false)")
    conn = sqlite3.connect(":memory:")
    create_tasks_table(conn)
    conn.execute(
        "INSERT INTO tasks (id, goal_id, title, is_completed, completed_at) VALUES (1, 1, 'Task T4', 1, '2026-11-01 10:20:30')"
    )
    conn.commit()

    simulate_update_task_completion(conn, 1, False)
    is_completed, completed_at = get_task(conn, 1)

    assert_equal("T4-a: is_completed=0", is_completed, 0)
    assert_equal("T4-b: completed_at=NULL", completed_at, None,
                 "completed_at must be set to NULL on uncomplete")
    conn.close()


def test_T5():
    """T5: 0,NULL → uncomplete → is_completed=0, completed_at=NULL"""
    print("\n[T5] Task(is_completed=0, completed_at=NULL) → updateTaskCompletion(id, false)")
    conn = sqlite3.connect(":memory:")
    create_tasks_table(conn)
    conn.execute(
        "INSERT INTO tasks (id, goal_id, title, is_completed, completed_at) VALUES (1, 1, 'Task T5', 0, NULL)"
    )
    conn.commit()

    simulate_update_task_completion(conn, 1, False)
    is_completed, completed_at = get_task(conn, 1)

    assert_equal("T5-a: is_completed=0", is_completed, 0)
    assert_equal("T5-b: completed_at=NULL", completed_at, None)
    conn.close()


# ==========================================
# NHÓM R — Reminder Uniqueness
# ==========================================

def create_reminders_table_v1(conn):
    """Tạo bảng reminders không có unique index (mô phỏng schema DB v1/v2)"""
    conn.execute("""
        CREATE TABLE IF NOT EXISTS reminders (
            id INTEGER PRIMARY KEY,
            type TEXT NOT NULL,
            time_string TEXT NOT NULL,
            is_enabled INTEGER NOT NULL DEFAULT 1
        )
    """)
    conn.commit()


def simulate_migrate_v2_to_v3(conn):
    """
    Mô phỏng migration v2→v3:
    - Kiểm tra duplicate type
    - Nếu không có duplicate: tạo unique index
    - Nếu có duplicate: raise Exception (không xóa dữ liệu)
    """
    cur = conn.cursor()
    cur.execute("SELECT type, COUNT(*) as cnt FROM reminders GROUP BY type HAVING COUNT(*) > 1")
    has_dup = cur.fetchone() is not None
    if has_dup:
        raise Exception(
            "Migration v2→v3 thất bại: Tồn tại bản ghi reminders trùng type. "
            "Cần xử lý thủ công trước khi tạo unique index."
        )
    conn.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_reminders_type_unique ON reminders(type);")
    conn.commit()


def test_R1():
    """R1: Migration từ DB chưa có bảng reminders (v1→v3 fresh install) → tạo bảng + index thành công"""
    print("\n[R1] Fresh install (DB chưa có bảng reminders) → Tạo bảng + unique index")
    conn = sqlite3.connect(":memory:")
    # Mô phỏng onCreate() của DatabaseHelper v3 — tạo bảng + index ngay
    conn.execute("""
        CREATE TABLE IF NOT EXISTS reminders (
            id INTEGER PRIMARY KEY,
            type TEXT NOT NULL,
            time_string TEXT NOT NULL,
            is_enabled INTEGER NOT NULL DEFAULT 1
        )
    """)
    conn.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_reminders_type_unique ON reminders(type);")
    conn.commit()

    # Kiểm tra index tồn tại
    cur = conn.cursor()
    cur.execute("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_reminders_type_unique'")
    row = cur.fetchone()
    assert_not_none("R1: unique index tồn tại sau fresh install", row,
                    "idx_reminders_type_unique should be created on fresh install")
    conn.close()


def test_R2():
    """R2: Migration v2→v3 không có duplicate → tạo index thành công"""
    print("\n[R2] DB v2 (không có duplicate reminders) → migrate v2→v3 thành công")
    conn = sqlite3.connect(":memory:")
    create_reminders_table_v1(conn)
    conn.execute("INSERT INTO reminders VALUES (1, 'MORNING', '08:00', 1)")
    conn.execute("INSERT INTO reminders VALUES (2, 'EVENING', '21:00', 1)")
    conn.commit()

    try:
        simulate_migrate_v2_to_v3(conn)
        # Kiểm tra index tồn tại
        cur = conn.cursor()
        cur.execute("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_reminders_type_unique'")
        row = cur.fetchone()
        assert_not_none("R2: unique index tồn tại sau migration v2→v3", row)
    except Exception as e:
        global FAIL_COUNT
        print(f"  [FAIL] R2: unexpected exception: {e}")
        FAIL_COUNT += 1
    conn.close()


def test_R3():
    """R3: Migration v2→v3 có duplicate type='MORNING' → detect, raise lỗi, KHÔNG xóa dữ liệu"""
    print("\n[R3] DB v2 có 2 bản ghi type='MORNING' → migrate phải raise lỗi, KHÔNG xóa")
    conn = sqlite3.connect(":memory:")
    create_reminders_table_v1(conn)
    # Chèn 2 bản ghi cùng type MORNING (vi phạm uniqueness)
    conn.execute("INSERT INTO reminders VALUES (1, 'MORNING', '08:00', 1)")
    conn.execute("INSERT INTO reminders VALUES (2, 'MORNING', '09:00', 1)")  # Duplicate!
    conn.commit()

    raised = False
    try:
        simulate_migrate_v2_to_v3(conn)
    except Exception as e:
        raised = True
        print(f"    -> Caught expected error: {e}")

    # Kiểm tra lỗi đã được raise
    assert_equal("R3-a: Exception raised khi có duplicate", raised, True,
                 "Migration MUST raise an error on duplicate type")

    # Kiểm tra dữ liệu không bị xóa
    cur = conn.cursor()
    cur.execute("SELECT COUNT(*) FROM reminders WHERE type='MORNING'")
    count = cur.fetchone()[0]
    assert_equal("R3-b: Dữ liệu vẫn còn 2 bản ghi MORNING (không tự xóa)", count, 2,
                 "Data must NOT be deleted during failed migration")
    conn.close()


def test_R4():
    """R4: INSERT cùng type='MORNING' 2 lần sau khi có unique index → IntegrityError"""
    print("\n[R4] Sau khi có unique index, INSERT type='MORNING' lần 2 → IntegrityError")
    conn = sqlite3.connect(":memory:")
    create_reminders_table_v1(conn)
    conn.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_reminders_type_unique ON reminders(type);")
    conn.execute("INSERT INTO reminders VALUES (1, 'MORNING', '08:00', 1)")
    conn.commit()

    def insert_duplicate():
        conn.execute("INSERT INTO reminders VALUES (3, 'MORNING', '09:00', 1)")
        conn.commit()

    assert_raises("R4: INSERT duplicate type → IntegrityError", sqlite3.IntegrityError, insert_duplicate)
    conn.close()


def test_R5():
    """R5: INSERT type='MORNING' và type='EVENING' → thành công (khác type)"""
    print("\n[R5] INSERT type='MORNING' và type='EVENING' → cả hai thành công")
    conn = sqlite3.connect(":memory:")
    create_reminders_table_v1(conn)
    conn.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_reminders_type_unique ON reminders(type);")
    conn.commit()

    try:
        conn.execute("INSERT INTO reminders VALUES (1, 'MORNING', '08:00', 1)")
        conn.execute("INSERT INTO reminders VALUES (2, 'EVENING', '21:00', 1)")
        conn.commit()

        cur = conn.cursor()
        cur.execute("SELECT COUNT(*) FROM reminders")
        count = cur.fetchone()[0]
        assert_equal("R5: Đã insert 2 reminders với type khác nhau thành công", count, 2)
    except Exception as e:
        global FAIL_COUNT
        print(f"  [FAIL] R5: unexpected exception: {e}")
        FAIL_COUNT += 1
    conn.close()


def test_R6():
    """R6: UPDATE reminder đã tồn tại (upsert pattern) → thành công, không tạo duplicate"""
    print("\n[R6] UPDATE thời gian reminder type='MORNING' → thành công, không duplicate")
    conn = sqlite3.connect(":memory:")
    create_reminders_table_v1(conn)
    conn.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_reminders_type_unique ON reminders(type);")
    conn.execute("INSERT INTO reminders VALUES (1, 'MORNING', '08:00', 1)")
    conn.execute("INSERT INTO reminders VALUES (2, 'EVENING', '21:00', 1)")
    conn.commit()

    try:
        # Cập nhật giờ MORNING từ 08:00 → 07:30
        conn.execute("UPDATE reminders SET time_string = '07:30' WHERE type = 'MORNING'")
        conn.commit()

        cur = conn.cursor()
        cur.execute("SELECT COUNT(*) FROM reminders WHERE type='MORNING'")
        count = cur.fetchone()[0]
        assert_equal("R6-a: Vẫn chỉ có 1 bản ghi MORNING sau khi update", count, 1)

        cur.execute("SELECT time_string FROM reminders WHERE type='MORNING'")
        time_val = cur.fetchone()[0]
        assert_equal("R6-b: Giờ đã được cập nhật thành '07:30'", time_val, "07:30")
    except Exception as e:
        global FAIL_COUNT
        print(f"  [FAIL] R6: unexpected exception: {e}")
        FAIL_COUNT += 1
    conn.close()


if __name__ == "__main__":
    print("=" * 60)
    print("GoalTracker DB v3 — Regression Tests (Prompt 2.2)")
    print("Loại: SQL standalone (sqlite3 Python in-memory)")
    print("KHÔNG phải Android Instrumentation Test")
    print("=" * 60)

    print("\n--- NHÓM T: TIMESTAMP LOGIC ---")
    test_T1()
    test_T2()
    test_T3()
    test_T4()
    test_T5()

    print("\n--- NHÓM R: REMINDER UNIQUENESS ---")
    test_R1()
    test_R2()
    test_R3()
    test_R4()
    test_R5()
    test_R6()

    print("\n" + "=" * 60)
    total = PASS_COUNT + FAIL_COUNT
    print(f"Tổng kết: {PASS_COUNT}/{total} PASS, {FAIL_COUNT} FAIL")
    print("=" * 60)
    sys.exit(0 if FAIL_COUNT == 0 else 1)
