package com.ecosim.strategy;

import com.ecosim.model.Action;
import com.ecosim.model.Animal;
import com.ecosim.model.Entity;
import com.ecosim.model.WorldMap;

import java.util.List;

/**
 * Interface chiến lược sinh tồn (Strategy Pattern).
 *
 * Mỗi loài động vật có một SurvivalStrategy quyết định hành vi.
 * Có thể đổi strategy runtime (ví dụ: thỏ đói → AggressiveStrategy).
 *
 * Dễ dàng thêm strategy mới bằng cách implement interface này.
 */
public interface SurvivalStrategy {

    /**
     * Quyết định hành động tiếp theo cho con vật.
     *
     * @param self     Bản thân con vật
     * @param nearby   Danh sách entity trong tầm nhìn
     * @param worldMap Bản đồ thế giới
     * @return Action chứa loại hành động và mục tiêu
     */
    Action decide(Animal self, List<Entity> nearby, WorldMap worldMap);

    /**
     * Tên hiển thị của chiến lược.
     */
    String getName();
}
