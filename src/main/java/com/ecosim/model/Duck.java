package com.ecosim.model;

import com.ecosim.strategy.PassiveStrategy;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.List;

/**
 * Vịt - Động vật sống cả trên bờ và dưới nước.
 */
public class Duck extends Animal {

    public Duck(Vector2D position) {
        super(
            "Vịt",
            position,
            Constants.PRIORITY_DUCK,
            0.8, // Size to để nổi bật
            300.0, // maxHealth
            Constants.SPEED_DUCK,
            2.0, // attackPower
            Constants.SIGHT_DUCK
        );

        // Kẻ thù của vịt
        this.naturalEnemies = List.of(Wolf.class, Tiger.class, Hunter.class);
        
        // Thức ăn là cá, cỏ non
        this.preyTypes = List.of(Fish.class, Grass.class); 

        this.defaultStrategy = new PassiveStrategy();
        this.strategy = this.defaultStrategy;
    }

    @Override
    public double getTerrainSpeedModifier(TerrainType terrain) {
        return switch (terrain) {
            case WATER -> 1.2; // Bơi nhanh
            case MUD -> 0.8;
            case GRASSLAND -> 0.6; // Lạch bạch trên cạn
            case FOREST -> 0.3;
            case BUSH -> 0.2;
            case ROCK -> 0.0;
        };
    }

    @Override
    public boolean canTraverse(TerrainType terrain) {
        return terrain == TerrainType.WATER || terrain == TerrainType.MUD;
    }

    @Override
    public Animal createOffspring() {
        return new Duck(this.position.add(Vector2D.random(1.0)));
    }

    @Override
    public String getTypeName() {
        return "Vịt";
    }
}
