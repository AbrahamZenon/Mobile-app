# PHẦN 2 – KẾT QUẢ TỪNG THÀNH VIÊN
## BÁO CÁO KẾT QUẢ CÁ NHÂN: THÀNH VIÊN 2

- **Họ và tên:** Nguyễn Hoàng Anh Đức
- **Mã sinh viên:** B23DCVT097
- **Chức năng đảm nhiệm:** Module Theo Dõi Tiến Độ, Thống Kê Trực Quan & Hệ Thống Động Lực Khích Lệ

---

### a. Mô tả chi tiết chức năng
- **Chức năng 1: Đo lường & Theo dõi tiến độ mục tiêu (Progress Tracking)**
  - Tự động tính toán tỷ lệ % hoàn thành của từng mục tiêu dựa trên số lượng công việc con đã hoàn thành.
  - Cập nhật trạng thái mục tiêu: Chưa bắt đầu, Đang thực hiện, Hoàn thành, Quá hạn (Overdue).
- **Chức năng 2: Trực quan hóa dữ liệu bằng Biểu đồ thống kê (Analytics & Charts)**
  - Biểu đồ tròn (Pie Chart): Phân bố mục tiêu theo danh mục (Sức khỏe, Học tập, Sự nghiệp...).
  - Biểu đồ cột / đường (Bar / Line Chart): Số lượng nhiệm vụ hoàn thành theo các ngày trong tuần và các tuần trong tháng.
  - Thống kê tỷ lệ hoàn thành trung bình của người dùng.
- **Chức năng 3: Hệ thống Khích lệ & Gamification (Motivation System)**
  - Cơ chế tính chuỗi ngày duy trì liên tiếp (Streak Counter & Flame Icon).
  - Hệ thống huy hiệu thành tích (Achievement Badges) mở khóa khi đạt các cột mốc (ví dụ: "Người khởi đầu", "Kỷ luật thép 7 ngày", "Chiến binh 30 ngày", "Người chinh phục mục tiêu").
  - Widget câu nói truyền cảm hứng (Motivational Quotes) thay đổi ngẫu nhiên mỗi ngày.
  - Phản hồi tích cực (Lottie Confetti animation / thông điệp chúc mừng) ngay khi người dùng tick hoàn thành mục tiêu.
- **Chức năng 4: Tích hợp Thông báo nhắc nhở (Scheduled Notifications)**
  - Cài đặt giờ nhắc nhở định kỳ hàng ngày (ví dụ: nhắc lên kế hoạch sáng 8:00, nhắc tổng kết tối 21:00).
  - Nhắc nhở hạn chót của mục tiêu trước 1 ngày hoặc 3 ngày.
- **Luồng thực hiện & Ràng buộc nghiệp vụ:**
  - Nếu người dùng bỏ lỡ 1 ngày không hoàn thành bất kỳ hành động nào, chuỗi streak sẽ được tính toán lại (trừ khi có tính năng đóng băng streak).
  - Thông báo phải được lên lịch chính xác ngay cả khi tắt ứng dụng.
  *(Chèn sơ đồ Activity Diagram luồng tính tiến độ & mở khóa huy hiệu tại đây)*

---

### b. Thiết kế giao diện (UI)
- Màn hình Thống kê tiến độ (Progress & Analytics Dashboard với biểu đồ trực quan).
- Màn hình Thành tích & Huy hiệu (Badges Collection & Streak counter).
- Card / Popup hiển thị câu nói động lực và hiệu ứng chúc mừng (Confetti dialog).
- Màn hình cài đặt thời gian nhắc nhở thông báo.
*(Chèn hình ảnh chụp màn hình UI thiết kế tại đây)*

---

### c. Giải pháp kỹ thuật để xây dựng chức năng
- **Mô tả giải pháp, kỹ thuật:**
  - Thuật toán tổng hợp dữ liệu thống kê từ CSDL SQLite theo chu kỳ thời gian (GroupBy Day/Week/Month).
  - Sử dụng thư viện biểu đồ chuyên sâu (như `fl_chart`) để render biểu đồ tương tác chạm/vuốt mượt mà.
  - Cấu trúc hệ thống Gamification: Bảng `BadgeModel` với logic kiểm tra điều kiện mở khóa tự động sau mỗi hành động của người dùng.
  - Sử dụng thư viện `flutter_local_notifications` và `timezone` để đặt lịch thông báo chuẩn xác theo múi giờ thiết bị.
- **Vấn đề mới thực hiện được:**
  - Ứng dụng tâm lý học hành vi (Behavioral Psychology) và Gamification vào app quản lý mục tiêu giúp tăng tỷ lệ quay lại của người dùng (retention rate).
  - Tích hợp hiệu ứng hoạt họa vi mô (micro-animations) tạo cảm xúc hào hứng mỗi khi hoàn thành mục tiêu.
- **Vấn đề khó khăn khi xây dựng:**
  - Xử lý biểu đồ hiển thị trực quan và co giãn responsive trên nhiều kích cỡ màn hình khác nhau mà không bị vỡ layout hoặc tràn chữ.
  - Quản lý thông báo định kỳ chạy ngầm trên các phiên bản hệ điều hành Android mới (cần xin quyền `POST_NOTIFICATIONS` và xử lý tối ưu hóa pin `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`).

---

### d. Các nội dung có thể phát triển tiếp trong tương lai
- Tích hợp widget ngoài màn hình chính điện thoại (Home Screen Widgets) để xem chuỗi streak và câu nói truyền cảm hứng nhanh chóng.
- Hệ thống chia sẻ huy hiệu thành tích lên Facebook, Instagram Stories.
- Báo cáo phân tích chuyên sâu hàng tháng xuất ra định dạng file PDF.
