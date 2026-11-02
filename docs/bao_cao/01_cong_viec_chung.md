# PHẦN 1 – CÔNG VIỆC CHUNG CỦA NHÓM

## a. Lý do lựa chọn của ứng dụng
- **Lý do lựa chọn xây dựng ứng dụng:**
  - Nhu cầu phát triển bản thân và quản lý mục tiêu cá nhân ngày càng lớn.
  - Người dùng thường gặp khó khăn trong việc duy trì động lực, dễ bỏ cuộc giữa chừng nếu không có kế hoạch rõ ràng và không thấy được sự tiến bộ hàng ngày.
  - Ứng dụng kết hợp việc quản lý mục tiêu với yếu tố tâm lý hành vi (Gamification, Streak, Thông báo động viên) giúp người dùng bám sát mục tiêu.
- **Các ứng dụng có chức năng tương tự trên thị trường:**
  - *Ví dụ:* Fabulous, Habitica, Forest, Notion, Todoist...
  - *Ưu điểm của các app hiện có:* Giao diện đẹp, nhiều tính năng.
  - *Nhược điểm:* Quá phức tạp, tính phí cao (subscription), thiếu sự kết nối giữa mục tiêu dài hạn với kế hoạch hành động hàng ngày, thiếu cơ chế nhật ký đánh giá sâu.
  - *Ứng dụng của nhóm có gì khác biệt:* Tinh gọn, hoàn toàn miễn phí, dữ liệu lưu trữ cục bộ bảo mật cao, kết hợp giữa chia nhỏ mục tiêu + theo dõi tiến độ trực quan + cơ chế khích lệ tích cực + viết nhật ký hành trình.

---

## b. Mô tả yêu cầu chung của ứng dụng
- **Phân tích và mô tả yêu cầu chức năng (Functional Requirements):**
  - Quản lý mục tiêu (Ngắn hạn / Dài hạn, theo lĩnh vực: Sức khỏe, Công việc, Học tập, Sở thích...).
  - Quản lý kế hoạch hành động theo ngày / tuần (Action items / To-do list có mức độ ưu tiên).
  - Tự động đo lường & theo dõi tiến độ (Progress tracking theo %).
  - Trực quan hóa dữ liệu thống kê qua biểu đồ (Biểu đồ tròn tỷ lệ, biểu đồ cột công việc đã hoàn thành).
  - Hệ thống khích lệ & Gamification (Chuỗi ngày streak, huy hiệu vinh danh, câu nói truyền cảm hứng).
  - Ghi chú và nhật ký hành trình (Journaling & Retrospective).
  - Hệ thống thông báo nhắc nhở (Scheduled local notification).
  - Bảo mật dữ liệu cá nhân (Mã PIN / Xác thực vân tay).
- **Phân tích và mô tả yêu cầu phi chức năng (Non-functional Requirements):**
  - *Hiệu năng:* Khởi động nhanh dưới 2 giây, chuyển màn hình mượt mà (60fps).
  - *Bảo mật & Riêng tư:* Lưu trữ dữ liệu an toàn trên thiết bị, không chia sẻ thông tin cá nhân.
  - *Giao diện:* Thân thiện, chuẩn thiết kế Material 3, hỗ trợ Dark Mode.
  - *Tính sẵn sàng (Offline-first):* Hoạt động trơn tru ngay cả khi không có kết nối Internet.
- **Biểu đồ Use Case tổng quát:**
  *(Chèn ảnh sơ đồ Use Case từ thư mục `docs/diagrams/use_case_tong_quat.png` tại đây)*

---

## c. Phân tích lựa chọn kiến trúc, công nghệ và đưa ra mô hình thiết kế
- **Lựa chọn công nghệ:** Flutter / Dart (hoặc React Native / Kotlin), SQLite / Room / Hive cho CSDL local.
- **Mô hình kiến trúc ứng dụng:** Mô hình Clean Architecture kết hợp State Management (Provider / Bloc / Riverpod) phân tách rõ ràng 3 tầng: Presentation Layer, Domain Layer, Data Layer.
- **Mô hình kiến trúc thiết kế:**
  *(Chèn sơ đồ kiến trúc ứng dụng tại đây)*

---

## d. Cơ sở dữ liệu
- **Hình thức lưu trữ:** CSDL quan hệ SQLite (hoặc NoSQL Box - Hive/Isar) lưu cục bộ trên thiết bị người dùng.
- **Sơ đồ quan hệ thực thể (ERD) & Cấu trúc bảng:**
  - Bảng `Goal` (id, title, description, category, deadline, difficulty, type, progress, status, created_at)
  - Bảng `Task` (id, goal_id, title, priority, due_date, is_completed)
  - Bảng `Journal` (id, goal_id, date, note, mood, reflection)
  - Bảng `Badge` (id, title, description, icon, is_unlocked, unlocked_at)
  - Bảng `UserSetting` (id, pin_code, is_biometric_enabled, reminder_time)
  *(Chèn sơ đồ ERD từ thư mục `docs/diagrams/erd_database.png` tại đây)*

---

## e. Tổng quan các nội dung mới và khó mà nhóm đã thực hiện
- **Nội dung mới:**
  - Áp dụng kỹ thuật Gamification (Huy hiệu, Streak Flame) để tác động tích cực vào tâm lý người dùng.
  - Thuật toán tính trọng số tiến độ mục tiêu linh hoạt dựa trên mức độ hoàn thành các nhiệm vụ con và độ khó.
- **Nội dung khó:**
  - Tối ưu hóa xử lý truy vấn thống kê dữ liệu đa chiều để vẽ biểu đồ thời gian thực không gây giật lag giao diện.
  - Xử lý cơ chế đặt lịch thông báo cục bộ (Local Scheduled Notification) chính xác khi ứng dụng chạy ngầm hoặc sau khi khởi động lại máy.
  - Cơ chế bảo mật và mã hóa mã PIN / xác thực sinh trắc học cục bộ an toàn.
- **Dự kiến tính năng phát triển trong tương lai:**
  - Đồng bộ hóa dữ liệu đám mây (Cloud Sync đa thiết bị qua Firebase / Supabase).
  - Tích hợp AI cá nhân hóa (AI Goal Coach gợi ý lộ trình chia nhỏ mục tiêu thông minh).
  - Chia sẻ thành tích lên mạng xã hội hoặc nhóm bạn để cùng thi đua mục tiêu.
