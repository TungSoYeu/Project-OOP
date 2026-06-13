package com.ecosim.engine;

import com.ecosim.model.*;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * EntityManager:
 * - Quản lý entity
 * - Spawn ecosystem
 * - Maintain population
 * - Reproduction
 * - Cleanup
 * - Water drinking
 */
public class EntityManager {

    private final List<Entity> entities;
    private final WorldMap worldMap;
    private final Random random;

    public EntityManager(WorldMap worldMap) {

        this.worldMap = worldMap;
        this.entities = new CopyOnWriteArrayList<>();
        this.random = new Random();
    }

    // =========================================================
    // INITIAL SPAWN
    // =========================================================

    public void spawnInitialEntities() {

        // ===== Plants =====

        for (int i = 0; i < 120; i++) {
            addEntity(new Grass(randomGrasslandPos()));
        }

        for (int i = 0; i < 30; i++) {
            addEntity(new FruitTree(randomForestPos()));
        }

        // ===== Herbivores =====

        for (int i = 0; i < 30; i++) {
            addEntity(new Rabbit(randomGrasslandPos()));
        }

        for (int i = 0; i < 15; i++) {
            addEntity(new Deer(randomGrasslandPos()));
        }

        // ===== Predators =====

        for (int i = 0; i < 5; i++) {
            addEntity(new Wolf(randomGrasslandPos()));
        }

        for (int i = 0; i < 2; i++) {
            addEntity(new Tiger(randomForestPos()));
        }

        // ===== Special =====

        addEntity(new Hunter(new Vector2D(10, 10)));

        addEntity(new Elephant(randomGrasslandPos()));
        addEntity(new Elephant(randomGrasslandPos()));

        // ===== Aquatic =====
        for (int i = 0; i < 15; i++) {
            Vector2D pos = randomWaterPos();
            if (pos != null) addEntity(new Fish(pos));
        }

        for (int i = 0; i < 8; i++) {
            Vector2D pos = randomWaterPos();
            if (pos != null) addEntity(new Duck(pos));
        }
    }

    // =========================================================
    // SPRING SPAWN
    // =========================================================

    public void spawnSeasonAnimals() {

        for (int i = 0; i < 10; i++) {
            addEntity(new Rabbit(randomGrasslandPos()));
        }

        for (int i = 0; i < 5; i++) {
            addEntity(new Deer(randomGrasslandPos()));
        }

        for (int i = 0; i < 2; i++) {
            addEntity(new Wolf(randomGrasslandPos()));
        }

        for (int i = 0; i < 20; i++) {
            addEntity(new Grass(randomGrasslandPos()));
        }
    }

    // =========================================================
    // MAINTAIN POPULATION
    // =========================================================

    public void maintainPopulation() {

        if (countEntities(Rabbit.class) < 25) {

            for (int i = 0; i < 10; i++) {
                addEntity(new Rabbit(randomGrasslandPos()));
            }
        }

        if (countEntities(Deer.class) < 12) {

            for (int i = 0; i < 5; i++) {
                addEntity(new Deer(randomGrasslandPos()));
            }
        }

        if (countEntities(Wolf.class) < 4) {

            for (int i = 0; i < 2; i++) {
                addEntity(new Wolf(randomGrasslandPos()));
            }
        }

        if (countEntities(Grass.class) < 80) {

            for (int i = 0; i < 30; i++) {
                addEntity(new Grass(randomGrasslandPos()));
            }
        }
    }

    // =========================================================
    // REPRODUCTION
    // =========================================================

    public void processSpringReproduction() {

        List<Entity> newborns = new ArrayList<>();

        for (Entity entity : entities) {

            if (!(entity instanceof Animal animal)) {
                continue;
            }

            if (!animal.canReproduce()) {
                continue;
            }

            if (!canSpawnMoreOfSpecies(animal)) {
                continue;
            }

            if (random.nextDouble() < Constants.REPRODUCTION_CHANCE) {

                Animal baby =
                    AnimalReproductionFactory.createOffspring(
                        animal,
                        worldMap
                    );

                if (baby != null) {

                    newborns.add(baby);

                    animal.resetReproductionCooldown();
                }
            }
        }

        entities.addAll(newborns);
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    public void cleanup(Season currentSeason) {

        // Remove dead entities
        entities.removeIf(e -> !e.isAlive());

        // Plant spreading
        List<Entity> newPlants = new ArrayList<>();
        long plantCount =
            entities.stream()
                .filter(e -> e instanceof Plant)
                .count();

        for (Entity entity : entities) {
            if (currentSeason == Season.WINTER &&
                entity instanceof FruitTree &&
                random.nextDouble() < 0.03) {

                entity.setAlive(false);
                continue;
            }

            if (!(entity instanceof Plant plant) || !plant.canSpread()) {
                continue;
            }

            if (plantCount + newPlants.size() >= Constants.MAX_PLANTS) {
                break;
            }

            Vector2D spawnPos =
                plant.getSpreadPosition();

            TerrainType terrain =
                worldMap.getTerrainAt(
                    spawnPos.getX(),
                    spawnPos.getY()
                );

            if (terrain == TerrainType.GRASSLAND ||
                terrain == TerrainType.FOREST) {

                double chance = switch (currentSeason) {
                    case SPRING -> 0.65;
                    case SUMMER -> 0.35;
                    case AUTUMN -> 0.15;
                    case WINTER -> 0.0;
                };

                if (random.nextDouble() < chance) {

                    newPlants.add(
                        plant.createOffspring(spawnPos)
                    );
                }
            }

            plant.resetSpreadTimer();
        }

        entities.addAll(newPlants);
        entities.removeIf(e -> !e.isAlive());   
        // Limit grass
        long grassCount =
            entities.stream()
                .filter(e -> e instanceof Grass)
                .count();   

        if (grassCount > Constants.MAX_GRASS) {

            entities.stream()
                .filter(e -> e instanceof Grass)
                .limit(grassCount - Constants.MAX_GRASS)
                .forEach(e -> e.setAlive(false));

            entities.removeIf(e -> !e.isAlive());
        }
    }

    // =========================================================
    // WATER DRINKING
    // =========================================================

    public void processWaterDrinking() {

        for (Entity entity : entities) {

            if (!(entity instanceof Animal animal)) {
                continue;
            }

            if (!animal.isAlive()) {
                continue;
            }

            int tx = animal.getPosition().getTileX();
            int ty = animal.getPosition().getTileY();

            boolean nearWater = false;

            for (int dy = -1; dy <= 1; dy++) {

                for (int dx = -1; dx <= 1; dx++) {

                    if (worldMap.getTerrainAt(
                        tx + dx,
                        ty + dy
                    ) == TerrainType.WATER) {

                        nearWater = true;
                        break;
                    }
                }

                if (nearWater) break;
            }

            if (nearWater &&
                animal.getThirst() < 80 &&
                animal.getState() != AnimalState.FLEEING) {

                animal.drinkWater();
            }
        }
    }

    // =========================================================
    // STRATEGY SWITCH
    // =========================================================

    public void checkStrategySwitch() {

        for (Entity entity : entities) {

            if (entity instanceof Rabbit rabbit) {

                if (rabbit.getHunger()
                    < Constants.CRITICAL_HUNGER) {

                    if (!(rabbit.getStrategy() instanceof com.ecosim.strategy.AggressiveStrategy)) {
                        rabbit.setStrategy(
                            new com.ecosim.strategy.AggressiveStrategy()
                        );
                    }
                } else {
                    if (!(rabbit.getStrategy() instanceof com.ecosim.strategy.ScaredStrategy)) {
                        rabbit.setStrategy(new com.ecosim.strategy.ScaredStrategy());
                    }
                }

            } else if (entity instanceof Deer deer) {

                if (deer.getHunger()
                    < Constants.CRITICAL_HUNGER) {

                    if (!(deer.getStrategy() instanceof com.ecosim.strategy.AggressiveStrategy)) {
                        deer.setStrategy(
                            new com.ecosim.strategy.AggressiveStrategy()
                        );
                    }
                } else {
                    if (!(deer.getStrategy() instanceof com.ecosim.strategy.ScaredStrategy)) {
                        deer.setStrategy(new com.ecosim.strategy.ScaredStrategy());
                    }
                }
            }
        }
    }

    // =========================================================
    // MOVEMENT PRIORITY & FLOCKING (Separation)
    // =========================================================

    public void resolveMovementPriority() {
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity a = entities.get(i);
                Entity b = entities.get(j);

                if (!a.isAlive() || !b.isAlive()) {
                    continue;
                }

                double dist = a.distanceTo(b);
                double minDist = a.getSize() + b.getSize();

                // Lực đẩy mềm (Soft Separation) khi tới quá gần
                if (dist < minDist * 1.5) {
                    Entity higher = a.getPriority() >= b.getPriority() ? a : b;
                    Entity lower = a.getPriority() < b.getPriority() ? a : b;

                    if (!(lower instanceof Animal lowerAnimal)) {
                        continue;
                    }

                    Vector2D pushDir = dist <= 0.01
                        ? Vector2D.randomDirection()
                        : higher.getPosition().directionTo(lower.getPosition());

                    // Áp dụng lực đẩy (Steering Force) từ từ thay vì dịch chuyển tức thời
                    double force = (minDist * 1.5 - dist) * 0.05; // 0.05 là hệ số mượt (lerp factor)
                    
                    Vector2D newPos = lowerAnimal.getPosition().add(pushDir.multiply(force));
                    TerrainType terrain = worldMap.getTerrainAt(newPos.getX(), newPos.getY());

                    if (worldMap.isInBounds(newPos.getX(), newPos.getY()) && lowerAnimal.canTraverse(terrain)) {
                        lowerAnimal.setPosition(newPos);
                        // Cập nhật hướng xoay nhẹ theo hướng đẩy để trông tự nhiên
                        if (lowerAnimal.getDirection() != null) {
                            Vector2D newDir = lowerAnimal.getDirection().lerp(pushDir, 0.1).normalize();
                            if (newDir.magnitude() > 0) {
                                // lowerAnimal.setDirection(newDir); // Nếu có hàm setDirection
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public List<Entity> getNearby(Entity center, double radius) {

        List<Entity> result = new ArrayList<>();

        for (Entity entity : entities) {

            if (entity == center) continue;

            if (!entity.isAlive()) continue;

            if (center.distanceTo(entity) <= radius) {
                result.add(entity);
            }
        }

        return result;
    }

    public int countEntities(Class<?> clazz) {

        int count = 0;

        for (Entity entity : entities) {

            if (clazz.isInstance(entity)
                && entity.isAlive()) {

                count++;
            }
        }

        return count;
    }

    private boolean canSpawnMoreOfSpecies(
        Animal animal
    ) {

        Class<?> animalClass =
            animal.getClass();

        long currentCount =
            entities.stream()
                .filter(e ->
                    e.getClass() == animalClass)
                .count();

        int maxAllowed =
            getMaxPopulationForSpecies(
                animalClass
            );

        return currentCount < maxAllowed;
    }

    private int getMaxPopulationForSpecies(
        Class<?> animalClass
    ) {

        return switch (
            animalClass.getSimpleName()
        ) {

            case "Rabbit" -> Constants.MAX_RABBITS;
            case "Deer" -> Constants.MAX_DEER;
            case "Wolf" -> Constants.MAX_WOLVES;
            case "Tiger" -> Constants.MAX_TIGERS;
            case "Elephant" -> Constants.MAX_ELEPHANTS;
            case "Hunter" -> Constants.MAX_HUNTERS;

            default -> 0;
        };
    }

    private Vector2D randomGrasslandPos() {

        return new Vector2D(
            Constants.GRASSLAND_X1
                + random.nextInt(
                    Constants.GRASSLAND_X2
                    - Constants.GRASSLAND_X1
                ) + 0.5,

            Constants.GRASSLAND_Y1
                + random.nextInt(
                    Constants.GRASSLAND_Y2
                    - Constants.GRASSLAND_Y1
                ) + 0.5
        );
    }

    private Vector2D randomForestPos() {

        return new Vector2D(
            Constants.FOREST_X1
                + random.nextInt(
                    Constants.FOREST_X2
                    - Constants.FOREST_X1
                ) + 0.5,

            Constants.FOREST_Y1
                + random.nextInt(
                    Constants.FOREST_Y2
                    - Constants.FOREST_Y1
                ) + 0.5
        );
    }

    private Vector2D randomWaterPos() {
        for (int i = 0; i < 100; i++) {
            int rx = random.nextInt(Constants.MAP_WIDTH);
            int ry = random.nextInt(Constants.MAP_HEIGHT);
            if (worldMap.getTerrainAt(rx, ry) == TerrainType.WATER || worldMap.getTerrainAt(rx, ry) == TerrainType.MUD) {
                return new Vector2D(rx + 0.5, ry + 0.5);
            }
        }
        return null;
    }
    // =========================================================
    // GETTERS
    // =========================================================

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public long getAnimalCount() {

        return entities.stream()
            .filter(e ->
                e instanceof Animal
                && e.isAlive())
            .count();
    }

    public long getPlantCount() {

        return entities.stream()
            .filter(e ->
                e instanceof Plant
                && e.isAlive())
            .count();
    }

    public Map<String, Long> getEntityCountByType() {

        Map<String, Long> counts =
            new LinkedHashMap<>();

        entities.stream()
            .filter(Entity::isAlive)
            .forEach(e ->
                counts.merge(
                    e.getTypeName(),
                    1L,
                    Long::sum
                )
            );

        return counts;
    }

    public void clearAll() {
        entities.clear();
    }
}
