package com.ecosim.model;

import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

/**
 * Cây ăn quả - thực vật có giá trị dinh dưỡng cao.
 * Mọc trong rừng, cho quả khi trưởng thành.
 */
public class FruitTree extends Plant {
    /** Số quả hiện có */
    private int fruitCount;

    /** Số quả tối đa */
    private final int maxFruits;

    /** Thời gian mọc mỗi quả (giây) */
    private double fruitGrowthTimer;
    private final double fruitGrowthInterval;

    public FruitTree(Vector2D position) {
        super(
            "Cây ăn quả",       // name
            position,            // position
            Constants.PRIORITY_PLANT, // priority
            0.8,                 // initial size (lớn hơn cỏ)
            30.0,                // nutritionValue (mỗi quả)
            0.05,                // growthRate
            1.5,                 // maxSize
            60.0,                // spreadInterval (lâu hơn cỏ)
            5.0                  // spreadRadius
        );
        this.maxFruits = 5;
        this.fruitCount = 0;
        this.fruitGrowthTimer = 0;
        this.fruitGrowthInterval = 15.0; // 15 giây mọc 1 quả
    }

    @Override
    public void update(double deltaTime, WorldMap worldMap) {
        super.update(deltaTime, worldMap);
        if (!alive || !mature) return;

        // Mọc quả khi đã trưởng thành
        fruitGrowthTimer += deltaTime;
        if (fruitGrowthTimer >= fruitGrowthInterval && fruitCount < maxFruits) {
            fruitCount++;
            fruitGrowthTimer = 0;
        }
    }

    /** Hái quả - trả về dinh dưỡng */
    public double harvestFruit() {
        if (fruitCount <= 0) return 0;
        fruitCount--;
        return nutritionValue;
    }

    @Override
    public double beEaten() {
        // Ưu tiên ăn quả trước
        if (fruitCount > 0) {
            return harvestFruit();
        }
        return super.beEaten();
    }

    @Override
    public boolean isEdible() {
        return alive && (fruitCount > 0 || mature);
    }

    @Override
    public Plant createOffspring(Vector2D position) {
        FruitTree child = new FruitTree(position);
        child.size = 0.3;
        return child;
    }

    @Override
    public String getTypeName() {
        return "Cây ăn quả";
    }

    public int getFruitCount() { return fruitCount; }
    public int getMaxFruits() { return maxFruits; }
}
