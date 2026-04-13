package com.ecosim.util;

/**
 * Lớp biểu diễn vector 2 chiều, dùng cho vị trí và hướng di chuyển.
 * Immutable design - mỗi phép toán trả về Vector2D mới.
 */
public class Vector2D {
    private final double x;
    private final double y;

    public static final Vector2D ZERO = new Vector2D(0, 0);
    public static final Vector2D UP = new Vector2D(0, -1);
    public static final Vector2D DOWN = new Vector2D(0, 1);
    public static final Vector2D LEFT = new Vector2D(-1, 0);
    public static final Vector2D RIGHT = new Vector2D(1, 0);

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    /** Cộng hai vector */
    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    /** Trừ hai vector */
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    /** Nhân vector với scalar */
    public Vector2D multiply(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    /** Độ dài (magnitude) của vector */
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    /** Chuẩn hóa vector (độ dài = 1) */
    public Vector2D normalize() {
        double mag = magnitude();
        if (mag == 0) return ZERO;
        return new Vector2D(x / mag, y / mag);
    }

    /** Khoảng cách từ vector này đến vector khác */
    public double distanceTo(Vector2D other) {
        return this.subtract(other).magnitude();
    }

    /** Hướng từ vector này đến vector khác (normalized) */
    public Vector2D directionTo(Vector2D target) {
        return target.subtract(this).normalize();
    }

    /** Giới hạn magnitude trong phạm vi max */
    public Vector2D clampMagnitude(double maxMagnitude) {
        double mag = magnitude();
        if (mag <= maxMagnitude) return this;
        return normalize().multiply(maxMagnitude);
    }

    /** Tạo vector ngẫu nhiên có magnitude trong khoảng [0, maxMagnitude] */
    public static Vector2D random(double maxMagnitude) {
        double angle = Math.random() * 2 * Math.PI;
        double mag = Math.random() * maxMagnitude;
        return new Vector2D(Math.cos(angle) * mag, Math.sin(angle) * mag);
    }

    /** Tạo vector ngẫu nhiên normalized (magnitude = 1) */
    public static Vector2D randomDirection() {
        double angle = Math.random() * 2 * Math.PI;
        return new Vector2D(Math.cos(angle), Math.sin(angle));
    }

    /** Lấy tile index (ép kiểu int) cho tra cứu terrain */
    public int getTileX() { return (int) Math.floor(x); }
    public int getTileY() { return (int) Math.floor(y); }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vector2D other)) return false;
        return Double.compare(this.x, other.x) == 0
            && Double.compare(this.y, other.y) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }
}
