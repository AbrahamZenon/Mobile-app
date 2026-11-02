# Mã Nguồn Ứng Dụng (Source Code)

Đặt mã nguồn của dự án ứng dụng di động tại đây (ví dụ: Flutter, React Native, hoặc Android Native/Kotlin).

### Cấu trúc khuyến nghị (Clean Architecture / MVC / MVVM):
```
src/
├── api/            # Tích hợp API / Backend Service (nếu có)
├── assets/         # Tài nguyên tĩnh nội bộ app (fonts, icons, mock data)
├── components/     # Các widget/component giao diện tái sử dụng (Button, Card, Chart...)
├── models/         # Khai báo các đối tượng dữ liệu (Goal, Task, Milestone, Badge, User...)
├── screens/        # Các màn hình chức năng chính (Home, GoalDetail, Stats, Reward, Profile...)
├── services/       # Xử lý logic CSDL cục bộ (SQLite/Hive) hoặc Cloud (Firebase/Supabase), Notification Service
└── utils/          # Hàm tiện ích, hằng số (constants), format ngày tháng, theme màu sắc
```
