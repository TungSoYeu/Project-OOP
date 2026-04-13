package com.ecosim.strategy;

import com.ecosim.model.*;
import com.ecosim.util.Vector2D;

import java.util.List;

/**
 * Chiến lược thụ động (PassiveStrategy).
 * Dành cho động vật ăn cỏ hiền lành hoặc Voi.
 *
 * Hành vi:
 * 1. Nếu khát → tìm nguồn nước
 * 2. Nếu đói → tìm thức ăn gần nhất
 * 3. Nếu mệt (health thấp) → ngủ
 * 4. Nếu không có nhu cầu → lang thang
 */
public class PassiveStrategy implements SurvivalStrategy {

    @Override
    public Action decide(Animal self, List<Entity> nearby, WorldMap worldMap) {
        // 1. Khát → tìm nước
        if (self.getThirst() < 40) {
            // Kiểm tra có đang gần nước không
            TerrainType currentTerrain = worldMap.getTerrainAt(
                self.getPosition().getX(), self.getPosition().getY());
            if (isNearWater(self.getPosition(), worldMap)) {
                self.drinkWater();
                return Action.idle();
            }
            Vector2D water = worldMap.findNearestWater(self.getPosition());
            if (water != null) {
                return Action.drink(water);
            }
        }

        // 2. Đói → tìm thức ăn
        if (self.getHunger() < 60) {
            Entity food = findNearestFood(self, nearby);
            if (food != null) {
                double dist = self.distanceTo(food);
                if (dist < 1.5) {
                    return Action.eat(food);
                }
                return Action.moveTo(food.getPosition());
            }
        }

        // 3. Mệt → ngủ
        if (self.getHealth() < self.getMaxHealth() * 0.3) {
            return Action.sleep();
        }

        // 4. Lang thang
        return Action.wander();
    }

    /** Tìm thức ăn gần nhất trong tầm nhìn */
    protected Entity findNearestFood(Animal self, List<Entity> nearby) {
        Entity nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Entity entity : nearby) {
            if (!entity.isAlive()) continue;
            if (self.isPrey(entity)) {
                double dist = self.distanceTo(entity);
                if (dist < minDist) {
                    // Kiểm tra thêm nếu là Plant → phải edible
                    if (entity instanceof Plant plant && !plant.isEdible()) continue;
                    minDist = dist;
                    nearest = entity;
                }
            }
        }
        return nearest;
    }

    /** Kiểm tra có ở gần nước không (trong 2 tile) */
    protected boolean isNearWater(Vector2D pos, WorldMap worldMap) {
        int cx = pos.getTileX();
        int cy = pos.getTileY();
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                TerrainType t = worldMap.getTerrainAt(cx + dx, cy + dy);
                if (t == TerrainType.WATER) return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "Thụ động";
    }
}
