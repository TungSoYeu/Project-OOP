package com.ecosim.model;

import com.ecosim.strategy.HunterStrategy;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.List;

/**
 * Hổ - động vật ăn thịt mạnh, sống trong rừng rậm.
 * Chiến lược: HunterStrategy nhưng thiên về phục kích (ambush).
 * Đặc biệt: Gầm (roar) khi phát hiện con mồi, mạnh hơn sói nhưng chậm hơn.
 */
public class Tiger extends Animal {

    /** Hệ số sát thương khi phục kích (tấn công từ rừng) */
    private final double ambushMultiplier;

    /** Đang gầm (trigger sound) */
    private boolean roaring;

    public Tiger(Vector2D position) {
        super(
            "Hổ",                    // name
            position,                 // position
            Constants.PRIORITY_TIGER, // priority
            0.7,                      // size (lớn)
            1200.0,                    // maxHealth
            Constants.SPEED_TIGER,    // maxSpeed (chậm hơn sói)
            35.0,                     // attackPower (mạnh nhất)
            Constants.SIGHT_TIGER     // sightRange
        );
        this.ambushMultiplier = 2.0;
        this.roaring = false;

        // Kẻ thù: Thợ săn, Voi
        this.naturalEnemies = List.of(Hunter.class, Elephant.class);

        // Con mồi: Thỏ, Hươu, Sói (hổ ăn cả sói)
        this.preyTypes = List.of(Rabbit.class, Deer.class, Wolf.class);

        // Strategy: săn mồi
        this.defaultStrategy = new HunterStrategy();
        this.strategy = this.defaultStrategy;
    }

    /** Gầm - trigger khi phát hiện con mồi */
    public void roar() {
        this.roaring = true;
        // SoundManager sẽ lắng nghe sự kiện này
    }

    @Override
    public void executeAction(Action action, double deltaTime, WorldMap worldMap) {
        if (action.getType() == Action.Type.ATTACK) {
            doAttack(action.getTargetEntity(), deltaTime, worldMap);
            return;
        }
        super.executeAction(action, deltaTime, worldMap);
    }

    private void doAttack(Entity target, double deltaTime, WorldMap worldMap) {
        if (target == null || !target.isAlive()) return;

        TerrainType currentTerrain = worldMap.getTerrainAt(position.getX(), position.getY());
        double damage = attackPower;
        if (currentTerrain == TerrainType.FOREST) {
            damage *= ambushMultiplier;
        }

        double dist = distanceTo(target);
        if (dist < 2.0) {
            setState(AnimalState.ATTACKING);
            if (target instanceof Animal prey) {
                prey.takeDamage(damage * deltaTime);
            }
        } else {
            setState(AnimalState.RUNNING);
        }
    }

    @Override
    public double getTerrainSpeedModifier(TerrainType terrain) {
        return switch (terrain) {
            case GRASSLAND -> 0.8;  // Hổ thích rừng hơn
            case FOREST -> 1.0;      // Di chuyển nhanh nhất trong rừng
            case BUSH -> 0.0;
            case MUD -> 0.3;
            case WATER, ROCK -> 0.0;
        };
    }

    @Override
    public boolean canTraverse(TerrainType terrain) {
        return terrain != TerrainType.WATER
            && terrain != TerrainType.ROCK
            && terrain != TerrainType.BUSH;
    }

    @Override
    public String getTypeName() {
        return "Hổ";
    }

    /**
     * Create a tiger offspring at a nearby position.
     */
    @Override
    public Animal createOffspring() {
        Vector2D offset = Vector2D.random(Constants.OFFSPRING_MAX_DISTANCE);
        return new Tiger(position.add(offset));
    }

    public boolean isRoaring() {
        boolean was = roaring;
        roaring = false; // Reset sau khi đọc
        return was;
    }

    public double getAmbushMultiplier() { return ambushMultiplier; }
}
