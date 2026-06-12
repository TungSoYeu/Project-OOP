package com.ecosim.model;

import com.ecosim.util.Vector2D;

/**
 * Event dữ liệu thuần cho một lần animal bị mất máu.
 * Model chỉ báo "mất bao nhiêu máu ở đâu"; View sẽ quyết định vẽ hiệu ứng.
 */
public class DamageEvent {
    private final Vector2D position;
    private final double damage;

    public DamageEvent(Vector2D position, double damage) {
        this.position = position;
        this.damage = damage;
    }

    public Vector2D getPosition() { return position; }
    public double getDamage() { return damage; }
}
