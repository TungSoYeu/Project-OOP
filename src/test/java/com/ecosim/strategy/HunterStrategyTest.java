package com.ecosim.strategy;

import com.ecosim.model.Action;
import com.ecosim.model.Grass;
import com.ecosim.model.Hunter;
import com.ecosim.model.WorldMap;
import com.ecosim.util.Vector2D;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
