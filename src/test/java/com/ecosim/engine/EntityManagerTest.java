package com.ecosim.engine;

import com.ecosim.model.Deer;
import com.ecosim.model.WorldMap;
import com.ecosim.strategy.AggressiveStrategy;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
}
