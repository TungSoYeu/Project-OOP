package com.ecosim.model;

import com.ecosim.util.Constants;

/**
 * Loại địa hình trên bản đồ.
 * Mỗi loại có hệ số tốc độ di chuyển và khả năng đi qua.
 */
public enum TerrainType {
    /** Đồng cỏ - địa hình mặc định, tốc độ bình thường */
    GRASSLAND(1.0, true, Constants.TERRAIN_GRASSLAND, "Đồng cỏ"),

    /** Rừng rậm - tốc độ chậm hơn, nơi trú ẩn cho thỏ */
    FOREST(0.7, true, Constants.TERRAIN_FOREST, "Rừng rậm"),

    /** Hồ nước - chỉ một số loài đi qua được */
    WATER(0.3, false, Constants.TERRAIN_WATER, "Hồ nước"),

    /** Bùn - tốc độ rất chậm */
    MUD(0.4, true, Constants.TERRAIN_MUD, "Bùn"),

    /** Vách đá - không thể đi qua (vật cản) */
    ROCK(0.0, false, Constants.TERRAIN_ROCK, "Vách đá"),

    /** Bụi rậm - chậm nhưng thỏ trốn được, sói không vào */
    BUSH(0.5, true, Constants.TERRAIN_BUSH, "Bụi rậm");

    private final double speedModifier;
    private final boolean defaultTraversable;
    private final String hexColor;
    private final String displayName;

    TerrainType(double speedModifier, boolean defaultTraversable, String hexColor, String displayName) {
        this.speedModifier = speedModifier;
        this.defaultTraversable = defaultTraversable;
        this.hexColor = hexColor;
        this.displayName = displayName;
    }

    /** Hệ số tốc độ khi đi trên loại địa hình này (1.0 = 100%) */
    public double getSpeedModifier() { return speedModifier; }

    /** Mặc định có thể đi qua hay không (loài cụ thể override) */
    public boolean isDefaultTraversable() { return defaultTraversable; }

    /** Màu hiển thị trên bản đồ (Hex string) */
    public String getHexColor() { return hexColor; }

    /** Tên hiển thị tiếng Việt */
    public String getDisplayName() { return displayName; }
}
