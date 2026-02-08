# 🌙 NightfallAutoQuest

Chào mừng bạn đến với **NightfallAutoQuest**! Đây là một hệ thống giao nhiệm vụ tự động cực kỳ thông minh và linh hoạt dành cho các server Minecraft (Spigot/Paper). Plugin này được thiết kế để giúp server của bạn luôn nhộn nhịp, khiến người chơi luôn có mục tiêu để phấn đấu mà không cảm thấy nhàm chán.

## ✨ Điểm nổi bật là gì?

*   **⚡ Tự động hoàn toàn:** Hệ thống sẽ tự chọn ngẫu nhiên người chơi để giao nhiệm vụ theo chu kỳ. Bạn không cần phải tốn công setup thủ công từng tý một.
*   **📊 Theo dõi tiến độ trực quan:** Có thanh BossBar hiện ngay trên đầu để người chơi biết mình đã làm được bao nhiêu rồi.
*   **🎮 Đa dạng loại Quest:** Từ đào mỏ, săn quái, câu cá cho đến việc... đi bộ, cái gì cũng có thể biến thành nhiệm vụ.
*   **💾 Lưu trữ chắc chắn:** Hỗ trợ cả SQLite (mặc định cho gọn) và MySQL (cho các server lớn, ổn định hơn).
*   **🔗 Kết nối rộng rãi:** Tích hợp với PlaceholderAPI để bạn tha hồ hiển thị thông tin nhiệm vụ ở khắp mọi nơi (Scoreboard, Tab, vv).

## 🛠️ Các loại nhiệm vụ có sẵn

Plugin mang đến rất nhiều "thử thách" thú vị cho người chơi:

1.  **⛏️ Khai thác (Mining):** Đào các loại quặng hoặc đá theo yêu cầu.
2.  **🤺 Săn quái (Mob Killing):** Tiêu diệt các loại quái vật cụ thể.
3.  **🎣 Câu cá (Fishing):** Trở thành một ngư dân thực thụ.
4.  **🌾 Nông dân (Farming):** Thu hoạch mùa màng.
5.  **⚒️ Chế tạo (Crafting):** Làm ra những vật phẩm hữu ích.
6.  **🔥 Nung nấu (Smelting):** Nấu quặng hoặc thức ăn.
7.  **✨ Phù phép (Enchanting):** Nâng cấp trang bị.
8.  **🚶 Đi bộ (Walking):** Đơn giản là khám phá thế giới.
9.  **🏗️ Xây dựng (Placing):** Đặt các khối block vào vị trí.
10. **⚔️ Gây sát thương (Deal Damage):** Cho những chiến binh thích bạo lực.

## ⌨️ Lệnh và Quyền hạn

### Dành cho Người chơi (`naq.use`)
*   `/naq quest` (hoặc `/q`): Xem nhiệm vụ hiện tại bạn đang làm.
*   `/naq stats`: Xem "thành tích" cá nhân của bạn.
*   `/naq top`: Xem bảng xếp hạng những "thánh cày" quest.
*   `/naq giveup`: Nếu nhiệm vụ khó quá, bạn có thể bỏ qua (nhưng sẽ mất công sức đấy nhé!).
*   `/naq help`: Hiện ra danh sách các lệnh để bạn không bị bỡ ngỡ.

### Dành cho Admin (`naq.admin`)
*   `/naq reload`: Tải lại cấu hình plugin (không cần khởi động lại server).
*   `/naq reloadquests`: Tải lại danh sách các file nhiệm vụ.
*   `/naq module`: Bật/tắt các loại nhiệm vụ ngay lập tức.
*   `/naq getdata <tên>`: Kiểm tra xem một người chơi đang làm gì.
*   `/naq purge`: Xóa sạch dữ liệu (hãy cẩn thận với lệnh này!).

## 🚀 Cài đặt thế nào?

1.  Tải file `.jar` về và bỏ vào thư mục `plugins` của server.
2.  Khởi động server để plugin tạo ra các file cấu hình.
3.  Vào `plugins/NightfallAutoQuest/config.yml` để chỉnh sửa theo ý muốn (thời gian giao quest, tỷ lệ người chơi nhận quest...).
4.  Chỉnh sửa hoặc thêm mới các nhiệm vụ trong thư mục `quests/`.
5.  Dùng lệnh `/naq reload` và tận hưởng!

---
*Phát triển bởi **_kennji**!*
