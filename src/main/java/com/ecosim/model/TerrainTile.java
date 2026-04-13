package com.ecosim.model;

/**
 * Một ô (tile) trên bản đồ thế giới.
 * Mỗi tile có một loại địa hình và trạng thái riêng.
 */
public class TerrainTile {
    private TerrainType type;
    private boolean hasWaterSource;
    private boolean occupied;

    public TerrainTile(TerrainType type) {
        this.type = type;
        this.hasWaterSource = (type == TerrainType.WATER);
        this.occupied = false;
    }

    public TerrainType getType() { return type; }

    public void setType(TerrainType type) {
        this.type = type;
        this.hasWaterSource = (type == TerrainType.WATER);
    }

    /** Kiểm tra ô này có nguồn nước (dùng cho uống nước) */
    public boolean hasWaterSource() { return hasWaterSource; }

    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }

    /** Kiểm tra có phải vật cản không thể đi qua */
    public boolean isBlocked() {
        return type == TerrainType.ROCK;
    }
}
