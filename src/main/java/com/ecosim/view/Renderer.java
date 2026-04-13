package com.ecosim.view;

import com.ecosim.model.*;
import com.ecosim.util.Vector2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * Interface cho renderer - tách biệt ViewLogic khỏi BioLogic.
 * Cho phép chuyển đổi giữa Basic (shapes) và Sprite (images) mode.
 */
public interface Renderer {

    /**
     * Vẽ nền terrain (bản đồ).
     */
    void renderTerrain(GraphicsContext gc, WorldMap worldMap, Camera camera);

    /**
     * Vẽ một entity lên canvas.
     */
    void renderEntity(GraphicsContext gc, Entity entity, Camera camera);

    /**
     * Vẽ tất cả entity.
     */
    default void renderEntities(GraphicsContext gc, List<Entity> entities, Camera camera) {
        // Vẽ thực vật trước
        for (Entity entity : entities) {
            if (entity.isAlive() && entity instanceof Plant) {
                renderEntity(gc, entity, camera);
            }
        }
        // Vẽ động vật sau (lên trên)
        for (Entity entity : entities) {
            if (entity.isAlive() && entity instanceof Animal) {
                renderEntity(gc, entity, camera);
            }
        }
    }

    /**
     * Vẽ thông tin trạng thái trên đầu entity (HP bar, tên, ...).
     */
    void renderEntityInfo(GraphicsContext gc, Entity entity, Camera camera);

    /**
     * Lấy tên render mode.
     */
    String getModeName();
}
