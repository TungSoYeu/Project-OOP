package com.ecosim.strategy;

import com.ecosim.model.*;

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

            // Thỏ khi đói liều lĩnh phớt lờ nguy hiểm để đi ăn
            boolean recklessRabbit = (self instanceof Rabbit) && (self.getHunger() < 60);

            // Nếu kẻ thù trong tầm nhìn (toàn bộ sightRange) và không liều lĩnh
            if (!recklessRabbit && distToEnemy <= self.getSightRange()) {

                // Tìm "Bụi cỏ" (Grass entity) gần nhất để trốn
                if (self instanceof Rabbit) {
                    Optional<Entity> nearestGrass = nearby.stream()
                        .filter(e -> e.isAlive() && e instanceof Grass)
                        .min(Comparator.comparingDouble(self::distanceTo));

                    if (nearestGrass.isPresent()) {
                        Entity grass = nearestGrass.get();
                        double distToHide = self.distanceTo(grass);
                        // Luôn cố gắng chạy về bụi cỏ gần nhất nếu nó ở không quá xa (< 20 tiles)
                        if (distToHide < 20.0) {
                            return Action.hide(grass.getPosition());
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
