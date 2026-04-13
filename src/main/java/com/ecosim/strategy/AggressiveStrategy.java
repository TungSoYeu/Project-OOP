package com.ecosim.strategy;

import com.ecosim.model.*;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Chiến lược liều lĩnh (AggressiveStrategy).
 * Khi con vật đói quá → chấp nhận rủi ro vào vùng nguy hiểm tìm thức ăn.
 *
 * Áp dụng cho: Thỏ khi hunger < CRITICAL_HUNGER.
 * Khác với PassiveStrategy: không chạy trốn, dám đi qua vùng có kẻ thù.
 *
 * Khi hunger > ngưỡng an toàn → tự quay về ScaredStrategy/PassiveStrategy.
 */
public class AggressiveStrategy implements SurvivalStrategy {

    /** Ngưỡng đói để kích hoạt (mặc định = CRITICAL_HUNGER) */
    private final double hungerThreshold;

    public AggressiveStrategy() {
        this(Constants.CRITICAL_HUNGER);
    }

    public AggressiveStrategy(double hungerThreshold) {
        this.hungerThreshold = hungerThreshold;
    }

    @Override
    public Action decide(Animal self, List<Entity> nearby, WorldMap worldMap) {
        // Kiểm tra nếu đã no → quay về strategy mặc định
        if (self.getHunger() > 60) {
            self.resetStrategy();
            return Action.wander();
        }

        // 1. Tìm thức ăn bằng mọi giá - BẤT CHẤP kẻ thù
        Entity food = findBestFood(self, nearby);
        if (food != null) {
            double dist = self.distanceTo(food);
            if (dist < 1.5) {
                return Action.eat(food);
            }
            return Action.moveTo(food.getPosition());
        }

        // 2. Nếu khát → uống nước
        if (self.getThirst() < 30) {
            if (isNearWater(self.getPosition(), worldMap)) {
                self.drinkWater();
                return Action.idle();
            }
            Vector2D water = worldMap.findNearestWater(self.getPosition());
            if (water != null) {
                return Action.drink(water);
            }
        }

        // 3. Lang thang xa hơn bình thường tìm thức ăn
        return Action.wander();
    }

    /**
     * Tìm thức ăn tốt nhất - ưu tiên giá trị dinh dưỡng cao.
     * Bất chấp kẻ thù gần đó.
     */
    private Entity findBestFood(Animal self, List<Entity> nearby) {
        return nearby.stream()
            .filter(e -> e.isAlive() && self.isPrey(e))
            .filter(e -> !(e instanceof Plant p) || p.isEdible())
            .min(Comparator.comparingDouble(self::distanceTo))
            .orElse(null);
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
        return "Liều lĩnh";
    }

    public double getHungerThreshold() { return hungerThreshold; }
}
