package com.ecosim.view;

import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

/**
 * Camera điều khiển viewport - zoom, pan, và chuyển đổi tọa độ.
 * World coords (tiles) ↔ Screen coords (pixels).
 */
public class Camera {
    /** Vị trí camera trong world coords (tile ở góc trên trái viewport) */
    private double x;
    private double y;

    /** Hệ số zoom (1.0 = 1 tile = TILE_SIZE pixels) */
    private double zoom;

    /** Kích thước viewport (pixels) */
    private double viewportWidth;
    private double viewportHeight;

    /** Giới hạn zoom */
    private static final double MIN_ZOOM = 0.2;
    private static final double MAX_ZOOM = 4.0;

    public Camera(double viewportWidth, double viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.x = 0;
        this.y = 0;
        this.zoom = 1.0;
    }

    // ===== Chuyển đổi tọa độ =====

    /** World coord → Screen coord X */
    public double worldToScreenX(double worldX) {
        return (worldX - x) * getTileScreenSize();
    }

    /** World coord → Screen coord Y */
    public double worldToScreenY(double worldY) {
        return (worldY - y) * getTileScreenSize();
    }

    /** Screen coord → World coord X */
    public double screenToWorldX(double screenX) {
        return screenX / getTileScreenSize() + x;
    }

    /** Screen coord → World coord Y */
    public double screenToWorldY(double screenY) {
        return screenY / getTileScreenSize() + y;
    }

    /** Kích thước 1 tile trên màn hình (pixels) */
    public double getTileScreenSize() {
        return Constants.TILE_SIZE * zoom;
    }

    /** Kích thước entity trên màn hình */
    public double entityScreenSize(double entitySize) {
        return entitySize * getTileScreenSize() * 2;
    }

    // ===== Điều khiển camera =====

    /** Pan (kéo) camera */
    public void pan(double deltaX, double deltaY) {
        this.x -= deltaX / getTileScreenSize();
        this.y -= deltaY / getTileScreenSize();
        clampPosition();
    }

    /** Zoom vào/ra tại vị trí screen */
    public void zoomAt(double screenX, double screenY, double factor) {
        // Lưu world pos dưới con trỏ
        double worldXBefore = screenToWorldX(screenX);
        double worldYBefore = screenToWorldY(screenY);

        // Áp dụng zoom
        zoom *= factor;
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));

        // Điều chỉnh position để giữ nguyên world pos dưới con trỏ
        double worldXAfter = screenToWorldX(screenX);
        double worldYAfter = screenToWorldY(screenY);
        x += (worldXBefore - worldXAfter);
        y += (worldYBefore - worldYAfter);

        clampPosition();
    }

    /** Di chuyển camera đến vị trí (center) */
    public void centerOn(double worldX, double worldY) {
        this.x = worldX - getVisibleWidth() / 2;
        this.y = worldY - getVisibleHeight() / 2;
        clampPosition();
    }

    /** Focus vào một vùng cụ thể */
    public void focusOnRegion(double x1, double y1, double x2, double y2) {
        double regionWidth = x2 - x1;
        double regionHeight = y2 - y1;

        // Tính zoom để fit vùng vào viewport
        double zoomX = viewportWidth / (regionWidth * Constants.TILE_SIZE);
        double zoomY = viewportHeight / (regionHeight * Constants.TILE_SIZE);
        zoom = Math.min(zoomX, zoomY) * 0.9; // 90% để có margin
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));

        centerOn((x1 + x2) / 2, (y1 + y2) / 2);
    }

    /** Preset: Xem toàn bản đồ */
    public void viewFullMap() {
        focusOnRegion(0, 0, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
    }

    /** Preset: Xem Đồng cỏ */
    public void viewGrassland() {
        focusOnRegion(Constants.GRASSLAND_X1, Constants.GRASSLAND_Y1,
                      Constants.GRASSLAND_X2, Constants.GRASSLAND_Y2);
    }

    /** Preset: Xem Rừng rậm */
    public void viewForest() {
        focusOnRegion(Constants.FOREST_X1, Constants.FOREST_Y1,
                      Constants.FOREST_X2, Constants.FOREST_Y2);
    }

    /** Preset: Xem Hồ nước */
    public void viewLake() {
        focusOnRegion(Constants.LAKE_X1, Constants.LAKE_Y1,
                      Constants.LAKE_X2, Constants.LAKE_Y2);
    }

    // ===== Utility =====

    /** Giới hạn camera không vượt ra ngoài bản đồ */
    private void clampPosition() {
        double maxX = Constants.MAP_WIDTH - getVisibleWidth();
        double maxY = Constants.MAP_HEIGHT - getVisibleHeight();
        x = Math.max(0, Math.min(maxX, x));
        y = Math.max(0, Math.min(maxY, y));
    }

    /** Chiều rộng vùng nhìn thấy (world units) */
    public double getVisibleWidth() {
        return viewportWidth / getTileScreenSize();
    }

    /** Chiều cao vùng nhìn thấy (world units) */
    public double getVisibleHeight() {
        return viewportHeight / getTileScreenSize();
    }

    /** Kiểm tra entity có nằm trong viewport không */
    public boolean isVisible(double worldX, double worldY, double margin) {
        double sx = worldToScreenX(worldX);
        double sy = worldToScreenY(worldY);
        return sx >= -margin && sx <= viewportWidth + margin
            && sy >= -margin && sy <= viewportHeight + margin;
    }

    /** Cập nhật kích thước viewport khi resize cửa sổ */
    public void setViewportSize(double width, double height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    // ===== Getters =====
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZoom() { return zoom; }
    public double getViewportWidth() { return viewportWidth; }
    public double getViewportHeight() { return viewportHeight; }
}
