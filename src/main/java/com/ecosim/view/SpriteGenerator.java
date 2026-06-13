package com.ecosim.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 * Tạo ảnh sprite pixel-art cho các entity bằng code Java.
 * Dùng khi không có file ảnh bên ngoài.
 * Kích thước: 64x64 pixels, nền trong suốt.
 */
public class SpriteGenerator {

    private static final int SIZE = 64;

    /** Tạo toàn bộ sprite mặc định và đăng ký vào AssetManager */
    public static void generateAll() {
        AssetManager mgr = AssetManager.getInstance();
        
        mgr.registerGenerated("rabbit.png", generateRabbit());
        mgr.registerGenerated("deer.png", generateDeer());
        mgr.registerGenerated("wolf.png", generateWolf());
        mgr.registerGenerated("tiger.png", generateTiger());
        mgr.registerGenerated("hunter.png", generateHunter());
        mgr.registerGenerated("elephant.png", generateElephant());
        mgr.registerGenerated("grass.png", generateGrass());
        mgr.registerGenerated("fruittree.png", generateFruitTree());

        System.out.println("[SpriteGenerator] ✅ Đã tạo 8 sprite pixel-art");
    }

    // ===== THỎ =====
    private static Image generateRabbit() {
        Canvas c = new Canvas(SIZE, SIZE);
        GraphicsContext g = c.getGraphicsContext2D();

        // Thân - trắng xám
        g.setFill(Color.web("#E8E8E8"));
        g.fillOval(16, 22, 32, 26);

        // Đầu
        g.setFill(Color.web("#F0F0F0"));
        g.fillOval(30, 14, 22, 20);

        // Tai trái
        g.setFill(Color.web("#E0E0E0"));
        g.fillOval(33, 0, 7, 18);
        g.setFill(Color.web("#FFB6C1"));
        g.fillOval(34.5, 3, 4, 12);

        // Tai phải
        g.setFill(Color.web("#E0E0E0"));
        g.fillOval(42, 2, 7, 16);
        g.setFill(Color.web("#FFB6C1"));
        g.fillOval(43.5, 5, 4, 10);

        // Mắt
        g.setFill(Color.web("#2c1810"));
        g.fillOval(42, 20, 5, 5);
        g.setFill(Color.WHITE);
        g.fillOval(43, 20.5, 2, 2);

        // Mũi hồng
        g.setFill(Color.web("#FF69B4"));
        g.fillOval(49, 26, 4, 3);

        // Đuôi tròn
        g.setFill(Color.WHITE);
        g.fillOval(12, 30, 10, 10);

        // Chân trước
        g.setFill(Color.web("#D0D0D0"));
        g.fillOval(36, 42, 6, 8);
        g.fillOval(44, 42, 6, 8);

        // Chân sau
        g.fillOval(18, 40, 8, 10);

        return snapshot(c);
    }

    // ===== HƯƠU =====
    private static Image generateDeer() {
        Canvas c = new Canvas(SIZE, SIZE);
        GraphicsContext g = c.getGraphicsContext2D();

        // Thân nâu
        g.setFill(Color.web("#8B6C42"));
        g.fillOval(10, 24, 38, 22);

        // Bụng sáng
        g.setFill(Color.web("#D4B896"));
        g.fillOval(14, 32, 30, 12);

        // Đầu
        g.setFill(Color.web("#9A7B52"));
        g.fillOval(38, 12, 18, 18);

        // Sừng (antlers)
        g.setStroke(Color.web("#5C4033"));
        g.setLineWidth(2.5);
        g.setLineCap(StrokeLineCap.ROUND);
        g.strokeLine(43, 12, 38, 2);
        g.strokeLine(38, 2, 33, 6);
        g.strokeLine(38, 2, 35, -2);
        g.strokeLine(50, 14, 55, 4);
        g.strokeLine(55, 4, 60, 8);
        g.strokeLine(55, 4, 58, 0);

        // Mắt
        g.setFill(Color.web("#1a0f0a"));
        g.fillOval(49, 18, 4, 4);
        g.setFill(Color.WHITE);
        g.fillOval(50, 18.5, 1.5, 1.5);

        // Mũi
        g.setFill(Color.web("#3d2b1f"));
        g.fillOval(53, 24, 4, 3);

        // Chân
        g.setStroke(Color.web("#5C4033"));
        g.setLineWidth(3);
        g.strokeLine(20, 44, 18, 58);
        g.strokeLine(32, 44, 30, 58);
        g.strokeLine(38, 44, 40, 58);

        // Móng
        g.setFill(Color.web("#2d1f14"));
        g.fillOval(16, 56, 5, 5);
        g.fillOval(28, 56, 5, 5);
        g.fillOval(38, 56, 5, 5);

        return snapshot(c);
    }

    // ===== SÓI =====
    private static Image generateWolf() {
        Canvas c = new Canvas(SIZE, SIZE);
        GraphicsContext g = c.getGraphicsContext2D();

        // Thân xám
        g.setFill(Color.web("#6B7B8D"));
        g.fillOval(8, 22, 40, 22);

        // Bụng sáng hơn
        g.setFill(Color.web("#9AACBD"));
        g.fillOval(14, 32, 28, 10);

        // Đầu
        g.setFill(Color.web("#7A8B9D"));
        g.fillOval(36, 10, 22, 22);

        // Mõm dài
        g.setFill(Color.web("#8A9BAD"));
        double[] muzzleX = {52, 62, 52};
        double[] muzzleY = {22, 26, 30};
        g.fillPolygon(muzzleX, muzzleY, 3);

        // Tai nhọn trái
        g.setFill(Color.web("#4A5A6A"));
        double[] earLX = {40, 36, 44};
        double[] earLY = {12, 0, 8};
        g.fillPolygon(earLX, earLY, 3);

        // Tai nhọn phải
        double[] earRX = {50, 46, 54};
        double[] earRY = {12, 2, 8};
        g.fillPolygon(earRX, earRY, 3);

        // Mắt hung dữ (vàng)
        g.setFill(Color.web("#FFD700"));
        g.fillOval(48, 17, 5, 4);
        g.setFill(Color.BLACK);
        g.fillOval(49.5, 17.5, 2.5, 3);

        // Mũi
        g.setFill(Color.web("#1a1a1a"));
        g.fillOval(59, 24, 4, 3);

        // Đuôi dài xoắn
        g.setStroke(Color.web("#5A6A7A"));
        g.setLineWidth(4);
        g.setLineCap(StrokeLineCap.ROUND);
        g.strokeLine(8, 28, 2, 20);

        // Chân
        g.setStroke(Color.web("#4A5A6A"));
        g.setLineWidth(3);
        g.strokeLine(18, 42, 16, 56);
        g.strokeLine(30, 42, 28, 56);
        g.strokeLine(40, 42, 42, 56);

        return snapshot(c);
    }

    // ===== HỔ =====
    private static Image generateTiger() {
        Canvas c = new Canvas(SIZE, SIZE);
        GraphicsContext g = c.getGraphicsContext2D();

        // Thân cam
        g.setFill(Color.web("#E8820C"));
        g.fillOval(6, 20, 44, 24);

        // Bụng trắng
        g.setFill(Color.web("#F5DEB3"));
        g.fillOval(14, 32, 28, 10);

        // Vằn đen trên thân
        g.setStroke(Color.web("#1a1a1a"));
        g.setLineWidth(2.5);
        for (int i = 0; i < 5; i++) {
            double x = 16 + i * 7;
            g.strokeLine(x, 22, x + 3, 38);
        }

        // Đầu
        g.setFill(Color.web("#F0A030"));
        g.fillOval(38, 10, 22, 22);

        // Vằn trên mặt
        g.setStroke(Color.web("#1a1a1a"));
        g.setLineWidth(1.5);
        g.strokeLine(44, 12, 46, 18);
        g.strokeLine(52, 12, 54, 18);

        // Tai tròn
        g.setFill(Color.web("#D08010"));
        g.fillOval(39, 8, 8, 8);
        g.fillOval(52, 8, 8, 8);
        g.setFill(Color.web("#FFD0A0"));
        g.fillOval(41, 10, 4, 4);
        g.fillOval(54, 10, 4, 4);

        // Mắt (xanh lá)
        g.setFill(Color.web("#90EE90"));
        g.fillOval(47, 18, 6, 5);
        g.setFill(Color.BLACK);
        g.fillOval(49, 19, 3, 3);

        // Mũi
        g.setFill(Color.web("#FF6B6B"));
        g.fillOval(55, 24, 5, 4);

        // Miệng
        g.setStroke(Color.web("#1a1a1a"));
        g.setLineWidth(1);
        g.strokeLine(57, 28, 60, 30);
        g.strokeLine(57, 28, 54, 30);

        // Chân
        g.setStroke(Color.web("#C07000"));
        g.setLineWidth(4);
        g.strokeLine(16, 42, 14, 56);
        g.strokeLine(28, 42, 26, 56);
        g.strokeLine(40, 42, 42, 56);

        // Đuôi dài
        g.setStroke(Color.web("#E8820C"));
        g.setLineWidth(3.5);
        g.setLineCap(StrokeLineCap.ROUND);
        g.strokeLine(6, 28, 0, 18);
        g.strokeLine(0, 18, 4, 14);

        return snapshot(c);
    }

    // ===== THỢ SĂN =====
    private static Image generateHunter() {
        Canvas c = new Canvas(SIZE, SIZE);
        GraphicsContext g = c.getGraphicsContext2D();

        // Chân
        g.setFill(Color.web("#2F4F4F"));
        g.fillRect(22, 46, 7, 14);
        g.fillRect(34, 46, 7, 14);

        // Giày
        g.setFill(Color.web("#4A3728"));
        g.fillRoundRect(20, 56, 10, 6, 3, 3);
        g.fillRoundRect(32, 56, 10, 6, 3, 3);

        // Thân - áo khoác xanh rêu
        g.setFill(Color.web("#556B2F"));
        g.fillRoundRect(18, 26, 28, 22, 6, 6);

        // Thắt lưng
        g.setFill(Color.web("#5C4033"));
        g.fillRect(18, 42, 28, 4);
        g.setFill(Color.web("#FFD700"));
        g.fillRect(29, 42, 6, 4);

        // Tay
        g.setFill(Color.web("#556B2F"));
        g.fillOval(10, 28, 10, 16);
        g.fillOval(44, 28, 10, 16);

        // Da tay
        g.setFill(Color.web("#F0C8A0"));
        g.fillOval(12, 40, 6, 6);
        g.fillOval(46, 40, 6, 6);

        // Đầu
        g.setFill(Color.web("#F0C8A0"));
        g.fillOval(22, 8, 20, 20);

        // Mũ nồi
        g.setFill(Color.web("#4A6741"));
        g.fillOval(20, 4, 24, 12);
        g.fillRect(18, 10, 28, 4);

        // Viền mũ
        g.setFill(Color.web("#3A5731"));
        g.fillRect(16, 12, 32, 3);

        // Mắt
        g.setFill(Color.web("#2c1810"));
        g.fillOval(30, 16, 4, 4);
        g.setFill(Color.WHITE);
        g.fillOval(31, 16.5, 1.5, 1.5);

        // Mũi
        g.setFill(Color.web("#D4A87A"));
        g.fillOval(34, 20, 3, 3);

        // Súng (Bên phải)
        g.setStroke(Color.web("#444444"));
        g.setLineWidth(3);
        g.setLineCap(StrokeLineCap.ROUND);
        g.strokeLine(50, 34, 62, 10);
        g.setFill(Color.web("#5C4033"));
        g.fillRoundRect(48, 34, 6, 12, 2, 2);

        return snapshot(c);
    }

    // ===== VOI =====
    private static Image generateElephant() {
        Canvas c = new Canvas(SIZE, SIZE);
        GraphicsContext g = c.getGraphicsContext2D();

        // Thân to tròn - xám xanh
        g.setFill(Color.web("#8B98A8"));
        g.fillOval(6, 16, 44, 32);

        // Bụng sáng hơn
        g.setFill(Color.web("#A0ADBD"));
        g.fillOval(14, 34, 28, 12);

        // Đầu
        g.setFill(Color.web("#7A8898"));
        g.fillOval(36, 6, 24, 28);

        // Tai to (trái)
        g.setFill(Color.web("#6A7888"));
        g.fillOval(28, 8, 14, 22);
        g.setFill(Color.web("#B0A0A8"));
        g.fillOval(31, 12, 8, 14);

        // Vòi dài cong
        g.setStroke(Color.web("#8B98A8"));
        g.setLineWidth(5);
        g.setLineCap(StrokeLineCap.ROUND);
        g.strokeLine(54, 26, 58, 38);
        g.strokeLine(58, 38, 54, 48);
        g.strokeLine(54, 48, 50, 52);

        // Ngà
        g.setStroke(Color.web("#FFFFF0"));
        g.setLineWidth(2.5);
        g.strokeLine(50, 28, 56, 36);

        // Mắt nhỏ
        g.setFill(Color.web("#1a1a1a"));
        g.fillOval(50, 16, 4, 4);
        g.setFill(Color.WHITE);
        g.fillOval(51, 16.5, 1.5, 1.5);

        // Chân to
        g.setFill(Color.web("#6A7888"));
        g.fillRoundRect(12, 44, 10, 14, 4, 4);
        g.fillRoundRect(26, 44, 10, 14, 4, 4);
        g.fillRoundRect(38, 44, 10, 14, 4, 4);

        // Móng
        g.setFill(Color.web("#C0C0C0"));
        g.fillOval(13, 54, 3, 3);
        g.fillOval(17, 54, 3, 3);
        g.fillOval(27, 54, 3, 3);
        g.fillOval(31, 54, 3, 3);

        // Đuôi ngắn
        g.setStroke(Color.web("#6A7888"));
        g.setLineWidth(2);
        g.strokeLine(6, 24, 2, 18);
        g.setFill(Color.web("#5A6878"));
        g.fillOval(0, 14, 4, 6);

        return snapshot(c);
    }

    // ===== CỎ =====
    private static Image generateGrass() {
        Canvas c = new Canvas(SIZE, SIZE);
        GraphicsContext g = c.getGraphicsContext2D();

        g.setLineCap(StrokeLineCap.ROUND);

        // Nhiều cọng cỏ xanh
        Color[] greens = {
            Color.web("#228B22"), Color.web("#32CD32"), Color.web("#006400"),
            Color.web("#3CB371"), Color.web("#2E8B57"), Color.web("#90EE90")
        };

        for (int i = 0; i < 12; i++) {
            double x = 8 + (i % 6) * 8 + Math.random() * 4;
            double baseY = 54 + Math.random() * 6;
            double tipX = x + (Math.random() - 0.5) * 12;
            double tipY = 10 + Math.random() * 20;

            g.setStroke(greens[i % greens.length]);
            g.setLineWidth(2 + Math.random());
            g.strokeLine(x, baseY, tipX, tipY);
        }

        return snapshot(c);
    }

    // ===== CÂY ĂN QUẢ =====
    private static Image generateFruitTree() {
        Canvas c = new Canvas(SIZE, SIZE);
        GraphicsContext g = c.getGraphicsContext2D();

        // Thân cây nâu
        g.setFill(Color.web("#5C4033"));
        g.fillRoundRect(26, 32, 12, 28, 4, 4);

        // Nhánh cây
        g.setStroke(Color.web("#5C4033"));
        g.setLineWidth(3);
        g.setLineCap(StrokeLineCap.ROUND);
        g.strokeLine(32, 38, 18, 28);
        g.strokeLine(32, 36, 46, 26);

        // Tán lá tròn (nhiều lớp)
        g.setFill(Color.web("#1B5E20"));
        g.fillOval(8, 6, 24, 24);
        g.setFill(Color.web("#2E7D32"));
        g.fillOval(22, 2, 26, 26);
        g.setFill(Color.web("#388E3C"));
        g.fillOval(14, 10, 28, 24);

        // Quả đỏ
        g.setFill(Color.web("#E53935"));
        g.fillOval(18, 16, 6, 6);
        g.fillOval(30, 12, 6, 6);
        g.fillOval(38, 18, 6, 6);
        g.fillOval(24, 24, 5, 5);

        // Highlight quả
        g.setFill(Color.rgb(255, 255, 255, 0.4));
        g.fillOval(19, 16.5, 2, 2);
        g.fillOval(31, 12.5, 2, 2);
        g.fillOval(39, 18.5, 2, 2);

        return snapshot(c);
    }

    /** Snapshot Canvas thành Image (nền trong suốt) */
    private static Image snapshot(Canvas canvas) {
        WritableImage image = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        canvas.snapshot(params, image);
        return image;
    }
}
