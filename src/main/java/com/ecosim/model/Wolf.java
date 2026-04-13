package com.ecosim.model;

import com.ecosim.strategy.HunterStrategy;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.List;

/**
 * Sói - động vật ăn thịt, săn đuổi con mồi.
 * Chiến lược: HunterStrategy - quét tìm mục tiêu trong bán kính và tấn công.
 * Đặc biệt: Tăng tốc khi đuổi mồi, KHÔNG vào được bụi rậm.
 */
public class Wolf extends Animal {

    /** Hệ số tăng tốc khi đuổi mồi */
    private final double chaseSpeedMultiplier;

    public Wolf(Vector2D position) {
        super(
            "Sói",                   // name
            position,                 // position
            Constants.PRIORITY_WOLF,  // priority
            0.5,                      // size
            100.0,                    // maxHealth
            Constants.SPEED_WOLF,     // maxSpeed (nhanh nhất)
            25.0,                     // attackPower (mạnh)
            Constants.SIGHT_WOLF      // sightRange (xa nhất)
        );
        this.chaseSpeedMultiplier = 1.5;

        // Kẻ thù: Hổ, Thợ săn, Voi
        this.naturalEnemies = List.of(Tiger.class, Hunter.class, Elephant.class);

        // Con mồi: Thỏ, Hươu
        this.preyTypes = List.of(Rabbit.class, Deer.class);

        // Strategy: săn mồi
        this.defaultStrategy = new HunterStrategy();
        this.strategy = this.defaultStrategy;
    }

    @Override
    protected void doAttack(Entity target, double deltaTime) {
        if (target == null || !target.isAlive()) return;

        double dist = distanceTo(target);
        if (dist < 1.5) {
            super.doAttack(target, deltaTime);
        } else {
            // Tăng tốc đuổi mồi
            double originalSpeed = this.speed;
            this.speed = this.maxSpeed * chaseSpeedMultiplier;
            setState(AnimalState.RUNNING);
            this.speed = originalSpeed;
        }
    }

    @Override
    public double getTerrainSpeedModifier(TerrainType terrain) {
        return switch (terrain) {
            case GRASSLAND -> 1.0;
            case FOREST -> 0.7;
            case BUSH -> 0.0;     // Sói KHÔNG vào bụi rậm được!
            case MUD -> 0.4;
            case WATER, ROCK -> 0.0;
        };
    }

    @Override
    public boolean canTraverse(TerrainType terrain) {
        // Sói không vào bụi rậm (thỏ trốn ở đây) và không vào nước/đá
        return terrain != TerrainType.WATER
            && terrain != TerrainType.ROCK
            && terrain != TerrainType.BUSH;
    }

    @Override
    public String getTypeName() {
        return "Sói";
    }

    public double getChaseSpeedMultiplier() { return chaseSpeedMultiplier; }
}
