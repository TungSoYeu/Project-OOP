package com.ecosim.model;

import com.ecosim.strategy.ScaredStrategy;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.List;

/**
 * Thỏ - động vật ăn cỏ nhỏ, nhanh nhẹn.
 * Chiến lược mặc định: ScaredStrategy (chạy trốn khi gặp kẻ thù).
 * Đặc biệt: Có thể trốn vào bụi rậm (nơi sói không vào được).
 * Khi đói quá có thể chuyển sang AggressiveStrategy.
 */
public class Rabbit extends Animal {

    /** Khả năng trốn vào bụi rậm */
    private final boolean canHideInBush;

    public Rabbit(Vector2D position) {
        super(
            "Thỏ",                    // name
            position,                  // position
            Constants.PRIORITY_RABBIT, // priority
            0.3,                       // size
            60.0,                      // maxHealth
            Constants.SPEED_RABBIT,    // maxSpeed (nhanh)
            5.0,                       // attackPower (yếu)
            Constants.SIGHT_RABBIT     // sightRange
        );
        this.canHideInBush = true;

        // Kẻ thù: Sói, Hổ, Thợ săn
        this.naturalEnemies = List.of(Wolf.class, Tiger.class, Hunter.class);

        // Thức ăn: Cỏ, Cây ăn quả
        this.preyTypes = List.of(Grass.class, FruitTree.class);

        // Strategy mặc định: chạy trốn
        this.defaultStrategy = new ScaredStrategy();
        this.strategy = this.defaultStrategy;
    }

    @Override
    public double getTerrainSpeedModifier(TerrainType terrain) {
        return switch (terrain) {
            case GRASSLAND -> 1.0;
            case FOREST -> 0.8;      // Trong rừng hơi chậm
            case BUSH -> 0.9;         // Trong bụi rậm gần như bình thường (thỏ nhỏ)
            case MUD -> 0.3;
            case WATER, ROCK -> 0.0;  // Không đi được
        };
    }

    @Override
    public boolean canTraverse(TerrainType terrain) {
        return terrain != TerrainType.WATER && terrain != TerrainType.ROCK;
    }

    @Override
    public String getTypeName() {
        return "Thỏ";
    }

    public boolean canHideInBush() { return canHideInBush; }
}
