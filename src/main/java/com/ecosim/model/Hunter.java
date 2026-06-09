package com.ecosim.model;

import com.ecosim.strategy.HunterStrategy;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.List;

/**
 * Thợ săn (Con người) - thực thể đặc biệt.
 * Ưu tiên di chuyển cao (priority = 9).
 * Có thể đặt vật cản (vách đá) trên bản đồ.
 * Tầm nhìn xa nhất, có thể đi trên hầu hết địa hình.
 */
public class Hunter extends Animal {

    /** Tầm vũ khí (tiles) */
    private final double weaponRange;

    /** Số vật cản đã đặt */
    private int obstaclesPlaced;

    /** Giới hạn vật cản */
    private final int maxObstacles;

    public Hunter(Vector2D position) {
        super(
                "Thợ săn", // name
                position, // position
                Constants.PRIORITY_HUNTER, // priority (rất cao)
                0.5, // size
                150.0, // maxHealth (khỏe nhất)
                Constants.SPEED_HUNTER, // maxSpeed (chậm hơn thú)
                40.0, // attackPower (mạnh nhờ vũ khí)
                Constants.SIGHT_HUNTER // sightRange (xa nhất)
        );
        this.weaponRange = 8.0;
        this.obstaclesPlaced = 0;
        this.maxObstacles = 10;

        // Không sợ ai
        this.naturalEnemies = List.of();

        // Săn tất cả động vật (trừ Voi)
        this.preyTypes = List.of(Rabbit.class, Deer.class, Wolf.class, Tiger.class);

        // Strategy: săn mồi
        this.defaultStrategy = new HunterStrategy();
        this.strategy = this.defaultStrategy;
    }

    /**
     * Đặt vật cản (vách đá) tại vị trí chỉ định.
     * 
     * @return true nếu đặt thành công
     */
    public boolean placeObstacle(WorldMap worldMap, Vector2D pos) {
        if (obstaclesPlaced >= maxObstacles)
            return false;

        int tx = pos.getTileX();
        int ty = pos.getTileY();
        TerrainTile tile = worldMap.getTileAt(tx, ty);

        if (tile != null && tile.getType() != TerrainType.ROCK && tile.getType() != TerrainType.WATER) {
            worldMap.setTerrainAt(tx, ty, TerrainType.ROCK);
            obstaclesPlaced++;
            return true;
        }
        return false;
    }

    @Override
    protected void doAttack(Entity target, double deltaTime) {
        if (target == null || !target.isAlive())
            return;

        double dist = distanceTo(target);
        // Thợ săn có thể tấn công từ xa (vũ khí)
        if (dist < weaponRange) {
            setState(AnimalState.ATTACKING);
            if (target instanceof Animal prey) {
                prey.takeDamage(attackPower * deltaTime);
            }
        } else {
            setState(AnimalState.RUNNING);
        }
    }

    @Override
    public double getTerrainSpeedModifier(TerrainType terrain) {
        return switch (terrain) {
            case GRASSLAND -> 1.0;
            case FOREST -> 0.6;
            case BUSH -> 0.5;
            case MUD -> 0.4;
            case WATER, ROCK -> 0.0;
        };
    }

    @Override
    public boolean canTraverse(TerrainType terrain) {
        return terrain != TerrainType.WATER && terrain != TerrainType.ROCK;
    }

    @Override
    public String getTypeName() {
        return "Thợ săn";
    }

    /**
     * Hunters are humans and do not reproduce in this simulation.
     * The population is fixed at 1.
     */
    @Override
    public Animal createOffspring() {
        return null;
    }

    public double getWeaponRange() {
        return weaponRange;
    }

    public int getObstaclesPlaced() {
        return obstaclesPlaced;
    }

    public int getMaxObstacles() {
        return maxObstacles;
    }
}
