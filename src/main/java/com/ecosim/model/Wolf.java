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
            1000.0,                    // maxHealth
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

        setState(AnimalState.ATTACKING);

        if (target instanceof Animal prey) {

            prey.takeDamage(attackPower * deltaTime);

            // Nếu con mồi chết -> ăn ngay
            if (!prey.isAlive()) {

                // Tăng no bụng
                hunger = Math.min(
                    Constants.MAX_HUNGER,
                    hunger + 40
                );

                // Hồi máu nhẹ
                health = Math.min(
                    maxHealth,
                    health + 10
                );
            }
        }

    } else {
        setState(AnimalState.RUNNING);
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

    /**
     * Create a wolf offspring at a nearby position.
     */
    @Override
    public Animal createOffspring() {
        Vector2D offset = Vector2D.random(Constants.OFFSPRING_MAX_DISTANCE);
        return new Wolf(position.add(offset));
    }

    public double getChaseSpeedMultiplier() { return chaseSpeedMultiplier; }
}
