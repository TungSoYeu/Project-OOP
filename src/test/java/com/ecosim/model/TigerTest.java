package com.ecosim.model;

import com.ecosim.util.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TigerTest {

    @Test
    void tigerDealsAmbushDamageWhenAttackingFromForest() {
        WorldMap worldMap = new WorldMap();
        worldMap.setTerrainAt(60, 10, TerrainType.FOREST);
        Tiger tiger = new Tiger(new Vector2D(60.5, 10.5));
        Rabbit rabbit = new Rabbit(new Vector2D(61.0, 10.5));

        double initialHealth = rabbit.getHealth();

        tiger.executeAction(Action.attack(rabbit), 1.0, worldMap);

        assertTrue(initialHealth - rabbit.getHealth() > tiger.getAttackPower());
    }
}
