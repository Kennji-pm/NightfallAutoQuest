# 🌙 NightfallAutoQuest

**NightfallAutoQuest** là một hệ thống giao nhiệm vụ tự động (Quest) chuyên nghiệp và tối ưu cho các server Minecraft (Spigot/Paper). Plugin được xây dựng với kiến trúc hiện đại, tập trung vào hiệu suất và trải nghiệm người chơi.

## ✨ Tính năng nổi bật

- **⚡ Tự động hoàn toàn (Async):** Hệ thống scheduler chạy ngầm tự động chọn người chơi để giao nhiệm vụ theo chu kỳ cấu hình sẵn.
- **🌍 Giới hạn thế giới (World Restriction):** Dễ dàng giới hạn các world được phép gán và làm nhiệm vụ (`allowed_worlds`).
- **🧩 Hệ thống Module linh hoạt:** Bật/tắt các loại nhiệm vụ (`mining`, `farming`, `mobkilling`, v.v.) ngay trong `config.yml`.
- **📊 Theo dõi tiến trình (BossBar):** Thanh BossBar hiển thị thời gian thực tiến độ, tên nhiệm vụ và thời gian còn lại.
- **✨ Placeholder động & Thông minh:** 
  - `%amount%`: Tự động thay thế bằng số lượng đã random (Hỗ trợ định dạng range `10-20`).
  - `%task%`: Tự động định dạng tên vật phẩm/mục tiêu (VD: `DIAMOND_ORE` -> `Diamond Ore`).
- **💾 Lưu trữ hiệu suất cao:** Sử dụng **HikariCP** cho kết nối Database (Hỗ trợ SQLite & MySQL).
- **🔗 Tích hợp PlaceholderAPI:** Cung cấp hàng loạt placeholder để hiển thị thông tin ở Scoreboard, Tab, vv.

## 🛠️ Các loại nhiệm vụ (Modules)

Plugin hỗ trợ đa dạng các loại hành động để biến thành nhiệm vụ:
- **Mining** (Khai thác), **Placing** (Đặt khối), **Crafting** (Chế tạo)
- **Mob Killing** (Săn quái), **Farming** (Thu hoạch), **Fishing** (Câu cá)
- **Walking** (Di chuyển), **Smelting** (Nấu đồ), **Enchanting** (Phù phép)
- **Deal Damage** (Gây sát thương), **Placeholder** (Dựa trên PAPI)

## ⌨️ Lệnh và Quyền hạn

### Lệnh dành cho Người chơi (`nightfallautoquest.player`)
- `/naq quest` (hoặc `/q`): Xem chi tiết nhiệm vụ hiện tại, tiến độ và thời gian.
- `/naq stats`: Xem thống kê cá nhân (Số quest hoàn thành, thất bại, tỷ lệ).
- `/naq top`: Xem bảng xếp hạng những người chơi chăm chỉ nhất.
- `/naq giveup`: Từ bỏ nhiệm vụ hiện tại.
- `/naq help`: Xem danh sách trợ giúp.

### Lệnh dành cho Admin (`nightfallautoquest.admin`)
- `/naq reload`: Tải lại toàn bộ cấu hình, module và các file nhiệm vụ (`quests/`).
- `/naq purge`: Xóa sạch toàn bộ dữ liệu người chơi (Cẩn thận!).

## 🚀 Cài đặt

1. Tải file `.jar` và bỏ vào thư mục `plugins/`.
2. Khởi động server để tạo các file cấu hình mặc định.
3. Chỉnh sửa `config.yml` để cấu hình thế giới, module và thời gian.
4. Tùy chỉnh hoặc thêm mới quest trong thư mục `quests/`.
5. Sử dụng `/naq reload` để áp dụng mọi thay đổi.

---
*Phát triển với ❤️ bởi **Nightfall Team & _kennji***!*
