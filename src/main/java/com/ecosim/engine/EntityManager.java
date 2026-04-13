package com.ecosim.engine;

import com.ecosim.model.*;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Quản lý tất cả entity trong simulation.
 * Chịu trách nhiệm spawn, remove, và truy vấn entity theo không gian.
 */
public class EntityManager {
    private final List<Entity> entities;
    private final WorldMap worldMap;
    private final Random random = new Random();

    public EntityManager(WorldMap worldMap) {
        this.worldMap = worldMap;
        this.entities = new CopyOnWriteArrayList<>();
    }

    /**
     * Spawn entities ban đầu cho hệ sinh thái.
     */
    public void spawnInitialEntities() {
        // Cỏ trên đồng cỏ
        for (int i = 0; i < 80; i++) {
            Vector2D pos = randomGrasslandPos();
            addEntity(new Grass(pos));
        }

        // Cây ăn quả trong rừng
        for (int i = 0; i < 20; i++) {
            Vector2D pos = randomForestPos();
            addEntity(new FruitTree(pos));
        }

        // Thỏ (đồng cỏ)
        for (int i = 0; i < 15; i++) {
            addEntity(new Rabbit(randomGrasslandPos()));
        }

        // Hươu (đồng cỏ + rìa rừng)
        for (int i = 0; i < 8; i++) {
            addEntity(new Deer(randomGrasslandPos()));
        }

        // Sói (lang thang khắp nơi trừ hồ)
        for (int i = 0; i < 5; i++) {
            addEntity(new Wolf(randomGrasslandPos()));
        }

        // Hổ (rừng)
        for (int i = 0; i < 3; i++) {
            addEntity(new Tiger(randomForestPos()));
        }

        // Thợ săn (1 con)
        addEntity(new Hunter(new Vector2D(10, 10)));

        // Voi (1-2 con)
        addEntity(new Elephant(randomGrasslandPos()));
        addEntity(new Elephant(randomGrasslandPos()));
    }

    /** Thêm entity vào hệ thống */
    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    /** Xóa entity đã chết và xử lý sinh sôi thực vật */
    public void cleanup(Season currentSeason) {
        // 1. Xóa entity đã chết
        entities.removeIf(e -> !e.isAlive());

        // 2. Xử lý sinh sôi thực vật
        List<Entity> newPlants = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof Plant plant && plant.canSpread()) {
                Vector2D spawnPos = plant.getSpreadPosition();
                // Kiểm tra vị trí hợp lệ
                TerrainType terrain = worldMap.getTerrainAt(spawnPos.getX(), spawnPos.getY());
                if (terrain == TerrainType.GRASSLAND || terrain == TerrainType.FOREST) {
                    // Áp dụng hệ số mùa
                    if (random.nextDouble() < currentSeason.getPlantGrowthMultiplier() * 0.3) {
                        newPlants.add(plant.createOffspring(spawnPos));
                        plant.resetSpreadTimer();
                    }
                }
            }
        }
        entities.addAll(newPlants);

        // 3. Giới hạn số lượng cỏ (tránh lag)
        long grassCount = entities.stream().filter(e -> e instanceof Grass).count();
        if (grassCount > 200) {
            entities.stream()
                .filter(e -> e instanceof Grass)
                .skip(150) // Giữ 150, xóa phần dư
                .forEach(e -> e.setAlive(false));
            entities.removeIf(e -> !e.isAlive());
        }
    }

    /**
     * Lấy danh sách entity trong bán kính từ vị trí cho trước.
     */
    public List<Entity> getNearby(Entity center, double radius) {
        List<Entity> result = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity == center || !entity.isAlive()) continue;
            if (center.distanceTo(entity) <= radius) {
                result.add(entity);
            }
        }
        return result;
    }

    /**
     * Xử lý nhường đường (Priority System).
     * Entity nhỏ hơn phải dạt khi entity lớn đến gần.
     */
    public void resolveMovementPriority() {
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity a = entities.get(i);
                Entity b = entities.get(j);

                if (!a.isAlive() || !b.isAlive()) continue;
                if (!(a instanceof Animal) && !(b instanceof Animal)) continue;

                double dist = a.distanceTo(b);
                double minDist = a.getSize() + b.getSize();

                if (dist < minDist && dist > 0.01) {
                    // Entity có priority thấp hơn phải dạt
                    Entity higher = a.getPriority() >= b.getPriority() ? a : b;
                    Entity lower = a.getPriority() < b.getPriority() ? a : b;

                    // Đẩy entity thấp sang bên
                    Vector2D pushDir = higher.getPosition().directionTo(lower.getPosition());
                    double pushDist = minDist - dist + 0.1;
                    lower.setPosition(lower.getPosition().add(pushDir.multiply(pushDist)));
                }
            }
        }
    }

    /**
     * Kiểm tra và chuyển strategy khi đói quá.
     * Thỏ/Hươu đói → AggressiveStrategy.
     */
    public void checkStrategySwitch() {
        for (Entity entity : entities) {
            if (entity instanceof Rabbit rabbit) {
                if (rabbit.getHunger() < Constants.CRITICAL_HUNGER
                    && !(rabbit.getStrategy() instanceof com.ecosim.strategy.AggressiveStrategy)) {
                    rabbit.setStrategy(new com.ecosim.strategy.AggressiveStrategy());
                }
            } else if (entity instanceof Deer deer) {
                if (deer.getHunger() < Constants.CRITICAL_HUNGER
                    && !(deer.getStrategy() instanceof com.ecosim.strategy.AggressiveStrategy)) {
                    deer.setStrategy(new com.ecosim.strategy.AggressiveStrategy());
                }
            }
        }
    }

    /**
     * Xử lý uống nước khi entity gần nguồn nước.
     */
    public void processWaterDrinking() {
        for (Entity entity : entities) {
            if (entity instanceof Animal animal && animal.isAlive()) {
                TerrainType terrain = worldMap.getTerrainAt(
                    animal.getPosition().getX(), animal.getPosition().getY());

                // Kiểm tra ô xung quanh có nước không
                int tx = animal.getPosition().getTileX();
                int ty = animal.getPosition().getTileY();
                boolean nearWater = false;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (worldMap.getTerrainAt(tx + dx, ty + dy) == TerrainType.WATER) {
                            nearWater = true;
                            break;
                        }
                    }
                    if (nearWater) break;
                }

                if (nearWater && animal.getThirst() < 50 && animal.getState() != AnimalState.FLEEING) {
                    animal.drinkWater();
                }
            }
        }
    }

    // ===== Spawn helpers =====

    private Vector2D randomGrasslandPos() {
        return new Vector2D(
            Constants.GRASSLAND_X1 + random.nextInt(Constants.GRASSLAND_X2 - Constants.GRASSLAND_X1) + 0.5,
            Constants.GRASSLAND_Y1 + random.nextInt(Constants.GRASSLAND_Y2 - Constants.GRASSLAND_Y1) + 0.5
        );
    }

    private Vector2D randomForestPos() {
        return new Vector2D(
            Constants.FOREST_X1 + random.nextInt(Constants.FOREST_X2 - Constants.FOREST_X1) + 0.5,
            Constants.FOREST_Y1 + random.nextInt(Constants.FOREST_Y2 - Constants.FOREST_Y1) + 0.5
        );
    }

    // ===== Getters =====
    public List<Entity> getEntities() { return Collections.unmodifiableList(entities); }

    public long getAnimalCount() {
        return entities.stream().filter(e -> e instanceof Animal && e.isAlive()).count();
    }

    public long getPlantCount() {
        return entities.stream().filter(e -> e instanceof Plant && e.isAlive()).count();
    }

    public Map<String, Long> getEntityCountByType() {
        Map<String, Long> counts = new LinkedHashMap<>();
        entities.stream()
            .filter(Entity::isAlive)
            .forEach(e -> counts.merge(e.getTypeName(), 1L, Long::sum));
        return counts;
    }
}
