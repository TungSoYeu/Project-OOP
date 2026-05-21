package com.ecosim.engine;

import com.ecosim.model.*;
import com.ecosim.strategy.SurvivalStrategy;

import java.util.List;

/**
 * Engine điều khiển vòng lặp simulation chính.
 * Tách biệt hoàn toàn khỏi View — chỉ xử lý BioLogic.
 *
 * Mỗi tick:
 * 1. Cập nhật nhu cầu sinh tồn
 * 2. Strategy quyết định hành động
 * 3. Thực thi hành động (di chuyển, ăn, tấn công, ...)
 * 4. Xử lý va chạm & nhường đường
 * 5. Xử lý sinh sôi thực vật
 * 6. Dọn dẹp entity đã chết
 */
public class SimulationEngine {
    private final WorldMap worldMap;
    private final EntityManager entityManager;
    private final SeasonManager seasonManager;

    private boolean running;
    private double speedMultiplier;
    private double totalTime;

    /*update*/private Season previousSeason;

    public SimulationEngine() {
        this.worldMap = new WorldMap();
        this.entityManager = new EntityManager(worldMap);
        this.seasonManager = new SeasonManager();
        this.running = false;
        this.speedMultiplier = 1.0;
        this.totalTime = 0;
        /*update*/this.previousSeason = seasonManager.getCurrentSeason();
        // Spawn entities ban đầu
        entityManager.spawnInitialEntities();
    }

    /**
     * Thực thi một tick của simulation.
     * Gọi bởi game loop (AnimationTimer) trong View.
     *
     * @param deltaTime thời gian kể từ tick trước (giây)
     */
    public void tick(double deltaTime) {
        if (!running) return;

        double adjustedDelta = deltaTime * speedMultiplier;
        totalTime += adjustedDelta;

        // ===== 1. Update season =====
        seasonManager.update(adjustedDelta);

        Season currentSeason = seasonManager.getCurrentSeason();

        // ===== 2. Spawn thêm thú vào đầu xuân =====
        if (currentSeason == Season.SPRING &&
            previousSeason != Season.SPRING) {

            // Spawn tự nhiên
            entityManager.spawnSeasonAnimals();

            // Sinh sản
            entityManager.processSpringReproduction();
        }

        // Save previous season
        previousSeason = currentSeason;

        // ===== 3. Maintain population =====
        entityManager.maintainPopulation();

        // ===== 4. Update entities =====
        List<Entity> entities = entityManager.getEntities();

        for (Entity entity : entities) {

            if (!entity.isAlive()) continue;

            // Biological update
            entity.update(adjustedDelta, worldMap);

            // AI update
            if (entity instanceof Animal animal &&
                animal.isAlive()) {

                SurvivalStrategy strategy = animal.getStrategy();

                if (strategy != null) {

                    List<Entity> nearby =
                        entityManager.getNearby(
                            animal,
                            animal.getSightRange()
                        );

                    Action action =
                        strategy.decide(
                            animal,
                            nearby,
                            worldMap
                        );

                    animal.executeAction(
                        action,
                        adjustedDelta,
                        worldMap
                    );
                }
            }
        }

        // ===== 5. Resolve movement =====
        entityManager.resolveMovementPriority();

        // ===== 6. Water drinking =====
        entityManager.processWaterDrinking();

        // ===== 7. Strategy switching =====
        entityManager.checkStrategySwitch();

        // ===== 8. Cleanup =====
        entityManager.cleanup(currentSeason);}

    // ===== Controls =====

    public void start() { running = true; }
    public void pause() { running = false; }
    public void toggle() { running = !running; }
    public boolean isRunning() { return running; }

    public void setSpeedMultiplier(double multiplier) {
        this.speedMultiplier = Math.max(0.25, Math.min(8.0, multiplier));
    }

    public double getSpeedMultiplier() { return speedMultiplier; }

    // ===== User Interactions =====

    /** Gieo mầm cỏ tại vị trí click */
    public void plantGrass(double worldX, double worldY) {
        TerrainType terrain = worldMap.getTerrainAt(worldX, worldY);
        if (terrain == TerrainType.GRASSLAND || terrain == TerrainType.FOREST) {
            entityManager.addEntity(new Grass(new com.ecosim.util.Vector2D(worldX, worldY)));
        }
    }

    /** Đặt vách đá tại vị trí click */
    public void placeRock(int tileX, int tileY) {
        worldMap.setTerrainAt(tileX, tileY, TerrainType.ROCK);
    }

    /** Spawn entity theo loại */
    public void spawnEntity(String type, double worldX, double worldY) {
        com.ecosim.util.Vector2D pos = new com.ecosim.util.Vector2D(worldX, worldY);
        switch (type) {
            case "Thỏ" -> entityManager.addEntity(new Rabbit(pos));
            case "Hươu" -> entityManager.addEntity(new Deer(pos));
            case "Sói" -> entityManager.addEntity(new Wolf(pos));
            case "Hổ" -> entityManager.addEntity(new Tiger(pos));
            case "Thợ săn" -> entityManager.addEntity(new Hunter(pos));
            case "Voi" -> entityManager.addEntity(new Elephant(pos));
            case "Cỏ" -> entityManager.addEntity(new Grass(pos));
            case "Cây ăn quả" -> entityManager.addEntity(new FruitTree(pos));
        }
    }

    // ===== Getters =====
    public WorldMap getWorldMap() { return worldMap; }
    public EntityManager getEntityManager() { return entityManager; }
    public SeasonManager getSeasonManager() { return seasonManager; }
    public double getTotalTime() { return totalTime; }
}
