# PHẦN 2 – KẾT QUẢ TỪNG THÀNH VIÊN
## BÁO CÁO KẾT QUẢ CÁ NHÂN: THÀNH VIÊN 1

- **Họ và tên:** [Điền Họ và Tên Thành Viên 1]
- **Mã sinh viên:** [Điền MSSV]
- **Chức năng đảm nhiệm:** Module Quản Lý Mục Tiêu, Kế Hoạch Cá Nhân & Nhật Ký Hành Trình

---

### a. Mô tả chi tiết chức năng
- **Chức năng 1: Thiết lập & Quản lý Mục tiêu cá nhân (Goal Management)**
  - Cho phép tạo mới, chỉnh sửa, xóa và xem danh sách mục tiêu.
  - Phân loại mục tiêu theo lĩnh vực: Sức khỏe, Công việc, Học tập, Tài chính, Sở thích.
  - Thiết lập thuộc tính: Ngắn hạn / Dài hạn, độ khó (Dễ / Trung bình / Khó), ngày bắt đầu và deadline.
- **Chức năng 2: Lập kế hoạch hàng ngày / tuần (Action Planning)**
  - Chia nhỏ mục tiêu thành các nhiệm vụ con (tasks / to-do items).
  - Thiết lập mức độ ưu tiên: Cao (High), Trung bình (Medium), Thấp (Low).
  - Đánh dấu hoàn thành nhiệm vụ theo ngày hoặc tuần.
- **Chức năng 3: Ghi chú & Nhật ký hành trình (Notes & Journaling)**
  - Viết nhật ký gắn liền với quá trình thực hiện mục tiêu.
  - Ghi nhận cảm xúc (mood), bài học rút ra và điều chỉnh chiến lược.
- **Chức năng 4: Bảo mật & Riêng tư (Security Settings)**
  - Thiết lập mã PIN hoặc kích hoạt khóa vân tay khi vào app để bảo vệ thông tin mục tiêu riêng tư.
- **Luồng thực hiện & Ràng buộc nghiệp vụ:**
  - Mục tiêu phải có tên và hạn chót hợp lệ (không được nhỏ hơn ngày hiện tại).
  - Khi xóa một mục tiêu, hệ thống phải cảnh báo người dùng và xử lý xóa liên hoàn (cascade) các nhiệm vụ và nhật ký con liên quan.
  *(Chèn sơ đồ Activity Diagram luồng tạo & quản lý mục tiêu tại đây)*

---

### b. Thiết kế giao diện (UI)
- Màn hình Dashboard danh sách mục tiêu (phân loại theo tab lĩnh vực).
- Màn hình tạo mới / chỉnh sửa mục tiêu (Bottom Sheet / Full screen form).
- Màn hình danh sách công việc hàng ngày / hàng tuần (Daily/Weekly Plan checklist).
- Màn hình viết và xem nhật ký hành trình.
- Màn hình cài đặt bảo mật mã PIN.
*(Chèn hình ảnh chụp màn hình UI thiết kế tại đây)*

---

### c. Giải pháp kỹ thuật để xây dựng chức năng
- **Mô tả giải pháp, kỹ thuật:**
  - Cấu trúc các Data Model: `GoalModel`, `TaskModel`, `JournalModel`.
  - Sử dụng SQLite / DAO Pattern để thực hiện các thao tác CRUD bất đồng bộ (async/await).
  - Quản lý trạng thái màn hình bằng State Management pattern, đảm bảo khi thêm/sửa/xóa mục tiêu thì giao diện cập nhật ngay lập tức.
  - Sử dụng thư viện bảo mật (như `flutter_secure_storage` hoặc Android Keystore) để mã hóa mã PIN của người dùng.
- **Vấn đề mới thực hiện được:**
  - Cơ chế phân loại đa cấp độ: Mục tiêu -> Giai đoạn (Milestone) -> Nhiệm vụ hàng ngày (Task) kèm theo mức ưu tiên rõ ràng.
- **Vấn đề khó khăn khi xây dựng:**
  - Xử lý ràng buộc quan hệ dữ liệu giữa Goal - Task - Journal trong SQLite để tránh mất tính toàn vẹn khi người dùng sửa đổi mục tiêu.
  - Tối ưu hóa UI Form nhập liệu phức tạp với DatePicker, Dropdown, Slider độ khó mượt mà trên nhiều kích thước màn hình.

---

### d. Các nội dung có thể phát triển tiếp trong tương lai
- Hỗ trợ đính kèm hình ảnh minh chứng kết quả vào nhật ký mục tiêu.
- Tính năng gắn thẻ tag tùy chỉnh không giới hạn theo nhu cầu cá nhân.
- Tự động gợi ý danh sách to-do theo mẫu template sẵn (ví dụ: Template học IELTS, Template chạy bộ 5km...).
