package com.ecosim.view;

import com.ecosim.model.*;
import com.ecosim.util.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Basic Renderer - Vẽ entities bằng hình tròn/vuông đơn giản.
 * Mỗi loại entity có màu riêng (theo Constants).
 *
 * Chế độ hiển thị đơn giản, dùng cho debug và khi không có sprite.
 */
public class BasicRenderer implements Renderer {

    private static final Font LABEL_FONT = Font.font("Arial", 9);
    private static final Font INFO_FONT = Font.font("Arial", 10);

    @Override
    public void renderTerrain(GraphicsContext gc, WorldMap worldMap, Camera camera) {
        double tileSize = camera.getTileScreenSize();

        // Tính vùng tile cần vẽ (chỉ vẽ trong viewport)
        int startX = Math.max(0, (int) Math.floor(camera.getX()));
        int startY = Math.max(0, (int) Math.floor(camera.getY()));
        int endX = Math.min(worldMap.getWidth(), (int) Math.ceil(camera.getX() + camera.getVisibleWidth()) + 1);
        int endY = Math.min(worldMap.getHeight(), (int) Math.ceil(camera.getY() + camera.getVisibleHeight()) + 1);

        for (int ty = startY; ty < endY; ty++) {
            for (int tx = startX; tx < endX; tx++) {
                TerrainTile tile = worldMap.getTileAt(tx, ty);
                if (tile == null) continue;

                double sx = camera.worldToScreenX(tx);
                double sy = camera.worldToScreenY(ty);

                // Vẽ ô terrain
                gc.setFill(tile.getType().getColor());
                gc.fillRect(sx, sy, tileSize + 0.5, tileSize + 0.5); // +0.5 để tránh kẽ hở

                // Vẽ viền nhẹ khi zoom đủ lớn
                if (tileSize > 12) {
                    gc.setStroke(tile.getType().getColor().darker().deriveColor(0, 1, 1, 0.3));
                    gc.setLineWidth(0.5);
                    gc.strokeRect(sx, sy, tileSize, tileSize);
                }
            }
        }
    }

    @Override
    public void renderEntity(GraphicsContext gc, Entity entity, Camera camera) {
        if (!entity.isAlive()) return;

        double sx = camera.worldToScreenX(entity.getPosition().getX());
        double sy = camera.worldToScreenY(entity.getPosition().getY());
        double size = camera.entityScreenSize(entity.getSize());

        // Kiểm tra có trong viewport không
        if (!camera.isVisible(entity.getPosition().getX(), entity.getPosition().getY(), size)) {
            return;
        }

        // Lấy màu theo loại entity
        Color color = getEntityColor(entity);
        gc.setFill(color);

        if (entity instanceof Plant) {
            // Thực vật: hình vuông
            double halfSize = size / 2;
            gc.fillRect(sx - halfSize, sy - halfSize, size, size);

            // Viền
            gc.setStroke(color.darker());
            gc.setLineWidth(1);
            gc.strokeRect(sx - halfSize, sy - halfSize, size, size);
        } else if (entity instanceof Animal animal) {
            // Động vật: hình tròn
            double halfSize = size / 2;
            gc.fillOval(sx - halfSize, sy - halfSize, size, size);

            // Viền
            gc.setStroke(color.darker());
            gc.setLineWidth(1.5);
            gc.strokeOval(sx - halfSize, sy - halfSize, size, size);

            // Vẽ hướng di chuyển (mũi tên nhỏ)
            if (animal.getState() == AnimalState.WALKING || animal.getState() == AnimalState.RUNNING
                || animal.getState() == AnimalState.FLEEING) {
                drawDirectionArrow(gc, sx, sy, animal.getDirection(), size * 0.6, color.brighter());
            }

            // Vẽ trạng thái icon
            drawStateIcon(gc, sx, sy - halfSize - 4, animal.getState(), size);
        }

        // Vẽ tên khi zoom đủ lớn
        if (camera.getZoom() > 1.0) {
            gc.setFill(Color.WHITE);
            gc.setFont(LABEL_FONT);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(entity.getTypeName(), sx, sy + size / 2 + 12);
        }
    }

    @Override
    public void renderEntityInfo(GraphicsContext gc, Entity entity, Camera camera) {
        if (!entity.isAlive()) return;
        if (!(entity instanceof Animal animal)) return;

        double sx = camera.worldToScreenX(entity.getPosition().getX());
        double sy = camera.worldToScreenY(entity.getPosition().getY());
        double size = camera.entityScreenSize(entity.getSize());

        double barWidth = Math.max(size * 1.5, 20);
        double barHeight = 3;
        double barY = sy - size / 2 - 16;

        // HP bar (đỏ)
        drawBar(gc, sx - barWidth / 2, barY, barWidth, barHeight,
                animal.getHealth() / animal.getMaxHealth(), Color.RED, Color.DARKRED);

        // Hunger bar (vàng)
        drawBar(gc, sx - barWidth / 2, barY - 5, barWidth, barHeight,
                animal.getHunger() / Constants.MAX_HUNGER, Color.GOLD, Color.DARKGOLDENROD);

        // Thirst bar (xanh dương)
        drawBar(gc, sx - barWidth / 2, barY - 10, barWidth, barHeight,
                animal.getThirst() / Constants.MAX_THIRST, Color.DODGERBLUE, Color.DARKBLUE);
    }

    // ===== Helper methods =====

    private Color getEntityColor(Entity entity) {
        if (entity instanceof Grass) return Constants.COLOR_GRASS;
        if (entity instanceof FruitTree) return Constants.COLOR_FRUIT_TREE;
        if (entity instanceof Rabbit) return Constants.COLOR_RABBIT;
        if (entity instanceof Deer) return Constants.COLOR_DEER;
        if (entity instanceof Wolf) return Constants.COLOR_WOLF;
        if (entity instanceof Tiger) return Constants.COLOR_TIGER;
        if (entity instanceof Hunter) return Constants.COLOR_HUNTER;
        if (entity instanceof Elephant) return Constants.COLOR_ELEPHANT;
        return Color.WHITE;
    }

    private void drawBar(GraphicsContext gc, double x, double y, double width, double height,
                          double fillPercent, Color fillColor, Color bgColor) {
        // Background
        gc.setFill(bgColor);
        gc.fillRect(x, y, width, height);
        // Fill
        gc.setFill(fillColor);
        gc.fillRect(x, y, width * Math.max(0, Math.min(1, fillPercent)), height);
        // Border
        gc.setStroke(Color.rgb(0, 0, 0, 0.5));
        gc.setLineWidth(0.5);
        gc.strokeRect(x, y, width, height);
    }

    private void drawDirectionArrow(GraphicsContext gc, double cx, double cy,
                                     com.ecosim.util.Vector2D direction, double length, Color color) {
        if (direction == null) return;
        double endX = cx + direction.getX() * length;
        double endY = cy + direction.getY() * length;
        gc.setStroke(color);
        gc.setLineWidth(1.5);
        gc.strokeLine(cx, cy, endX, endY);
    }

    private void drawStateIcon(GraphicsContext gc, double x, double y, AnimalState state, double size) {
        String icon = switch (state) {
            case EATING -> "🍽";
            case DRINKING -> "💧";
            case SLEEPING -> "💤";
            case ATTACKING -> "⚔";
            case FLEEING -> "💨";
            case HIDING -> "🌿";
            default -> null;
        };
        if (icon != null && size > 8) {
            gc.setFont(Font.font(Math.max(8, size * 0.5)));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(icon, x, y);
        }
    }

    @Override
    public String getModeName() {
        return "Basic (Shapes)";
    }
}
