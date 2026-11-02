# 🎯 ỨNG DỤNG QUẢN LÝ VÀ THEO DÕI MỤC TIÊU CÁ NHÂN
> **Môn học:** Phát triển ứng dụng di động (Mobile Application Development - MAD)  
> **Chủ đề 15:** Ứng dụng quản lý và theo dõi mục tiêu cá nhân: Đặt mục tiêu, theo dõi tiến độ, và nhận khích lệ.  
> **Quy mô nhóm:** 02 thành viên  

---

## 📌 MỤC LỤC
1. [Giới Thiệu Dự Án](#-giới-thiệu-dự-án)
2. [Thông Tin Thành Viên & Bảng Phân Công Công Việc](#-thông-tin-thành-viên--bảng-phân-công-công-việc)
3. [Kế Hoạch Thực Hiện Chi Tiết (Project Roadmap)](#-kế-hoạch-thực-hiện-chi-tiết-project-roadmap)
4. [Phân Rã Chức Năng Bám Sát Yêu Cầu Đề Tài](#-phân-rã-chức-năng-bám-sát-yêu-cầu-đề-tài)
5. [Cấu Trúc Thư Mục Repository (GitHub Outline)](#-cấu-trúc-thư-mục-repository-github-outline)
6. [Quy Trình Phối Hợp & Quy Chuẩn Git (Git Workflow)](#-quy-trình-phối-hợp--quy-chuẩn-git-git-workflow)
7. [Đề Xuất Công Nghệ & Kiến Trúc Kỹ Thuật](#-đề-xuất-công-nghệ--kiến-trúc-kỹ-thuật)
8. [Chuẩn Bị Khung Báo Cáo Bài Tập Nhóm](#-chuẩn-bị-khung-báo-cáo-bài-tập-nhóm)

---

## 📖 1. GIỚI THIỆU DỰ ÁN
Trong cuộc sống hiện đại, việc quản lý thời gian và duy trì kỷ luật để đạt được các mục tiêu cá nhân (học tập, sự nghiệp, sức khỏe, thói quen) là thách thức lớn đối với nhiều người.

**Ứng dụng Quản Lý & Theo Dõi Mục Tiêu Cá Nhân** giúp người dùng:
- Thiết lập mục tiêu rõ ràng theo phương pháp SMART (ngắn hạn, dài hạn, phân loại theo lĩnh vực).
- Chia nhỏ mục tiêu lớn thành các kế hoạch hành động và nhiệm vụ (tasks/milestones) hàng ngày, hàng tuần.
- Theo dõi tiến độ trực quan thông qua biểu đồ, bảng tiến trình và chỉ số định lượng.
- Nhận phản hồi tích cực, thông điệp truyền cảm hứng (quotes, streak, huy hiệu thưởng) để duy trì động lực bền bỉ.
- Viết nhật ký hành trình, đánh giá và điều chỉnh chiến lược kịp thời.

---

## 👥 2. THÔNG TIN THÀNH VIÊN & BẢNG PHÂN CÔNG CÔNG VIỆC

*(Áp dụng theo đúng mẫu Bìa Báo Cáo BTL MAD: tổng công sức đóng góp đạt 100%)*

| STT | Họ và Tên | MSSV | Phần việc tham gia chung cả nhóm | Phần công việc cá nhân đảm nhiệm | Ước tính % đóng góp |
|:---:|:---|:---:|:---|:---|:---:|
| **1** | Nguyễn Tất Khôi Nguyên | B23DCVT315 | • Khảo sát thị trường & phân tích yêu cầu.<br>• Thống nhất kiến trúc ứng dụng & thiết kế CSDL (ERD).<br>• Lập trình giao diện khung & cấu hình base project.<br>• Viết báo cáo Phần 1 & hoàn thiện slide, video demo. | **Module Quản Lý Mục Tiêu & Kế Hoạch:**<br>1. Thiết lập & CRUD mục tiêu đa lĩnh vực (ngắn hạn/dài hạn, độ khó, deadline).<br>2. Lập kế hoạch hàng ngày/tuần, quản lý danh sách việc cần làm (to-do) & mức độ ưu tiên.<br>3. Module Ghi chú & Nhật ký hành trình (Notes & Journaling).<br>4. Quản lý tài khoản, cấu hình & bảo mật quyền riêng tư (Mã PIN / Biometric lock). | **50%** |
| **2** | [Điền Họ Tên Thành Viên 2] | [MSSV 2] | • Khảo sát thị trường & phân tích yêu cầu.<br>• Thống nhất kiến trúc ứng dụng & thiết kế CSDL (ERD).<br>• Thiết kế UI/UX trên Figma, chuẩn bị asset đồ họa.<br>• Viết báo cáo Phần 1 & đóng gói bản cài đặt (APK). | **Module Theo Dõi Tiến Độ & Động Lực Khích Lệ:**<br>1. Thuật toán tính toán tiến độ mục tiêu tự động theo thời gian thực.<br>2. Trực quan hóa dữ liệu bằng biểu đồ thống kê (biểu đồ tròn, đường, cột theo tuần/tháng).<br>3. Hệ thống Khích lệ & Gamification (Chuỗi streak, bảng huy hiệu thành tích, quote truyền cảm hứng).<br>4. Tích hợp Thông báo nhắc nhở thông minh (Push Notification & Scheduled Alarms). | **50%** |

---

## 📅 3. KẾ HOẠCH THỰC HIỆN CHI TIẾT (PROJECT ROADMAP)

Kế hoạch được chia làm **6 giai đoạn (Sprints)**, đảm bảo tiến độ từ nghiên cứu đến bàn giao sản phẩm:

```mermaid
gantt
    title KẾ HOẠCH BÀI TẬP NHÓM (6 TUẦN)
    dateFormat  YYYY-MM-DD
    section Giai đoạn 1: Khởi động
    Khảo sát & Phân tích yêu cầu (SRS)    :done, w1_1, 2026-11-03, 3d
    Use Case tổng quát & Thiết kế ERD     :done, w1_2, 2026-11-06, 4d
    section Giai đoạn 2: Thiết kế & Setup
    Thiết kế UI/UX trên Figma             :active, w2_1, 2026-11-10, 4d
    Setup Repo GitHub & Kiến trúc dự án   :active, w2_2, 2026-11-13, 3d
    section Giai đoạn 3: Phát triển Core
    TV1: CRUD Mục tiêu & Tasks ngày/tuần  :w3_1, 2026-11-17, 7d
    TV2: Tracking tiến độ & Biểu đồ      :w3_2, 2026-11-17, 7d
    section Giai đoạn 4: Nâng cao & Khích lệ
    TV1: Nhật ký, ghi chú, bảo mật        :w4_1, 2026-11-24, 7d
    TV2: Gamification, Badge, Notification:w4_2, 2026-11-24, 7d
    section Giai đoạn 5: Tích hợp & Test
    Tích hợp module & Review chéo code    :w5_1, 2026-12-01, 4d
    Testing, Fix lỗi & Tối ưu hiệu năng   :w5_2, 2026-12-05, 3d
    section Giai đoạn 6: Nghiệm thu & Báo cáo
    Hoàn thiện file Báo cáo BTL (Word/PDF):w6_1, 2026-12-08, 4d
    Quay video demo app & Slide thuyết trình:w6_2, 2026-12-11, 3d
```

### Chi tiết từng giai đoạn:

#### 🔹 Tuần 1: Khởi Động & Phân Tích Hệ Thống
- [ ] Nghiên cứu đề tài số 15 trong tài liệu BTL MAD.
- [ ] Khảo sát các app tương tự trên thị trường (Fabulous, Habitica, Forest, Notion Goals): phân tích ưu/nhược điểm và định vị điểm khác biệt của ứng dụng nhóm.
- [ ] Xây dựng tài liệu đặc tả yêu cầu chức năng & phi chức năng.
- [ ] Vẽ biểu đồ Use Case tổng quát (Actor: User, System).
- [ ] Thiết kế cơ sở dữ liệu (Sơ đồ ERD hoặc cấu trúc bảng SQLite/NoSQL).

#### 🔹 Tuần 2: Thiết Kế UI/UX & Khởi Tạo Dự Án (Project Setup)
- [ ] Thiết kế Wireframe & Prototype giao diện chuẩn trên Figma (Dark/Light mode, bảng màu tạo cảm hứng).
- [ ] Thống nhất công nghệ phát triển (Khuyên dùng: **Flutter** hoặc **React Native** hoặc **Android Kotlin**).
- [ ] Khởi tạo Git repository, cấu hình nhánh `main`, `develop`, file `.gitignore`.
- [ ] Dựng khung kiến trúc mã nguồn (Clean Architecture / MVVM), cấu hình State Management và Local Database.

#### 🔹 Tuần 3: Phát Triển Tính Năng Cốt Lõi (Core Sprint)
- **Thành viên 1:**
  - [ ] Xây dựng màn hình danh sách mục tiêu (Home Dashboard).
  - [ ] Màn hình tạo mới/chỉnh sửa mục tiêu (Tên, mô tả, danh mục, hạn chót, độ khó, mục tiêu ngắn hạn/dài hạn).
  - [ ] Lập kế hoạch theo ngày và tuần: Tạo và đánh dấu hoàn thành các to-do item thuộc mục tiêu.
- **Thành viên 2:**
  - [ ] Xây dựng module tính toán % hoàn thành của mục tiêu dựa trên các nhiệm vụ con.
  - [ ] Màn hình chi tiết tiến độ mục tiêu.
  - [ ] Tích hợp thư viện biểu đồ trực quan (Biểu đồ tròn tỷ lệ hoàn thành, biểu đồ cột công việc đã làm trong tuần).

#### 🔹 Tuần 4: Tính Năng Mở Rộng & Động Lực Khích Lệ (Advanced Sprint)
- **Thành viên 1:**
  - [ ] Xây dựng module Ghi chú & Nhật ký hành trình (viết cảm nghĩ, khó khăn, bài học sau mỗi ngày).
  - [ ] Tính năng lọc, tìm kiếm và phân loại mục tiêu theo mức độ ưu tiên & độ khó.
  - [ ] Chức năng bảo mật cá nhân: Đặt mã PIN / xác thực vân tay khi mở app để bảo vệ mục tiêu riêng tư.
- **Thành viên 2:**
  - [ ] Xây dựng hệ thống Khích Lệ (Gamification): Tính điểm thưởng, chuỗi ngày liên tiếp (Streak flame), bảng mở khóa Huy hiệu (Badges).
  - [ ] Hộp thông điệp truyền cảm hứng (Daily Motivation Quotes & Dynamic Feedback khi hoàn thành mục tiêu).
  - [ ] Tích hợp tính năng Nhắc nhở (Local Push Notifications) theo lịch trình đã đặt trước.

#### 🔹 Tuần 5: Tích Hợp Hệ Thống & Kiểm Thử Toàn Diện (Testing & Optimization)
- [ ] Ghép nối toàn bộ các màn hình và module giữa 2 thành viên.
- [ ] Kiểm thử luồng người dùng (User Journey Testing).
- [ ] Kiểm thử trên các kích thước màn hình khác nhau (Điện thoại nhỏ, lớn, Tablet).
- [ ] Sửa lỗi phát sinh, tối ưu tốc độ phản hồi và chuyển cảnh mượt mà.
- [ ] Đóng gói file cài đặt `.apk`.

#### 🔹 Tuần 6: Hoàn Thiện Tài Liệu Báo Cáo & Chuẩn Bị Bảo Vệ
- [ ] Hoàn thiện báo cáo BTL theo đúng Khung chuẩn của giảng viên (Bìa, Phần 1 - Công việc chung, Phần 2 - Kết quả từng thành viên).
- [ ] Xuất file báo cáo Word/PDF và lưu vào thư mục `docs/bao_cao/`.
- [ ] Thiết kế Slide thuyết trình bảo vệ đề tài.
- [ ] Quay video demo ứng dụng (thời lượng 3 - 5 phút, giới thiệu đầy đủ luồng tính năng).
- [ ] Cập nhật lại README.md với hướng dẫn cài đặt và chạy ứng dụng.

---

## 🔍 4. PHÂN RÃ CHỨC NĂNG BÁM SÁT YÊU CẦU ĐỀ TÀI

| Nhóm chức năng (Theo Đề tài 15) | Mô tả chi tiết | Thành viên phụ trách chính |
|:---|:---|:---:|
| **1. Đặt mục tiêu cá nhân** | • Chọn lĩnh vực: Sức khỏe, Công việc, Học tập, Tài chính, Sở thích.<br>• Phân loại mục tiêu ngắn hạn (vài ngày/tuần) và dài hạn (tháng/năm).<br>• Nhập mô tả chi tiết, ngày bắt đầu, deadline. | **Thành viên 1** |
| **2. Kế hoạch hàng ngày / tuần** | • Lên danh sách các việc cần làm cụ thể (Action Steps) để đạt mục tiêu.<br>• Thiết lập thứ tự ưu tiên (Cao, Trung bình, Thấp). | **Thành viên 1** |
| **3. Quản lý nhiều mục tiêu** | • Xem tổng quan dashboard toàn bộ mục tiêu đang chạy.<br>• Đánh giá mức độ khó của từng mục tiêu (Dễ, Vừa, Khó). | **Thành viên 1** |
| **4. Ghi chú & Nhật ký** | • Ghi chép nhật ký hành trình thực hiện mục tiêu.<br>• Tự đánh giá cảm xúc, tiến bộ và điều chỉnh chiến lược. | **Thành viên 1** |
| **5. Bảo mật & Riêng tư** | • Bảo vệ dữ liệu mục tiêu cá nhân không bị người khác tò mò.<br>• Khóa ứng dụng bằng PIN / Sinh trắc học (Fingerprint / Face ID). | **Thành viên 1** |
| **6. Theo dõi tiến độ** | • Tự động tính toán % hoàn thành theo thời gian.<br>• Biểu đồ thống kê: Cột, tròn, xu hướng hoàn thành theo tuần/tháng. | **Thành viên 2** |
| **7. Nhận khích lệ & Động lực** | • Phản hồi tích cực tức thì khi người dùng hoàn thành một nhiệm vụ.<br>• Hệ thống chuỗi ngày liên tục (Streaks) & Danh hiệu/Huy hiệu thưởng.<br>• Hiển thị câu nói truyền cảm hứng (Motivational Quotes) ngẫu nhiên mỗi ngày. | **Thành viên 2** |
| **8. Tích hợp nhắc nhở** | • Đặt lịch nhắc nhở hàng ngày/hàng tuần cho từng mục tiêu cụ thể.<br>• Bắn thông báo đẩy cục bộ (Local Notifications) đúng khung giờ người dùng chọn. | **Thành viên 2** |

---

## 📂 5. CẤU TRÚC THƯ MỤC REPOSITORY (GITHUB OUTLINE)

Repository đã được dựng sẵn cấu trúc outline chuyên nghiệp để 2 thành viên thuận tiện push code và tài liệu:

```
Mobile-app/
│
├── .gitignore                      # Bộ lọc bỏ file rác build, cache, IDE
├── README.md                       # Kế hoạch làm việc, tổng quan dự án & hướng dẫn
│
├── docs/                           # Tài liệu báo cáo & thiết kế dự án
│   ├── README.md                   # Hướng dẫn chi tiết viết tài liệu
│   ├── bao_cao/                    # Chứa báo cáo bài tập nhóm theo khung của môn học
│   │   ├── 00_trang_bia.md         # Trang bìa & bảng % đóng góp
│   │   ├── 01_cong_viec_chung.md   # Phần 1: Tổng quan, Use Case, CSDL, điểm mới & khó
│   │   ├── 02_thanh_vien_1.md      # Phần 2.1: Chi tiết kết quả của Thành viên 1
│   │   └── 03_thanh_vien_2.md      # Phần 2.2: Chi tiết kết quả của Thành viên 2
│   ├── diagrams/                   # Sơ đồ Use Case, ERD, Kiến trúc, Activity Diagram
│   └── ui_ux/                      # Mockup giao diện, Figma screenshots
│
├── assets/                         # Tài nguyên ứng dụng
│   ├── images/                     # Ảnh minh họa, logo, badges, avatar
│   ├── icons/                      # Các icon danh mục mục tiêu
│   └── quotes/                     # Bộ dữ liệu châm ngôn truyền cảm hứng
│
└── src/                            # Mã nguồn ứng dụng (Source Code)
    ├── README.md                   # Hướng dẫn cấu trúc source code
    ├── models/                     # Data models (Goal, Task, Journal, Badge...)
    ├── screens/                    # Màn hình giao diện (Home, Detail, Stats, Diary, Settings...)
    ├── components/                 # Các UI widgets tái sử dụng (Card, Progress Bar, Dialog...)
    ├── services/                   # Xử lý CSDL cục bộ (SQLite/Hive) & Notification
    └── utils/                      # Helper, formatters, colors, theme
```

---

## 🌿 6. QUY TRÌNH PHỐI HỢP & QUY CHUẨN GIT (GIT WORKFLOW)

Để tránh xung đột code (conflict) giữa 2 thành viên, nhóm áp dụng chiến lược phân nhánh chuẩn:

### 1. Quy định các nhánh (Branching Strategy):
- `main`: Nhánh ổn định cao nhất, chỉ merge khi tính năng đã hoàn thiện và chạy không lỗi.
- `develop`: Nhánh tích hợp chung của nhóm. Mọi tính năng sau khi làm xong sẽ merge vào đây để test.
- `feature/tv1-<tên-tính-năng>`: Nhánh làm việc riêng của Thành viên 1 (ví dụ: `feature/tv1-goal-crud`, `feature/tv1-journal`).
- `feature/tv2-<tên-tính-năng>`: Nhánh làm việc riêng của Thành viên 2 (ví dụ: `feature/tv2-progress-chart`, `feature/tv2-notification`).

### 2. Quy tắc Commit Message (Chuẩn Conventional Commits):
- `feat:` Thêm chức năng mới (vd: `feat: add goal creation screen with date picker`)
- `fix:` Sửa lỗi (vd: `fix: resolve streak calculation bug`)
- `docs:` Cập nhật tài liệu, README hoặc báo cáo (vd: `docs: update use case diagram in docs`)
- `style:` Chỉnh sửa giao diện, màu sắc, font chữ không ảnh hưởng logic
- `refactor:` Tối ưu, cấu trúc lại code

### 3. Quy trình Pull Request (PR):
1. Thành viên hoàn thành việc trên nhánh riêng của mình.
2. Push lên GitHub: `git push origin feature/tv1-goal-crud`
3. Tạo Pull Request trên GitHub để merge vào nhánh `develop`.
4. Thành viên còn lại vào **Review code**, chạy thử và bấm `Approve & Merge`.
5. Cả 2 thành viên thực hiện `git pull origin develop` để đồng bộ code mới nhất.

---

## 🛠️ 7. ĐỀ XUẤT CÔNG NGHỆ & KIẾN TRÚC KỸ THUẬT

### 1. Công nghệ đề xuất:
- **Ngôn ngữ & Framework:** **Flutter (Dart)** hoặc **React Native** (Hỗ trợ đa nền tảng Android/iOS, dựng UI nhanh, thư viện phong phú) hoặc **Android Native (Kotlin + Jetpack Compose)**.
- **Lưu trữ CSDL Cục bộ (Local Database):** SQLite / Room DB / Isar / Hive (Dữ liệu cá nhân được lưu offline trực tiếp trên máy đảm bảo quyền riêng tư và tốc độ cao).
- **Trực quan hóa Dữ liệu:** Thư viện `fl_chart` (với Flutter) hoặc `react-native-chart-kit` (với React Native).
- **Thông báo & Lên lịch:** `flutter_local_notifications` + `workmanager` (hoặc AlarmManager trên Android).
- **Bảo mật sinh trắc học:** `local_auth` (Hỗ trợ xác thực vân tay / Face ID / Passcode).

### 2. Điểm Mới & Điểm Khó Của Dự Án (Để đạt điểm cao theo khung BTL):
- **Điểm mới:** 
  - Không chỉ dừng lại ở việc check-in việc cần làm (to-do) đơn thuần mà có cơ chế **tự động tính toán trọng số tiến độ** dựa trên độ khó và hạn chót.
  - Tích hợp **thuật toán nhắc nhở thông minh** dựa trên thời gian thói quen của người dùng và hệ thống khích lệ tâm lý tích cực (Streak, Huy hiệu thăng hạng).
- **Điểm khó:**
  - Lập lịch Background Tasks & Notification chính xác trên các phiên bản Android mới (quản lý Doze Mode và Battery Optimization).
  - Thuật toán tổng hợp dữ liệu thống kê biểu đồ động theo nhiều mốc thời gian (ngày/tuần/tháng/toàn thời gian).
  - Đảm bảo tính toàn vẹn dữ liệu khi có nhiều mục tiêu liên kết với hàng chục công việc con và nhật ký liên quan.

---

## 📝 8. CHUẨN BỊ KHUNG BÁO CÁO BÀI TẬP NHÓM
Nội dung báo cáo chi tiết được chuẩn bị sẵn trong thư mục `docs/bao_cao/` bám sát 100% tài liệu hướng dẫn của Giảng viên:
- **Trang bìa:** Tên đề tài, bảng phân công nhiệm vụ và ước tính % đóng góp.
- **Phần 1 - Công việc chung:** Lý do lựa chọn, so sánh thị trường, đặc tả chức năng/phi chức năng, Use Case tổng quát, kiến trúc, sơ đồ CSDL, điểm mới & khó.
- **Phần 2 - Kết quả từng thành viên:** Luồng thực hiện (Activity Diagram), thiết kế màn hình, giải pháp kỹ thuật cụ thể và hướng phát triển tương lai cho từng module.

---
*Chúc nhóm 2 bạn hoàn thành xuất sắc Bài tập lớn môn Phát triển ứng dụng di động! 🚀*
