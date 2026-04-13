package com.ecosim.strategy;

import com.ecosim.model.*;
import com.ecosim.util.Vector2D;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Chiến lược sợ hãi (ScaredStrategy).
 * Dành cho Thỏ, Hươu - luôn cảnh giác và chạy trốn khi gặp nguy hiểm.
 *
 * Hành vi:
 * 1. Luôn quét tìm kẻ thù
 * 2. Nếu phát hiện kẻ thù → chạy ngược hướng
 * 3. Nếu gần bụi rậm/rừng → chạy vào trốn (thỏ)
 * 4. Nếu an toàn → ăn uống bình thường (delegate cho PassiveStrategy)
 */
public class ScaredStrategy implements SurvivalStrategy {

    /** Strategy dùng khi an toàn */
    private final PassiveStrategy safeMode = new PassiveStrategy();

    @Override
    public Action decide(Animal self, List<Entity> nearby, WorldMap worldMap) {
        // 1. Quét tìm kẻ thù gần nhất
        Optional<Entity> nearestEnemy = findNearestEnemy(self, nearby);

        if (nearestEnemy.isPresent()) {
            Entity enemy = nearestEnemy.get();
            double distToEnemy = self.distanceTo(enemy);

            // Nếu kẻ thù trong tầm nguy hiểm (< sightRange * 0.8)
            if (distToEnemy < self.getSightRange() * 0.8) {

                // Thử tìm nơi trốn (bụi rậm / rừng) trước
                if (self instanceof Rabbit rabbit && rabbit.canHideInBush()) {
                    Vector2D hideSpot = worldMap.findNearestHidingSpot(self.getPosition());
                    if (hideSpot != null) {
                        double distToHide = self.getPosition().distanceTo(hideSpot);
                        // Nếu chỗ trốn gần hơn kẻ thù → chạy vào trốn
                        if (distToHide < distToEnemy) {
                            return Action.hide(hideSpot);
                        }
                    }
                }

                // Không có chỗ trốn → chạy ngược hướng
                return Action.flee(enemy.getPosition());
            }
        }

        // 2. An toàn → hành vi bình thường (ăn uống)
        return safeMode.decide(self, nearby, worldMap);
    }

    /** Tìm kẻ thù gần nhất trong tầm nhìn */
    private Optional<Entity> findNearestEnemy(Animal self, List<Entity> nearby) {
        return nearby.stream()
            .filter(e -> e.isAlive() && self.isEnemy(e))
            .min(Comparator.comparingDouble(self::distanceTo));
    }

    @Override
    public String getName() {
        return "Sợ hãi";
    }
}
