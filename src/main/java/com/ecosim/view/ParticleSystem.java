package com.ecosim.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParticleSystem {

    private static class Particle {
        double x, y;
        double vx, vy;
        double life, maxLife;
        Color color;
        double size;
        String type; // "dust", "blood", "heart", "zzz"

        boolean isDead() { return life <= 0; }
    }

    private final List<Particle> particles = new ArrayList<>();

    public void update(double deltaTime) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.x += p.vx * deltaTime;
            p.y += p.vy * deltaTime;
            p.life -= deltaTime;
            if (p.isDead()) {
                it.remove();
            }
        }
    }

    public void render(GraphicsContext gc, Camera camera) {
        for (Particle p : particles) {
            if (!camera.isVisible(p.x, p.y, p.size)) continue;

            double sx = camera.worldToScreenX(p.x);
            double sy = camera.worldToScreenY(p.y);
            double sSize = camera.entityScreenSize(p.size);
            double opacity = Math.max(0, p.life / p.maxLife);

            gc.save();
            gc.setGlobalAlpha(opacity);

            switch (p.type) {
                case "dust" -> {
                    gc.setFill(p.color);
                    gc.fillOval(sx - sSize/2, sy - sSize/2, sSize, sSize);
                }
                case "blood" -> {
                    gc.setFill(p.color);
                    gc.fillOval(sx - sSize/2, sy - sSize/2, sSize, sSize * 1.5);
                }
                case "heart" -> {
                    gc.setFill(Color.RED);
                    gc.setFont(javafx.scene.text.Font.font(sSize * 2));
                    gc.fillText("❤", sx - sSize, sy + sSize);
                }
                case "zzz" -> {
                    gc.setFill(Color.WHITE);
                    gc.setFont(javafx.scene.text.Font.font(sSize * 2));
                    gc.fillText("Zzz", sx - sSize, sy);
                }
            }
            gc.restore();
        }
    }

    public void emitDust(double worldX, double worldY) {
        for (int i = 0; i < 3; i++) {
            Particle p = new Particle();
            p.x = worldX + (Math.random() - 0.5) * 0.2;
            p.y = worldY + (Math.random() - 0.5) * 0.2;
            p.vx = (Math.random() - 0.5) * 0.5;
            p.vy = (Math.random() - 0.5) * 0.5 + 0.2; // Bụi bay lên
            p.life = p.maxLife = 0.5 + Math.random() * 0.5;
            p.size = 0.1 + Math.random() * 0.15;
            p.color = Color.rgb(200, 190, 170, 0.6);
            p.type = "dust";
            particles.add(p);
        }
    }

    public void emitBlood(double worldX, double worldY) {
        for (int i = 0; i < 5; i++) {
            Particle p = new Particle();
            p.x = worldX;
            p.y = worldY;
            p.vx = (Math.random() - 0.5) * 2;
            p.vy = (Math.random() - 0.5) * 2;
            p.life = p.maxLife = 0.3 + Math.random() * 0.3;
            p.size = 0.05 + Math.random() * 0.1;
            p.color = Color.RED;
            p.type = "blood";
            particles.add(p);
        }
    }

    public void emitHeart(double worldX, double worldY) {
        Particle p = new Particle();
        p.x = worldX;
        p.y = worldY - 0.5;
        p.vx = (Math.random() - 0.5) * 0.2;
        p.vy = -0.5 - Math.random() * 0.5; // Bay lên
        p.life = p.maxLife = 1.0 + Math.random() * 0.5;
        p.size = 0.3;
        p.type = "heart";
        particles.add(p);
    }

    public void emitZzz(double worldX, double worldY) {
        Particle p = new Particle();
        p.x = worldX + 0.2;
        p.y = worldY - 0.5;
        p.vx = 0.1;
        p.vy = -0.3;
        p.life = p.maxLife = 1.5;
        p.size = 0.2;
        p.type = "zzz";
        particles.add(p);
    }
}
