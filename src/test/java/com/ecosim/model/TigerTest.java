package com.ecosim.model;

import com.ecosim.util.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TigerTest {

    @Test
    void tigerDealsAmbushDamageWhenAttackingFromForest() {
        WorldMap worldMap = new WorldMap();
        Tiger tiger = new Tiger(new Vector2D(60.5, 10.5));
        Rabbit rabbit = new Rabbit(new Vector2D(61.0, 10.5));

        double initialHealth = rabbit.getHealth();

        tiger.executeAction(Action.attack(rabbit), 1.0, worldMap);

        assertTrue(initialHealth - rabbit.getHealth() > tiger.getAttackPower());
    }

    @Test
    void rabbitCanHideInBushButWolfCannotEnter() {
        Rabbit rabbit = new Rabbit(new Vector2D(10.5, 10.5));
        Wolf wolf = new Wolf(new Vector2D(10.5, 10.5));

        assertTrue(rabbit.canTraverse(TerrainType.BUSH));
        assertFalse(wolf.canTraverse(TerrainType.BUSH));
    }
}
