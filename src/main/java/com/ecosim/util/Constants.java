package com.ecosim.util;

import javafx.scene.paint.Color;

/**
 * Hằng số cấu hình cho toàn bộ hệ thống mô phỏng.
 */
public final class Constants {

    private Constants() {} // Không cho phép tạo instance

    // ===== World Map =====
    /** Kích thước bản đồ (theo tiles) */
    public static final int MAP_WIDTH = 120;
    public static final int MAP_HEIGHT = 80;

    /** Kích thước mỗi tile (pixels) - dùng cho rendering */
    public static final int TILE_SIZE = 16;

    // ===== Vùng địa lý (tile coordinates) =====
    // Đồng cỏ: phía trên bên trái
    public static final int GRASSLAND_X1 = 0, GRASSLAND_Y1 = 0;
    public static final int GRASSLAND_X2 = 55, GRASSLAND_Y2 = 45;

    // Rừng rậm: phía trên bên phải
    public static final int FOREST_X1 = 55, FOREST_Y1 = 0;
    public static final int FOREST_X2 = 120, FOREST_Y2 = 55;

    // Hồ nước: phía dưới giữa
    public static final int LAKE_X1 = 25, LAKE_Y1 = 50;
    public static final int LAKE_X2 = 75, LAKE_Y2 = 80;

    // ===== Simulation =====
    /** Tốc độ mô phỏng mặc định (ticks/giây) */
    public static final double DEFAULT_TICK_RATE = 60.0;

    /** Thời gian mỗi mùa (giây thực) */
    public static final double SEASON_DURATION_SECONDS = 60.0;

    // ===== Entity defaults =====
    /** Giá trị tối đa cho hunger/thirst */
    public static final double MAX_HUNGER = 200.0;
    public static final double MAX_THIRST = 200.0;
    public static final double MAX_HEALTH = 100.0;

    /** Tốc độ giảm hunger/thirst mỗi giây */
    public static final double HUNGER_DECAY_RATE = 0.25;
    public static final double THIRST_DECAY_RATE = 0.25;

    /** Ngưỡng nguy hiểm */
    public static final double CRITICAL_HUNGER = 20.0;
    public static final double CRITICAL_THIRST = 15.0;

    // ===== Entity priorities (nhường đường) =====
    public static final int PRIORITY_PLANT = 0;
    public static final int PRIORITY_RABBIT = 2;
    public static final int PRIORITY_DEER = 3;
    public static final int PRIORITY_WOLF = 7;
    public static final int PRIORITY_TIGER = 8;
    public static final int PRIORITY_HUNTER = 9;
    public static final int PRIORITY_ELEPHANT = 10;

    // ===== Sight ranges (tiles) =====
    public static final double SIGHT_RABBIT = 8.0;
    public static final double SIGHT_DEER = 10.0;
    public static final double SIGHT_WOLF = 15.0;
    public static final double SIGHT_TIGER = 12.0;
    public static final double SIGHT_HUNTER = 20.0;
    public static final double SIGHT_ELEPHANT = 10.0;

    // ===== Speeds (tiles/giây) =====
    public static final double SPEED_RABBIT = 5.0;
    public static final double SPEED_DEER = 4.5;
    public static final double SPEED_WOLF = 5.5;
    public static final double SPEED_TIGER = 4.0;
    public static final double SPEED_HUNTER = 3.0;
    public static final double SPEED_ELEPHANT = 2.5;

    // ===== BasicRenderer colors =====
    public static final Color COLOR_GRASS = Color.web("#90EE90");
    public static final Color COLOR_FRUIT_TREE = Color.web("#228B22");
    public static final Color COLOR_RABBIT = Color.web("#32CD32");
    public static final Color COLOR_DEER = Color.web("#D2691E");
    public static final Color COLOR_WOLF = Color.web("#808080");
    public static final Color COLOR_TIGER = Color.web("#FF8C00");
    public static final Color COLOR_HUNTER = Color.web("#DC143C");
    public static final Color COLOR_ELEPHANT = Color.web("#A0A0C0");

    // ===== Terrain colors =====
    public static final Color TERRAIN_GRASSLAND = Color.web("#7CCD7C");
    public static final Color TERRAIN_FOREST = Color.web("#2E5E2E");
    public static final Color TERRAIN_WATER = Color.web("#4A90D9");
    public static final Color TERRAIN_MUD = Color.web("#8B7355");
    public static final Color TERRAIN_ROCK = Color.web("#696969");
    public static final Color TERRAIN_BUSH = Color.web("#3A6B35");

    // ===== Reproduction System =====
    /** Probability of reproduction during SPRING (0.0-1.0) */
    public static final double REPRODUCTION_CHANCE = 0.3;
    
    /** Cooldown time between reproductions (seconds) */
    public static final double REPRODUCTION_COOLDOWN = 60.0;
    
    /** Minimum spawn distance from parent (tiles) */
    public static final double OFFSPRING_MIN_DISTANCE = 0.5;
    
    /** Maximum spawn distance from parent (tiles) */
    public static final double OFFSPRING_MAX_DISTANCE = 3.0;
    
    /** Maximum attempts to find valid spawn position */
    public static final int OFFSPRING_SPAWN_RETRIES = 3;
    
    // ===== Population Caps (max animals per species) =====
    public static final int MAX_RABBITS = 50;
    public static final int MAX_DEER = 25;
    public static final int MAX_WOLVES = 15;
    public static final int MAX_TIGERS = 8;
    public static final int MAX_ELEPHANTS = 3;
    public static final int MAX_HUNTERS = 1;

    // ===== Window =====
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final String WINDOW_TITLE = "🌿 Wild-Life Eco Simulation";
}
