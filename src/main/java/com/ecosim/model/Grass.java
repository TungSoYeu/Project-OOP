package com.ecosim.model;

import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

/**
 * Cỏ - thực vật cơ bản, thức ăn cho động vật ăn cỏ.
 * Tự mọc lại sau khi bị ăn, lan rộng theo thời gian.
 */
public class Grass extends Plant {

    public Grass(Vector2D position) {
        super(
            "Cỏ",              // name
            position,           // position
            Constants.PRIORITY_PLANT, // priority
            0.3,                // initial size
            15.0,               // nutritionValue
            0.1,                // growthRate (size/giây)
            0.5,                // maxSize
            20.0,               // spreadInterval (giây)
            3.0                 // spreadRadius (tiles)
        );
    }

    @Override
    public Plant createOffspring(Vector2D position) {
        Grass child = new Grass(position);
        child.size = 0.1; // Mới mọc, rất nhỏ
        return child;
    }

    @Override
    public String getTypeName() {
        return "Cỏ";
    }
}
