package com.ecosim.model;

import com.ecosim.strategy.PassiveStrategy;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.List;

/**
 * Cá - Động vật sống dưới nước.
 */
public class Fish extends Animal {

    public Fish(Vector2D position) {
        super(
            "Cá",
            position,
            Constants.PRIORITY_FISH,
            0.8, // Size to để dễ nhìn trong hồ nước
            200.0, // maxHealth
            Constants.SPEED_FISH,
            0.0, // attackPower
            Constants.SIGHT_FISH
        );

        this.naturalEnemies = List.of(Duck.class, Hunter.class);
        
        // Thức ăn là thực vật phù du, coi như không cần săn, cứ bơi (Wander)
        this.preyTypes = List.of(); 

        this.defaultStrategy = new PassiveStrategy();
        this.strategy = this.defaultStrategy;
    }

    @Override
    public double getTerrainSpeedModifier(TerrainType terrain) {
        return switch (terrain) {
            case WATER -> 1.5;
            case MUD -> 0.5;
            case GRASSLAND, FOREST, BUSH, ROCK -> 0.0;
        };
    }

    @Override
    public boolean canTraverse(TerrainType terrain) {
        return terrain == TerrainType.WATER || terrain == TerrainType.MUD;
    }

    @Override
    public Animal createOffspring() {
        return new Fish(this.position.add(Vector2D.random(1.0)));
    }

    @Override
    public String getTypeName() {
        return "Cá";
    }
}
