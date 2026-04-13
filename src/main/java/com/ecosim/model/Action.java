package com.ecosim.model;

import com.ecosim.util.Vector2D;

/**
 * Hành động được quyết định bởi SurvivalStrategy.
 * Chứa loại hành động và mục tiêu (nếu có).
 */
public class Action {

    /** Các loại hành động */
    public enum Type {
        /** Không làm gì */
        IDLE,
        /** Di chuyển đến vị trí target */
        MOVE_TO,
        /** Ăn entity target */
        EAT,
        /** Uống nước (di chuyển đến nguồn nước gần nhất) */
        DRINK,
        /** Tấn công entity target */
        ATTACK,
        /** Chạy trốn (di chuyển ngược hướng target) */
        FLEE,
        /** Trốn (tìm bụi rậm / rừng gần nhất) */
        HIDE,
        /** Ngủ */
        SLEEP,
        /** Lang thang ngẫu nhiên */
        WANDER
    }

    private final Type type;
    private final Vector2D targetPosition;
    private final Entity targetEntity;

    /** Hành động không cần target */
    public Action(Type type) {
        this(type, null, null);
    }

    /** Hành động hướng đến vị trí */
    public Action(Type type, Vector2D targetPosition) {
        this(type, targetPosition, null);
    }

    /** Hành động hướng đến entity */
    public Action(Type type, Entity targetEntity) {
        this(type, targetEntity != null ? targetEntity.getPosition() : null, targetEntity);
    }

    /** Full constructor */
    public Action(Type type, Vector2D targetPosition, Entity targetEntity) {
        this.type = type;
        this.targetPosition = targetPosition;
        this.targetEntity = targetEntity;
    }

    public Type getType() { return type; }
    public Vector2D getTargetPosition() { return targetPosition; }
    public Entity getTargetEntity() { return targetEntity; }

    // === Factory methods ===

    public static Action idle() { return new Action(Type.IDLE); }
    public static Action wander() { return new Action(Type.WANDER); }
    public static Action sleep() { return new Action(Type.SLEEP); }
    public static Action moveTo(Vector2D pos) { return new Action(Type.MOVE_TO, pos); }
    public static Action eat(Entity food) { return new Action(Type.EAT, food); }
    public static Action drink(Vector2D waterPos) { return new Action(Type.DRINK, waterPos); }
    public static Action attack(Entity prey) { return new Action(Type.ATTACK, prey); }
    public static Action flee(Vector2D awayFrom) { return new Action(Type.FLEE, awayFrom); }
    public static Action hide(Vector2D hideSpot) { return new Action(Type.HIDE, hideSpot); }

    @Override
    public String toString() {
        return "Action{" + type + ", target=" + targetPosition + "}";
    }
}
