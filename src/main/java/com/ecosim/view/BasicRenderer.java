package com.ecosim.view;

import com.ecosim.model.*;
import com.ecosim.util.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Basic Renderer - Vẽ entities bằng hình tròn/vuông đơn giản.
 * Mỗi loại entity có màu riêng (theo Constants).
 *
 * Chế độ hiển thị đơn giản, dùng cho debug và khi không có sprite.
 */
public class BasicRenderer implements Renderer {

    @Override
    public void renderTerrain(GraphicsContext gc, WorldMap worldMap, Camera camera) {
        double tileSize = camera.getTileScreenSize();
        double time = System.nanoTime() / 1_000_000_000.0; // Thời gian cho animation

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

                TerrainType type = tile.getType();

                if (type == TerrainType.WATER) {
                    // === HỒ NƯỚC: Gradient xanh + sóng gợn ===
                    // Tính độ sâu (xa viền = sâu hơn = tối hơn)
                    double depthFactor = getWaterDepth(worldMap, tx, ty);
                    
                    // Màu nước dao động theo thời gian (sóng gợn)
                    double wave = Math.sin(time * 1.5 + tx * 0.8 + ty * 0.6) * 0.08;
                    double r = Math.max(0, Math.min(1, 0.15 + wave - depthFactor * 0.1));
                    double g = Math.max(0, Math.min(1, 0.45 + wave * 0.5 - depthFactor * 0.05));
                    double b = Math.max(0, Math.min(1, 0.82 + wave * 0.3 - depthFactor * 0.15));

                    gc.setFill(Color.color(r, g, b, 0.92));
                    gc.fillRect(sx, sy, tileSize + 0.5, tileSize + 0.5);

                    // Highlight lấp lánh (phản chiếu ánh sáng)
                    if (tileSize > 4) {
                        double sparkle = Math.sin(time * 3.0 + tx * 2.7 + ty * 1.9);
                        if (sparkle > 0.7) {
                            double alpha = (sparkle - 0.7) / 0.3 * 0.5;
                            gc.setFill(Color.rgb(255, 255, 255, alpha));
                            double sparkleSize = tileSize * 0.3;
                            double sparkleX = sx + (Math.sin(tx * 1.3) * 0.5 + 0.5) * (tileSize - sparkleSize);
                            double sparkleY = sy + (Math.cos(ty * 1.7) * 0.5 + 0.5) * (tileSize - sparkleSize);
                            gc.fillOval(sparkleX, sparkleY, sparkleSize, sparkleSize * 0.5);
                        }
                    }

                    // Đường gợn sóng khi zoom đủ lớn
                    if (tileSize > 8) {
                        double waveY = sy + tileSize * (0.3 + Math.sin(time * 2 + tx * 0.5) * 0.15);
                        gc.setStroke(Color.rgb(255, 255, 255, 0.15));
                        gc.setLineWidth(0.8);
                        gc.strokeLine(sx + 1, waveY, sx + tileSize - 1, waveY);
                    }

                } else if (type == TerrainType.MUD) {
                    // === BÙN: Texture lốm đốm ===
                    gc.setFill(Color.web(type.getHexColor()));
                    gc.fillRect(sx, sy, tileSize + 0.5, tileSize + 0.5);

                    // Vệt bùn loang
                    if (tileSize > 6) {
                        int hash = tx * 31 + ty * 17;
                        gc.setFill(Color.rgb(100, 80, 55, 0.3));
                        double ox = (hash % 7) * tileSize / 10.0;
                        double oy = ((hash / 7) % 5) * tileSize / 8.0;
                        gc.fillOval(sx + ox, sy + oy, tileSize * 0.4, tileSize * 0.3);
                    }

                } else if (type == TerrainType.GRASSLAND) {
                    // === ĐỒNG CỎ: Màu sắc đa dạng hơn ===
                    int hash = tx * 7 + ty * 13;
                    double variation = (hash % 20) / 100.0 - 0.1;
                    Color baseColor = Color.web(type.getHexColor());
                    Color grassColor = baseColor.deriveColor(variation * 30, 1 + variation * 0.3, 1 + variation * 0.2, 1);
                    gc.setFill(grassColor);
                    gc.fillRect(sx, sy, tileSize + 0.5, tileSize + 0.5);

                    // Chấm cỏ nhỏ khi zoom lớn
                    if (tileSize > 10 && hash % 3 == 0) {
                        gc.setFill(grassColor.brighter().deriveColor(0, 1, 1, 0.4));
                        gc.fillOval(sx + tileSize * 0.2, sy + tileSize * 0.3, tileSize * 0.15, tileSize * 0.15);
                    }

                } else if (type == TerrainType.FOREST) {
                    // === RỪNG: Tối hơn + bóng cây ===
                    int hash = tx * 11 + ty * 23;
                    double variation = (hash % 15) / 100.0 - 0.07;
                    Color forestColor = Color.web(type.getHexColor()).deriveColor(variation * 20, 1, 1 + variation * 0.3, 1);
                    gc.setFill(forestColor);
                    gc.fillRect(sx, sy, tileSize + 0.5, tileSize + 0.5);

                    // Bóng lá rơi
                    if (tileSize > 8 && hash % 4 == 0) {
                        gc.setFill(Color.rgb(0, 0, 0, 0.15));
                        gc.fillOval(sx + tileSize * 0.1, sy + tileSize * 0.2, tileSize * 0.6, tileSize * 0.4);
                    }

                } else if (type == TerrainType.BUSH) {
                    // === BỤI RẬM: Xanh đậm + lá ===
                    gc.setFill(Color.web(type.getHexColor()));
                    gc.fillRect(sx, sy, tileSize + 0.5, tileSize + 0.5);

                    if (tileSize > 8) {
                        gc.setFill(Color.rgb(50, 120, 50, 0.35));
                        gc.fillOval(sx + 1, sy + 1, tileSize - 2, tileSize - 2);
                    }

                } else {
                    // === Các loại terrain khác (ROCK, ...) ===
                    if (type == TerrainType.ROCK) {
                        // Vẽ nền cỏ cho đá
                        int hash = tx * 7 + ty * 13;
                        double variation = (hash % 20) / 100.0 - 0.1;
                        Color grassColor = Color.web(TerrainType.GRASSLAND.getHexColor()).deriveColor(variation * 30, 1 + variation * 0.3, 1 + variation * 0.2, 1);
                        gc.setFill(grassColor);
                        gc.fillRect(sx, sy, tileSize + 0.5, tileSize + 0.5);
                    } else {
                        gc.setFill(Color.web(type.getHexColor()));
                        gc.fillRect(sx, sy, tileSize + 0.5, tileSize + 0.5);
                    }
                }

                // Viền nhẹ khi zoom đủ lớn (không áp dụng cho nước)
                if (tileSize > 12 && type != TerrainType.WATER) {
                    gc.setStroke(Color.web(type.getHexColor()).darker().deriveColor(0, 1, 1, 0.2));
                    gc.setLineWidth(0.5);
                    gc.strokeRect(sx, sy, tileSize, tileSize);
                }
            }
        }
    }

    /** Tính độ sâu nước (0.0 = nông/viền, 1.0 = sâu/giữa hồ) */
    private double getWaterDepth(WorldMap worldMap, int tx, int ty) {
        int depth = 0;
        for (int r = 1; r <= 3; r++) {
            boolean allWater = true;
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (Math.abs(dx) == r || Math.abs(dy) == r) {
                        TerrainType t = worldMap.getTerrainAt(tx + dx, ty + dy);
                        if (t != TerrainType.WATER) {
                            allWater = false;
                            break;
                        }
                    }
                }
                if (!allWater) break;
            }
            if (allWater) depth = r;
            else break;
        }
        return depth / 3.0;
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
    }

    @Override
    public void renderEntityInfo(GraphicsContext gc, Entity entity, Camera camera) {
        if (!entity.isAlive()) return;
        if (!(entity instanceof Animal animal)) return;

        double sx = camera.worldToScreenX(entity.getPosition().getX());
        double sy = camera.worldToScreenY(entity.getPosition().getY());
        double size = camera.entityScreenSize(entity.getSize());

        // Kích thước tooltip
        double tooltipW = 110;
        double tooltipH = 55;
        double tx = sx - tooltipW / 2;
        double ty = sy - size / 2 - tooltipH - 15;

        // Box
        gc.setFill(Color.rgb(44, 53, 37, 0.9)); // #2c3525
        gc.fillRoundRect(tx, ty, tooltipW, tooltipH, 8, 8);
        gc.setStroke(Color.rgb(74, 92, 63, 0.9)); // #4a5c3f
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(tx, ty, tooltipW, tooltipH, 8, 8);

        // Mũi tên trỏ xuống
        gc.setFill(Color.rgb(44, 53, 37, 0.9));
        gc.fillPolygon(new double[]{sx - 6, sx + 6, sx}, new double[]{ty + tooltipH, ty + tooltipH, ty + tooltipH + 6}, 3);

        // Text
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(animal.getTypeName() + " (" + animal.getState().getDisplayName() + ")", tx + 5, ty + 12);

        // Bars
        double barX = tx + 30;
        double barW = tooltipW - 35;
        double barH = 5;
        
        gc.setFont(Font.font("System", 9));
        gc.setFill(Color.LIGHTGRAY);
        
        // HP
        gc.fillText("HP", tx + 5, ty + 25);
        drawBar(gc, barX, ty + 19, barW, barH, animal.getHealth() / animal.getMaxHealth(), Color.web("#e53935"), Color.rgb(0, 0, 0, 0.5));
        
        // Đói
        gc.fillText("Food", tx + 5, ty + 35);
        drawBar(gc, barX, ty + 29, barW, barH, animal.getHunger() / Constants.MAX_HUNGER, Color.web("#ffb300"), Color.rgb(0, 0, 0, 0.5));
        
        // Khát
        gc.fillText("H2O", tx + 5, ty + 45);
        drawBar(gc, barX, ty + 39, barW, barH, animal.getThirst() / Constants.MAX_THIRST, Color.web("#1e88e5"), Color.rgb(0, 0, 0, 0.5));
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
        if (entity instanceof Fish) return Constants.COLOR_FISH;
        if (entity instanceof Duck) return Constants.COLOR_DUCK;
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
