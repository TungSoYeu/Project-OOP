package com.ecosim.strategy;

import com.ecosim.model.Action;
import com.ecosim.model.Grass;
import com.ecosim.model.Hunter;
import com.ecosim.model.Rabbit;
import com.ecosim.model.Wolf;
import com.ecosim.model.WorldMap;
import com.ecosim.util.Vector2D;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HunterStrategyTest {

    @Test
    void hungryHunterMovesTowardNearestPlantWhenNoPreyExists() {
        Hunter hunter = new Hunter(new Vector2D(40.5, 40.5));
        hunter.setHunger(10);

        Grass nearPlant = new Grass(new Vector2D(42.5, 40.5));
        nearPlant.update(5, new WorldMap());

        Grass farPlant = new Grass(new Vector2D(49.5, 49.5));
        farPlant.update(5, new WorldMap());

        Action action = new HunterStrategy().decide(hunter, List.of(farPlant, nearPlant), new WorldMap());

        assertEquals(Action.Type.MOVE_TO, action.getType());
        assertEquals(nearPlant.getPosition(), action.getTargetPosition());
    }

    @Test
    void predatorChaseActionKeepsTargetEntityForRunMovement() {
        WorldMap worldMap = new WorldMap();
        Wolf wolf = new Wolf(new Vector2D(10.5, 10.5));
        Rabbit rabbit = new Rabbit(new Vector2D(14.5, 10.5));

        Action action = new HunterStrategy().decide(wolf, List.of(rabbit), worldMap);

        assertEquals(Action.Type.MOVE_TO, action.getType());
        assertSame(rabbit, action.getTargetEntity());

        double before = wolf.distanceTo(rabbit);
        wolf.executeAction(action, 0.5, worldMap);

        assertEquals(com.ecosim.model.AnimalState.RUNNING, wolf.getState());
        assertTrue(wolf.distanceTo(rabbit) < before);
    }
}
