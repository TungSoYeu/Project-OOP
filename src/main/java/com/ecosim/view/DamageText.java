package com.ecosim.view;

import com.ecosim.util.Vector2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Hiệu ứng chữ damage bay lên rồi mờ dần.
 * Tọa độ gốc lưu theo world position để bám đúng vị trí animal khi camera pan/zoom.
 */
public class DamageText {
    private static final double MAX_LIFETIME = 0.9;
    private static final double FLOAT_SPEED = 18.0;
    private static final Font FONT = Font.font("Arial", FontWeight.BOLD, 13);
    private static final Color DAMAGE_COLOR = Color.rgb(255, 80, 70);

    private final Vector2D worldPosition;
    private final int damage;
    private double lifeTime;
    private double offsetY;

    public DamageText(Vector2D worldPosition, double damage) {
        this.worldPosition = worldPosition;
        this.damage = Math.max(1, (int) Math.round(damage));
        this.lifeTime = MAX_LIFETIME;
        this.offsetY = 0;
    }

    public void update(double deltaTime) {
        lifeTime -= deltaTime;
        offsetY -= FLOAT_SPEED * deltaTime;
    }

    public void draw(GraphicsContext gc, Camera camera) {
        double alpha = Math.max(0, lifeTime / MAX_LIFETIME);
        double x = camera.worldToScreenX(worldPosition.getX());
        double y = camera.worldToScreenY(worldPosition.getY()) + offsetY - 12;

        gc.save();
        gc.setGlobalAlpha(alpha);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(FONT);

        String text = "-" + damage;
        gc.setFill(Color.rgb(40, 0, 0, 0.75));
        gc.fillText(text, x + 1, y + 1);
        gc.setFill(DAMAGE_COLOR);
        gc.fillText(text, x, y);

        gc.restore();
    }

    public boolean isExpired() {
        return lifeTime <= 0;
    }
}
