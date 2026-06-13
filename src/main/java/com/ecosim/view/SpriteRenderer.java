package com.ecosim.view;

import com.ecosim.model.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Sprite-style renderer: vẽ nhân vật bằng các sprite vector rõ đặc điểm loài.
 * Không chạm vào BioLogic; chỉ thay cách hiển thị trên Canvas.
 */
public class SpriteRenderer implements Renderer {

    private static final Font LABEL_FONT = Font.font("Segoe UI", FontWeight.BOLD, 10);
    private static final Font BADGE_FONT = Font.font("Segoe UI", FontWeight.BOLD, 11);

    private final BasicRenderer baseRenderer = new BasicRenderer();

    @Override
    public void renderTerrain(GraphicsContext gc, WorldMap worldMap, Camera camera) {
        baseRenderer.renderTerrain(gc, worldMap, camera);
    }

    @Override
    public void renderEntity(GraphicsContext gc, Entity entity, Camera camera) {
        if (!entity.isAlive()) return;

        double sx = camera.worldToScreenX(entity.getPosition().getX());
        double sy = camera.worldToScreenY(entity.getPosition().getY());
        double size = Math.max(5, camera.entityScreenSize(entity.getSize()));

        if (!camera.isVisible(entity.getPosition().getX(), entity.getPosition().getY(), size)) {
            return;
        }

        double phase = (System.nanoTime() / 280_000_000.0) + entity.getId().hashCode();
        double bob = 0;

        gc.save();
        gc.translate(sx, sy);

        if (entity instanceof Animal animal) {
            if (animal.getState() == AnimalState.RUNNING ||
                animal.getState() == AnimalState.FLEEING ||
                animal.getState() == AnimalState.WALKING) {

                bob = Math.sin(phase) * Math.max(1, size * 0.06);
            }

            if (animal.getDirection() != null && animal.getDirection().getX() < -0.05) {
                gc.scale(-1, 1);
            }
        }

        gc.translate(0, bob);

        switch (entity) {
            case Grass g -> drawGrass(gc, g, size);
            case FruitTree fruitTree -> drawFruitTree(gc, fruitTree, size);
            case Rabbit rabbit -> drawRabbit(gc, rabbit, size);
            case Deer deer -> drawDeer(gc, deer, size);
            case Wolf wolf -> drawWolf(gc, wolf, size);
            case Tiger tiger -> drawTiger(gc, tiger, size);
            case Hunter hunter -> drawHunter(gc, hunter, size);
            case Elephant elephant -> drawElephant(gc, elephant, size);
            default -> drawFallback(gc, size);
        }

        if (entity instanceof Animal animal) {
            drawStateBadge(gc, animal, size);
        }

        gc.restore();

        if (camera.getZoom() > 0.8) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(LABEL_FONT);
            gc.setFill(Color.WHITE);
            gc.fillText(entity.getTypeName(), sx, sy + size * 0.72 + 12);
        }
    }

    @Override
    public void renderEntityInfo(GraphicsContext gc, Entity entity, Camera camera) {
        baseRenderer.renderEntityInfo(gc, entity, camera);
    }

    @Override
    public String getModeName() {
        return "Đồ họa (Sprites)";
    }

    private void drawGrass(GraphicsContext gc, Grass grass, double size) {
        double h = size * 0.75;
        gc.setLineWidth(Math.max(1.2, size * 0.08));
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setStroke(Color.web("#1f8f3a"));

        for (int i = -2; i <= 2; i++) {
            double x = i * size * 0.12;
            gc.strokeLine(x, h * 0.35, x + i * size * 0.04, -h * 0.45);
        }

        gc.setStroke(Color.web("#8ce35b"));
        gc.strokeLine(0, h * 0.38, -size * 0.16, -h * 0.2);
        gc.strokeLine(0, h * 0.38, size * 0.18, -h * 0.28);
    }

    private void drawFruitTree(GraphicsContext gc, FruitTree tree, double size) {
        gc.setFill(Color.web("#7a4c24"));
        gc.fillRoundRect(-size * 0.12, -size * 0.05, size * 0.24, size * 0.52, 3, 3);

        gc.setFill(Color.web("#236d35"));
        gc.fillOval(-size * 0.46, -size * 0.62, size * 0.62, size * 0.62);
        gc.setFill(Color.web("#2d8b43"));
        gc.fillOval(-size * 0.12, -size * 0.72, size * 0.66, size * 0.66);
        gc.setFill(Color.web("#1f5e2f"));
        gc.fillOval(-size * 0.58, -size * 0.34, size * 0.78, size * 0.58);

        gc.setFill(Color.web("#e83f4f"));
        int fruits = Math.max(1, tree.getFruitCount());
        for (int i = 0; i < fruits; i++) {
            double x = (-0.24 + i * 0.22) * size;
            double y = (-0.35 + (i % 2) * 0.16) * size;
            gc.fillOval(x, y, size * 0.08, size * 0.08);
        }
    }

    private void drawRabbit(GraphicsContext gc, Rabbit rabbit, double size) {
        gc.setFill(Color.web("#dff4e3"));
        gc.fillOval(-size * 0.46, -size * 0.12, size * 0.78, size * 0.46);
        gc.fillOval(size * 0.08, -size * 0.36, size * 0.38, size * 0.36);

        gc.setFill(Color.web("#dff4e3"));
        gc.fillOval(size * 0.12, -size * 0.76, size * 0.12, size * 0.42);
        gc.fillOval(size * 0.28, -size * 0.72, size * 0.12, size * 0.38);
        gc.setFill(Color.web("#ffb6c8"));
        gc.fillOval(size * 0.15, -size * 0.68, size * 0.05, size * 0.28);
        gc.fillOval(size * 0.31, -size * 0.64, size * 0.05, size * 0.25);

        gc.setFill(Color.BLACK);
        gc.fillOval(size * 0.34, -size * 0.22, size * 0.045, size * 0.045);
        gc.setFill(Color.WHITE);
        gc.fillOval(-size * 0.52, -size * 0.08, size * 0.18, size * 0.18);
    }

    private void drawDeer(GraphicsContext gc, Deer deer, double size) {
        gc.setFill(Color.web("#9a6236"));
        gc.fillOval(-size * 0.48, -size * 0.22, size * 0.78, size * 0.42);
        gc.fillOval(size * 0.12, -size * 0.42, size * 0.34, size * 0.3);

        gc.setStroke(Color.web("#4b2a16"));
        gc.setLineWidth(Math.max(1, size * 0.06));
        gc.strokeLine(-size * 0.28, size * 0.1, -size * 0.34, size * 0.48);
        gc.strokeLine(size * 0.08, size * 0.08, size * 0.04, size * 0.48);
        gc.strokeLine(size * 0.25, -size * 0.42, size * 0.18, -size * 0.68);
        gc.strokeLine(size * 0.25, -size * 0.42, size * 0.4, -size * 0.66);
        gc.strokeLine(size * 0.18, -size * 0.6, size * 0.08, -size * 0.72);
        gc.strokeLine(size * 0.38, -size * 0.6, size * 0.5, -size * 0.72);

        gc.setFill(Color.web("#f4e3cf"));
        gc.fillOval(size * 0.32, -size * 0.28, size * 0.08, size * 0.08);
    }

    private void drawWolf(GraphicsContext gc, Wolf wolf, double size) {
        gc.setFill(Color.web("#7d8790"));
        gc.fillOval(-size * 0.48, -size * 0.2, size * 0.82, size * 0.38);
        gc.fillPolygon(
            new double[] {size * 0.2, size * 0.58, size * 0.28},
            new double[] {-size * 0.32, -size * 0.1, size * 0.08},
            3
        );

        gc.setFill(Color.web("#4f5960"));
        gc.fillPolygon(
            new double[] {size * 0.22, size * 0.34, size * 0.28},
            new double[] {-size * 0.34, -size * 0.58, -size * 0.24},
            3
        );
        gc.fillPolygon(
            new double[] {-size * 0.42, -size * 0.72, -size * 0.46},
            new double[] {-size * 0.16, -size * 0.42, size * 0.02},
            3
        );

        gc.setFill(Color.BLACK);
        gc.fillOval(size * 0.39, -size * 0.16, size * 0.045, size * 0.045);
    }

    private void drawTiger(GraphicsContext gc, Tiger tiger, double size) {
        gc.setFill(Color.web("#f08a24"));
        gc.fillOval(-size * 0.5, -size * 0.24, size * 0.88, size * 0.46);
        gc.fillOval(size * 0.16, -size * 0.42, size * 0.38, size * 0.34);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(Math.max(1, size * 0.055));
        for (int i = 0; i < 4; i++) {
            double x = (-0.28 + i * 0.18) * size;
            gc.strokeLine(x, -size * 0.2, x + size * 0.08, size * 0.12);
        }
        gc.strokeLine(size * 0.3, -size * 0.38, size * 0.36, -size * 0.12);
        gc.strokeLine(size * 0.44, -size * 0.35, size * 0.48, -size * 0.14);

        gc.setFill(Color.WHITE);
        gc.fillOval(size * 0.38, -size * 0.18, size * 0.1, size * 0.08);
        gc.setFill(Color.BLACK);
        gc.fillOval(size * 0.36, -size * 0.26, size * 0.045, size * 0.045);
    }

    private void drawHunter(GraphicsContext gc, Hunter hunter, double size) {
        gc.setStroke(Color.web("#3d2515"));
        gc.setLineWidth(Math.max(1, size * 0.07));
        gc.strokeOval(size * 0.26, -size * 0.32, size * 0.24, size * 0.64);
        gc.strokeLine(size * 0.28, 0, size * 0.5, -size * 0.28);

        gc.setFill(Color.web("#b33a3a"));
        gc.fillRoundRect(-size * 0.18, -size * 0.12, size * 0.34, size * 0.46, 4, 4);
        gc.setFill(Color.web("#f0c7a8"));
        gc.fillOval(-size * 0.18, -size * 0.46, size * 0.32, size * 0.32);
        gc.setFill(Color.web("#2f2f2f"));
        gc.fillRect(-size * 0.25, -size * 0.48, size * 0.46, size * 0.08);
        gc.fillRoundRect(-size * 0.14, -size * 0.58, size * 0.26, size * 0.12, 3, 3);
    }

    private void drawElephant(GraphicsContext gc, Elephant elephant, double size) {
        gc.setFill(Color.web("#9ba3ad"));
        gc.fillOval(-size * 0.52, -size * 0.32, size * 0.88, size * 0.58);
        gc.fillOval(size * 0.08, -size * 0.44, size * 0.5, size * 0.5);

        gc.setFill(Color.web("#858d98"));
        gc.fillOval(size * 0.0, -size * 0.38, size * 0.28, size * 0.36);
        gc.fillRoundRect(size * 0.42, -size * 0.12, size * 0.16, size * 0.52, 8, 8);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(Math.max(1, size * 0.05));
        gc.strokeLine(size * 0.43, size * 0.02, size * 0.62, size * 0.18);
        gc.strokeLine(size * 0.25, size * 0.02, size * 0.08, size * 0.18);

        gc.setFill(Color.BLACK);
        gc.fillOval(size * 0.38, -size * 0.25, size * 0.045, size * 0.045);
    }

    private void drawFallback(GraphicsContext gc, double size) {
        gc.setFill(Color.WHITE);
        gc.fillOval(-size * 0.35, -size * 0.35, size * 0.7, size * 0.7);
    }

    private void drawStateBadge(GraphicsContext gc, Animal animal, double size) {
        if (size < 9) return;

        String badge = switch (animal.getState()) {
            case EATING -> "+";
            case DRINKING -> "~";
            case SLEEPING -> "Z";
            case ATTACKING -> "!";
            case FLEEING -> ">";
            case HIDING -> "*";
            default -> "";
        };

        if (badge.isEmpty()) {
            return;
        }

        double r = Math.max(5, size * 0.18);
        gc.setFill(Color.rgb(0, 0, 0, 0.65));
        gc.fillOval(-r, -size * 0.72 - r, r * 2, r * 2);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(BADGE_FONT);
        gc.fillText(badge, 0, -size * 0.72 + r * 0.35);
    }
}
