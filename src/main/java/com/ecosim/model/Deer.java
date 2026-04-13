package com.ecosim.model;

import com.ecosim.strategy.ScaredStrategy;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.List;

/**
 * Hươu - động vật ăn cỏ trung bình.
 * Hiền lành, chạy trốn khi gặp nguy hiểm.
 * Tốc độ nhanh khi chạy trốn (fleeSpeedMultiplier).
 */
public class Deer extends Animal {

    /** Hệ số tăng tốc khi chạy trốn */
    private final double fleeSpeedMultiplier;

    public Deer(Vector2D position) {
        super(
            "Hươu",                  // name
            position,                 // position
            Constants.PRIORITY_DEER,  // priority
            0.5,                      // size (lớn hơn thỏ)
            80.0,                     // maxHealth
            Constants.SPEED_DEER,     // maxSpeed
            8.0,                      // attackPower (yếu, chỉ để tự vệ)
            Constants.SIGHT_DEER      // sightRange
        );
        this.fleeSpeedMultiplier = 1.4;

        // Kẻ thù: Sói, Hổ, Thợ săn
        this.naturalEnemies = List.of(Wolf.class, Tiger.class, Hunter.class);

        // Thức ăn: Cỏ, Cây ăn quả
        this.preyTypes = List.of(Grass.class, FruitTree.class);

        // Strategy mặc định: chạy trốn khi gặp nguy hiểm
        this.defaultStrategy = new ScaredStrategy();
        this.strategy = this.defaultStrategy;
    }

    @Override
    protected void doFlee(Vector2D threatPos, double deltaTime, WorldMap worldMap) {
        // Hươu chạy nhanh hơn bình thường khi hoảng sợ
        double originalSpeed = this.speed;
        this.speed = this.maxSpeed * fleeSpeedMultiplier;
        super.doFlee(threatPos, deltaTime, worldMap);
        this.speed = originalSpeed;
    }

    @Override
    public double getTerrainSpeedModifier(TerrainType terrain) {
        return switch (terrain) {
            case GRASSLAND -> 1.0;
            case FOREST -> 0.6;     // Hươu lớn, khó đi trong rừng
            case BUSH -> 0.4;        // Bụi rậm cản trở nhiều
            case MUD -> 0.3;
            case WATER, ROCK -> 0.0;
        };
    }

    @Override
    public boolean canTraverse(TerrainType terrain) {
        return terrain != TerrainType.WATER && terrain != TerrainType.ROCK;
    }

    @Override
    public String getTypeName() {
        return "Hươu";
    }

    public double getFleeSpeedMultiplier() { return fleeSpeedMultiplier; }
}
