package com.ecosim.engine;

import com.ecosim.model.Deer;
import com.ecosim.model.DamageEvent;
import com.ecosim.model.Elephant;
import com.ecosim.model.FruitTree;
import com.ecosim.model.Grass;
import com.ecosim.model.Rabbit;
import com.ecosim.model.Season;
import com.ecosim.model.TerrainType;
import com.ecosim.model.WorldMap;
import com.ecosim.strategy.AggressiveStrategy;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityManagerTest {

    @Test
    void hungryDeerSwitchesToAggressiveStrategy() {
        EntityManager entityManager = new EntityManager(new WorldMap());
        Deer deer = new Deer(new Vector2D(10.5, 10.5));
        deer.setHunger(Constants.CRITICAL_HUNGER - 1);
        entityManager.addEntity(deer);

        entityManager.checkStrategySwitch();

        assertInstanceOf(AggressiveStrategy.class, deer.getStrategy());
    }

    @Test
    void fruitTreeIsNotMovedByAnimalCollision() {
        WorldMap worldMap = new WorldMap();
        EntityManager entityManager = new EntityManager(worldMap);
        FruitTree tree = new FruitTree(new Vector2D(60.5, 10.5));
        Elephant elephant = new Elephant(new Vector2D(60.6, 10.5));
        Vector2D treePosition = tree.getPosition();

        entityManager.addEntity(tree);
        entityManager.addEntity(elephant);
        entityManager.resolveMovementPriority();

        assertEquals(treePosition, tree.getPosition());
        assertTrue(elephant.getPosition().distanceTo(treePosition) > 0.1);
    }

    @Test
    void rabbitCanPassThroughGrassWithoutMovingIt() {
        EntityManager entityManager = new EntityManager(new WorldMap());
        Grass grass = new Grass(new Vector2D(10.5, 10.5));
        Rabbit rabbit = new Rabbit(new Vector2D(10.6, 10.5));
        Vector2D grassPosition = grass.getPosition();
        Vector2D rabbitPosition = rabbit.getPosition();

        entityManager.addEntity(grass);
        entityManager.addEntity(rabbit);
        entityManager.resolveMovementPriority();

        assertEquals(grassPosition, grass.getPosition());
        assertEquals(rabbitPosition, rabbit.getPosition());
    }

    @Test
    void onlyRabbitCanTraverseBushTerrain() {
        assertTrue(new Rabbit(new Vector2D(10.5, 10.5)).canTraverse(TerrainType.BUSH));
        assertFalse(new Deer(new Vector2D(10.5, 10.5)).canTraverse(TerrainType.BUSH));
        assertFalse(new Elephant(new Vector2D(10.5, 10.5)).canTraverse(TerrainType.BUSH));
    }

    @Test
    void fruitTreesRequireSpacingWhenSpawned() {
        WorldMap worldMap = new WorldMap();
        worldMap.setTerrainAt(60, 10, TerrainType.FOREST);
        worldMap.setTerrainAt(61, 10, TerrainType.FOREST);
        EntityManager entityManager = new EntityManager(worldMap);

        assertTrue(entityManager.addFruitTreeAt(new Vector2D(60.5, 10.5)));
        assertFalse(entityManager.addFruitTreeAt(new Vector2D(61.5, 10.5)));
    }

    @Test
    void grassCanBePlantedInForest() {
        WorldMap worldMap = new WorldMap();
        worldMap.setTerrainAt(60, 10, TerrainType.FOREST);
        EntityManager entityManager = new EntityManager(worldMap);

        assertTrue(entityManager.addGrassAt(new Vector2D(60.5, 10.5)));
    }

    @Test
    void grassToolCanPlantNearAnimals() {
        EntityManager entityManager = new EntityManager(new WorldMap());
        entityManager.addEntity(new Deer(new Vector2D(10.5, 10.5)));

        assertTrue(entityManager.addGrassAt(new Vector2D(10.6, 10.5)));
    }

    @Test
    void cleanupDoesNotDeleteExistingGrassCap() {
        EntityManager entityManager = new EntityManager(new WorldMap());

        for (int i = 0; i < 100; i++) {
            entityManager.addEntity(new Grass(new Vector2D(i % 50 + 0.5, i / 50 + 0.5)));
        }

        entityManager.cleanup(Season.SPRING);

        assertEquals(100, entityManager.countEntities(Grass.class));
    }

    @Test
    void plantedGrassSurvivesCleanupWhileRunning() {
        EntityManager entityManager = new EntityManager(new WorldMap());

        assertTrue(entityManager.addGrassAt(new Vector2D(10.5, 10.5)));
        entityManager.cleanup(Season.SPRING);

        assertEquals(1, entityManager.countEntities(Grass.class));
    }

    @Test
    void cleanupCollectsDamageEventsBeforeRemovingDeadAnimals() {
        EntityManager entityManager = new EntityManager(new WorldMap());
        Rabbit rabbit = new Rabbit(new Vector2D(10.5, 10.5));
        entityManager.addEntity(rabbit);

        rabbit.takeDamage(rabbit.getMaxHealth());
        entityManager.cleanup(Season.SPRING);

        assertEquals(0, entityManager.countEntities(Rabbit.class));
        DamageEvent event = entityManager.consumeDamageEvents().get(0);
        assertEquals(rabbit.getMaxHealth(), event.getDamage());
        assertEquals(rabbit.getPosition(), event.getPosition());
    }

    @Test
    void repeatedSmallDamageIsMergedIntoOneDamageEvent() {
        WorldMap worldMap = new WorldMap();
        EntityManager entityManager = new EntityManager(worldMap);
        Rabbit rabbit = new Rabbit(new Vector2D(10.5, 10.5));
        entityManager.addEntity(rabbit);

        rabbit.takeDamage(0.4);
        rabbit.takeDamage(0.6);
        entityManager.cleanup(Season.SPRING);

        assertTrue(entityManager.consumeDamageEvents().isEmpty());

        rabbit.update(0.35, worldMap);
        entityManager.cleanup(Season.SPRING);

        DamageEvent event = entityManager.consumeDamageEvents().get(0);
        assertEquals(1.0, event.getDamage());
    }

    @Test
    void springSpawnsFruitTreesBack() {
        EntityManager entityManager = new EntityManager(new WorldMap());

        entityManager.spawnSeasonAnimals();

        assertTrue(entityManager.countEntities(FruitTree.class) > 0);
    }

    @Test
    void winterSeasonEffectStopsFruitGrowth() {
        FruitTree tree = new FruitTree(new Vector2D(60.5, 10.5));

        tree.setFruitGrowthMultiplier(
            SeasonEffect.forSeason(Season.WINTER).getFruitGrowthMultiplier()
        );

        assertEquals(0.0, SeasonEffect.forSeason(Season.WINTER).getFruitGrowthMultiplier());
    }

    @Test
    void summerSeasonEffectMakesThirstDecayFaster() {
        assertTrue(
            SeasonEffect.forSeason(Season.SUMMER).getThirstDecayMultiplier()
                > SeasonEffect.forSeason(Season.SPRING).getThirstDecayMultiplier()
        );
    }
}
