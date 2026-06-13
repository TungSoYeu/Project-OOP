package com.ecosim.strategy;

import com.ecosim.model.*;
import com.ecosim.util.Vector2D;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Chiến lược săn mồi (HunterStrategy).
 * Dành cho Sói, Hổ, Thợ săn.
 *
 * Hành vi:
 * 1. Quét tìm con mồi trong bán kính sightRange
 * 2. Nếu tìm thấy → đuổi theo và tấn công
 * 3. Nếu khát → tìm nước
 * 4. Nếu không có mồi → lang thang tìm kiếm
 */
public class HunterStrategy implements SurvivalStrategy {

    @Override
    public Action decide(Animal self, List<Entity> nearby, WorldMap worldMap) {
        // 1. Ưu tiên sinh tồn: khát quá thì uống nước trước
        if (self.getThirst() < 20) {
            if (isNearWater(self.getPosition(), worldMap)) {
                self.drinkWater();
                return Action.idle();
            }
            Vector2D water = worldMap.findNearestWater(self.getPosition());
            if (water != null) {
                return Action.drink(water);
            }
        }

        // 2. Quét tìm con mồi
        Optional<Entity> prey = findBestPrey(self, nearby);
        if (prey.isPresent()) {
            Entity target = prey.get();
            double dist = self.distanceTo(target);

            // Hổ gầm khi phát hiện con mồi
            if (self instanceof Tiger tiger) {
                tiger.roar();
            }

            // Đủ gần → tấn công
            if (dist < getAttackRange(self)) {
                return Action.attack(target);
            }

            // Đuổi theo
            return Action.moveTo(target.getPosition());
        }

        // 3. Đói → tìm thức ăn thực vật (backup cho khi không có mồi)
        if (self.getHunger() < 30) {
            Entity plant = findNearestPlant(self, nearby);
            if (plant != null) {
                if (self.canReach(plant)) {
                    return Action.eat(plant);
                }
                return Action.moveTo(plant.getPosition());
            }
        }

        // 4. Mệt → ngủ (hồi phục)
        if (self.getHealth() < self.getMaxHealth() * 0.3) {
            return Action.sleep();
        }

        // 5. Lang thang tìm mồi
        return Action.wander();
    }

    /**
     * Tìm con mồi tốt nhất:
     * - Ưu tiên con yếu nhất (health thấp)
     * - Ưu tiên con gần nhất
     */
    private Optional<Entity> findBestPrey(Animal self, List<Entity> nearby) {
        return nearby.stream()
            .filter(e -> e.isAlive() && self.isPrey(e))
            .filter(e -> e instanceof Animal) // Chỉ săn động vật
            .min(Comparator.comparingDouble(e -> {
                double dist = self.distanceTo(e);
                double healthFactor = (e instanceof Animal a) ? a.getHealth() / a.getMaxHealth() : 1.0;
                return dist * healthFactor; // Ưu tiên gần + yếu
            }));
    }

    /** Tìm thực vật gần nhất */
    private Entity findNearestPlant(Animal self, List<Entity> nearby) {
        return nearby.stream()
            .filter(e -> e.isAlive() && e instanceof Plant p && p.isEdible())
            .min(Comparator.comparingDouble(self::distanceTo))
            .orElse(null);
    }

    /** Tầm tấn công tùy loài */
    private double getAttackRange(Animal self) {
        if (self instanceof Hunter) return 8.0;  // Vũ khí tầm xa
        return 1.5; // Tấn công cận chiến
    }

    /** Kiểm tra gần nước */
    private boolean isNearWater(Vector2D pos, WorldMap worldMap) {
        int cx = pos.getTileX();
        int cy = pos.getTileY();
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                if (worldMap.getTerrainAt(cx + dx, cy + dy) == TerrainType.WATER) return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "Săn mồi";
    }
}
