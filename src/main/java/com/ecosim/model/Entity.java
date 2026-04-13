package com.ecosim.model;

import com.ecosim.util.Vector2D;

import java.util.UUID;

/**
 * Lớp cơ sở trừu tượng cho mọi thực thể trong hệ sinh thái.
 * Cung cấp thuộc tính chung: vị trí, tên, trạng thái sống, priority.
 *
 * Thiết kế cho phép dễ dàng mở rộng thêm loài mới bằng kế thừa.
 */
public abstract class Entity {
    /** ID duy nhất cho mỗi thực thể */
    protected final String id;

    /** Tên hiển thị */
    protected final String name;

    /** Vị trí trên bản đồ (world coordinates) */
    protected Vector2D position;

    /** Còn sống hay đã bị loại bỏ */
    protected boolean alive;

    /**
     * Độ ưu tiên (dùng cho cơ chế nhường đường).
     * Số lớn hơn = ưu tiên cao hơn.
     * Ví dụ: Voi(10) > Thợ săn(9) > Hổ(8) > Sói(7) > Hươu(3) > Thỏ(2) > Cỏ(0)
     */
    protected int priority;

    /** Kích thước hiển thị (radius tính theo tile) */
    protected double size;

    protected Entity(String name, Vector2D position, int priority, double size) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.position = position;
        this.priority = priority;
        this.size = size;
        this.alive = true;
    }

    /**
     * Cập nhật thực thể mỗi tick.
     * @param deltaTime thời gian kể từ tick trước (giây)
     * @param worldMap bản đồ thế giới
     */
    public abstract void update(double deltaTime, WorldMap worldMap);

    /**
     * Lấy hệ số tốc độ khi đi trên loại terrain chỉ định.
     * Mỗi loài có thể override để có hệ số riêng.
     */
    public abstract double getTerrainSpeedModifier(TerrainType terrain);

    /**
     * Kiểm tra loài này có thể đi qua loại terrain chỉ định không.
     * Ví dụ: Sói không vào bụi rậm, hầu hết loài không vào nước.
     */
    public abstract boolean canTraverse(TerrainType terrain);

    /**
     * Lấy tên loại entity (dùng cho hiển thị).
     */
    public abstract String getTypeName();

    // ===== Getters & Setters =====

    public String getId() { return id; }
    public String getName() { return name; }

    public Vector2D getPosition() { return position; }
    public void setPosition(Vector2D position) { this.position = position; }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    public int getPriority() { return priority; }
    public double getSize() { return size; }
    public void setSize(double size) { this.size = size; }

    /** Tính khoảng cách đến entity khác */
    public double distanceTo(Entity other) {
        return this.position.distanceTo(other.position);
    }

    @Override
    public String toString() {
        return String.format("%s[%s] at %s", getTypeName(), id, position);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Entity other)) return false;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
