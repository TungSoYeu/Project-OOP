package com.ecosim.view;

import com.ecosim.model.*;
import com.ecosim.util.Constants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Shape renderer for the simulation.
 * It stays lightweight, but uses varied terrain colors and species-specific shapes.
 */
public class BasicRenderer implements Renderer {

    private static final Font LABEL_FONT = Font.font("Arial", 9);

    @Override
    public void renderTerrain(GraphicsContext gc, WorldMap worldMap, Camera camera) {
        double tileSize = camera.getTileScreenSize();

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
                Color terrainColor = getTerrainColor(tile.getType(), tx, ty);

                gc.setFill(terrainColor);
                gc.fillRect(sx, sy, tileSize + 0.5, tileSize + 0.5);
                drawTerrainDetail(gc, tile.getType(), sx, sy, tileSize, tx, ty);

                if (tileSize > 12) {
                    gc.setStroke(terrainColor.darker().deriveColor(0, 1, 1, 0.22));
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

        if (!camera.isVisible(entity.getPosition().getX(), entity.getPosition().getY(), size)) {
            return;
        }

        Color color = getEntityColor(entity);

        if (entity instanceof Grass) {
            drawGrass(gc, sx, sy, size, color);
        } else if (entity instanceof FruitTree fruitTree) {
            drawFruitTree(gc, sx, sy, size, color, fruitTree.getFruitCount());
        } else if (entity instanceof Animal animal) {
            drawRoundAnimal(gc, sx, sy, size, color);

            if (animal.getState() == AnimalState.WALKING || animal.getState() == AnimalState.RUNNING
                || animal.getState() == AnimalState.FLEEING) {
                drawDirectionArrow(gc, sx, sy, animal.getDirection(), size * 0.6, color.brighter());
            }

            drawStateIcon(gc, sx, sy - size / 2 - 4, animal.getState(), size);
        }

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

        drawBar(gc, sx - barWidth / 2, barY, barWidth, barHeight,
            animal.getHealth() / animal.getMaxHealth(), Color.RED, Color.DARKRED);
        drawBar(gc, sx - barWidth / 2, barY - 5, barWidth, barHeight,
            animal.getHunger() / Constants.MAX_HUNGER, Color.GOLD, Color.DARKGOLDENROD);
        drawBar(gc, sx - barWidth / 2, barY - 10, barWidth, barHeight,
            animal.getThirst() / Constants.MAX_THIRST, Color.DODGERBLUE, Color.DARKBLUE);
    }

    private Color getTerrainColor(TerrainType type, int x, int y) {
        double variation = ((hash(x, y) % 9) - 4) * 0.018;
        Color base = type.getColor();

        return switch (type) {
            case GRASSLAND -> shiftBrightness(base, variation);
            case FOREST -> shiftBrightness(base, variation * 1.6);
            case BUSH -> shiftBrightness(base, variation * 1.3);
            case WATER -> shiftBrightness(base, Math.abs(variation) * 0.8);
            case MUD -> shiftBrightness(base, variation * 0.9);
            case ROCK -> shiftBrightness(base, variation * 1.2);
        };
    }

    private void drawTerrainDetail(GraphicsContext gc, TerrainType type, double sx, double sy,
                                   double tileSize, int tx, int ty) {
        if (tileSize < 6) return;

        int value = hash(tx * 3, ty * 5);
        if (type == TerrainType.WATER) {
            gc.setStroke(Color.rgb(180, 225, 255, 0.20));
            gc.setLineWidth(Math.max(0.6, tileSize * 0.06));
            double y = sy + tileSize * (0.35 + (value % 4) * 0.08);
            gc.strokeLine(sx + tileSize * 0.15, y, sx + tileSize * 0.85, y);
        } else if (type == TerrainType.GRASSLAND && tileSize > 10 && value % 5 == 0) {
            gc.setStroke(Color.rgb(230, 255, 205, 0.22));
            gc.setLineWidth(1);
            gc.strokeLine(sx + tileSize * 0.25, sy + tileSize * 0.7,
                sx + tileSize * 0.38, sy + tileSize * 0.45);
        } else if (type == TerrainType.ROCK && tileSize > 9) {
            gc.setFill(Color.rgb(245, 245, 245, 0.12));
            gc.fillOval(sx + tileSize * 0.28, sy + tileSize * 0.25,
                tileSize * 0.25, tileSize * 0.18);
        }
    }

    private Color shiftBrightness(Color color, double amount) {
        if (amount >= 0) {
            return color.brighter().interpolate(color, 1 - amount);
        }
        return color.darker().interpolate(color, 1 + amount);
    }

    private int hash(int x, int y) {
        int h = x * 73428767 ^ y * 912931;
        h ^= h >>> 13;
        return Math.abs(h);
    }

    private void drawGrass(GraphicsContext gc, double sx, double sy, double size, Color color) {
        double half = size / 2;
        gc.setStroke(color.darker());
        gc.setLineWidth(Math.max(1, size * 0.12));
        gc.strokeLine(sx, sy + half, sx, sy - half);
        gc.strokeLine(sx, sy + half * 0.7, sx - half * 0.55, sy - half * 0.15);
        gc.strokeLine(sx, sy + half * 0.65, sx + half * 0.55, sy - half * 0.05);
        gc.setFill(color.brighter());
        gc.fillOval(sx - half * 0.25, sy - half * 0.35, half * 0.5, half * 0.5);
    }

    private void drawFruitTree(GraphicsContext gc, double sx, double sy, double size, Color color, int fruitCount) {
        double half = size / 2;
        gc.setFill(Color.web("#6B4423"));
        gc.fillRoundRect(sx - half * 0.18, sy, half * 0.36, half * 0.85, 3, 3);

        gc.setFill(color);
        gc.fillOval(sx - half * 0.85, sy - half * 0.8, size * 0.85, size * 0.85);
        gc.fillOval(sx - half * 0.15, sy - half * 0.95, size * 0.85, size * 0.85);
        gc.fillOval(sx - half * 0.5, sy - half * 1.25, size * 0.9, size * 0.9);
        gc.setStroke(color.darker());
        gc.setLineWidth(1);
        gc.strokeOval(sx - half * 0.5, sy - half * 1.0, size * 0.9, size * 0.9);

        gc.setFill(Color.web("#FF5A4D"));
        int visibleFruits = Math.min(fruitCount, 3);
        for (int i = 0; i < visibleFruits; i++) {
            double fx = sx + (i - 1) * half * 0.35;
            double fy = sy - half * (0.65 + (i % 2) * 0.25);
            gc.fillOval(fx, fy, Math.max(2, size * 0.13), Math.max(2, size * 0.13));
        }
    }

    private void drawRoundAnimal(GraphicsContext gc, double sx, double sy, double size, Color color) {
        double half = size / 2;
        gc.setFill(color);
        gc.fillOval(sx - half, sy - half, size, size);
        gc.setStroke(color.darker());
        gc.setLineWidth(1.5);
        gc.strokeOval(sx - half, sy - half, size, size);
    }

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
        gc.setFill(bgColor);
        gc.fillRect(x, y, width, height);
        gc.setFill(fillColor);
        gc.fillRect(x, y, width * Math.max(0, Math.min(1, fillPercent)), height);
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
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, Math.max(8, size * 0.5)));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(icon, x, y);
        }
    }

    @Override
    public String getModeName() {
        return "Enhanced Shapes";
    }
}
