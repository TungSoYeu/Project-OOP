package com.ecosim.model;

/**
 * Trạng thái hiện tại của con vật - dùng cho cả logic và animation.
 */
public enum AnimalState {
    /** Đứng yên, không làm gì */
    IDLE("Nghỉ ngơi"),

    /** Di chuyển bình thường */
    WALKING("Đi bộ"),

    /** Chạy (tăng tốc - đuổi mồi hoặc chạy trốn) */
    RUNNING("Chạy"),

    /** Đang ăn */
    EATING("Đang ăn"),

    /** Đang uống nước */
    DRINKING("Uống nước"),

    /** Đang ngủ */
    SLEEPING("Đang ngủ"),

    /** Đang tấn công */
    ATTACKING("Tấn công"),

    /** Đang chạy trốn */
    FLEEING("Chạy trốn"),

    /** Đang trốn (trong bụi rậm, ...) */
    HIDING("Ẩn nấp"),

    /** Đã chết */
    DEAD("Đã chết");

    private final String displayName;

    AnimalState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
