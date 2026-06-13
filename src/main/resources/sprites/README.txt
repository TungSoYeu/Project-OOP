================================================================
  DANH SÁCH FILE ẢNH CẦN THÊM VÀO THƯ MỤC NÀY
  (AssetManager sẽ tự động load theo tên file)
================================================================

Quy tắc đặt tên:  {loài}_{trạng thái}.gif  hoặc  .png
Ưu tiên:           GIF (ảnh động) > PNG (ảnh tĩnh)
Fallback:          Nếu không có file → dùng hình vẽ vector

================================================================
  THỰC VẬT (không có trạng thái)
================================================================

  grass.png              — Cỏ
  fruittree.png          — Cây ăn quả

================================================================
  THỎ (Rabbit)
================================================================

  rabbit_idle.gif        — Thỏ đứng yên / nhìn xung quanh
  rabbit_run.gif         — Thỏ đang chạy (chạy trốn / di chuyển)
  rabbit_eat.gif         — Thỏ đang gặm cỏ
  rabbit_sleep.gif       — Thỏ đang ngủ (nằm cuộn tròn)
  rabbit.png             — Ảnh tĩnh mặc định (fallback)

================================================================
  HƯƠU (Deer)
================================================================

  deer_idle.gif          — Hươu đứng yên
  deer_run.gif           — Hươu đang chạy
  deer_eat.gif           — Hươu đang ăn cỏ
  deer_sleep.gif         — Hươu đang ngủ
  deer.png               — Ảnh tĩnh mặc định

================================================================
  SÓI (Wolf)
================================================================

  wolf_idle.gif          — Sói đứng rình mồi
  wolf_run.gif           — Sói đang đuổi mồi (tăng tốc)
  wolf_eat.gif           — Sói đang ăn thịt
  wolf_sleep.gif         — Sói đang ngủ
  wolf.png               — Ảnh tĩnh mặc định

================================================================
  HỔ (Tiger)
================================================================

  tiger_idle.gif         — Hổ đứng quan sát
  tiger_run.gif          — Hổ đang lao tới tấn công
  tiger_eat.gif          — Hổ đang ăn mồi
  tiger_sleep.gif        — Hổ đang ngủ
  tiger.png              — Ảnh tĩnh mặc định

================================================================
  THỢ SĂN (Hunter)
================================================================

  hunter_idle.gif        — Thợ săn đứng canh gác
  hunter_run.gif         — Thợ săn đang di chuyển
  hunter_eat.gif         — Thợ săn đang nghỉ ăn
  hunter_sleep.gif       — Thợ săn đang ngủ
  hunter.png             — Ảnh tĩnh mặc định

================================================================
  VOI (Elephant)
================================================================

  elephant_idle.gif      — Voi đứng yên
  elephant_run.gif       — Voi đang đi (chậm rãi)
  elephant_eat.gif       — Voi đang ăn cỏ / lá cây
  elephant_sleep.gif     — Voi đang ngủ
  elephant.png           — Ảnh tĩnh mặc định

================================================================
  GHI CHÚ
================================================================

- Kích thước khuyến nghị: 64x64 hoặc 128x128 pixels
- Nền trong suốt (transparent background)
- GIF nên có 4-8 frame để animation mượt
- Nếu chỉ có 1 file duy nhất cho 1 loài (ví dụ: wolf.png),
  hệ thống sẽ dùng ảnh đó cho mọi trạng thái.
- Nếu KHÔNG có file nào → fallback sang hình vẽ vector đẹp
  với hiệu ứng DropShadow (SpriteRenderer).

TỔNG CỘNG: 2 ảnh thực vật + 30 ảnh động vật = 32 file
(Tối thiểu cần: 8 file .png mặc định cho mỗi loài)
