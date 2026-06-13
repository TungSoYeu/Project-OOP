package com.ecosim.model;

import com.ecosim.util.Vector2D;

import java.util.Random;

/**
 * Lớp trừu tượng cho thực vật (Cỏ, Cây ăn quả).
 * Thực vật không di chuyển, tự phát triển và sinh sôi theo thời gian.
 */
public abstract class Plant extends Entity {
    /** Giá trị dinh dưỡng khi bị ăn */
    protected double nutritionValue;

    /** Tốc độ phát triển (đơn vị size / giây) */
    protected double growthRate;

    /** Kích thước tối đa */
    protected double maxSize;

    /** Đã trưởng thành chưa (đủ lớn để bị ăn / sinh sôi) */
    protected boolean mature;

    /** Thời gian kể từ lần sinh sôi cuối */
    protected double timeSinceLastSpread;

    /** Khoảng thời gian giữa các lần sinh sôi (giây) */
    protected double spreadInterval;

    /** Bán kính lan rộng khi sinh sôi (tiles) */
    protected double spreadRadius;

    protected final Random random = new Random();

    protected Plant(String name, Vector2D position, int priority, double size,
                    double nutritionValue, double growthRate, double maxSize,
                    double spreadInterval, double spreadRadius) {
        super(name, position, priority, size);
        this.nutritionValue = nutritionValue;
        this.growthRate = growthRate;
        this.maxSize = maxSize;
        this.mature = false;
        this.timeSinceLastSpread = 0;
        this.spreadInterval = spreadInterval;
        this.spreadRadius = spreadRadius;
    }

    @Override
    public void update(double deltaTime, WorldMap worldMap) {
        if (!alive) return;
        grow(deltaTime);
    }

    /** Phát triển theo thời gian */
    protected void grow(double deltaTime) {
        if (size < maxSize) {
            size += growthRate * deltaTime;
            if (size >= maxSize) {
                size = maxSize;
                mature = true;
            }
        }
        timeSinceLastSpread += deltaTime;
    }

    /** Kiểm tra có thể sinh sôi được không */
    public boolean canSpread() {
        return mature && timeSinceLastSpread >= spreadInterval;
    }

    /** Reset timer sinh sôi sau khi đã spread */
    public void resetSpreadTimer() {
        timeSinceLastSpread = 0;
    }

    /** Tạo vị trí ngẫu nhiên gần đây để sinh sôi */
    public Vector2D getSpreadPosition() {
        double angle = random.nextDouble() * 2 * Math.PI;
        double dist = 1 + random.nextDouble() * spreadRadius;
        return position.add(new Vector2D(
            Math.cos(angle) * dist,
            Math.sin(angle) * dist
        ));
    }

    /**
     * Tạo thực thể con (factory method).
     * Mỗi loại thực vật override để tạo đúng loại.
     */
    public abstract Plant createOffspring(Vector2D position);

    /** Có thể ăn được không (đã trưởng thành) */
    public boolean isEdible() {
        return mature && alive;
    }

    /**
     * Bị ăn - giảm kích thước hoặc chết từ từ theo thời gian.
     * @return lượng dinh dưỡng cung cấp
     */
    public double beEaten(double deltaTime) {
        if (!isEdible()) return 0;
        
        // Mất 3 giây để ăn hết 1 cây (kích thước tối đa)
        double sizeLoss = (maxSize / 3.0) * deltaTime;
        if (sizeLoss > size) sizeLoss = size;

        double nutrition = (sizeLoss / maxSize) * nutritionValue;
        size -= sizeLoss;
        if (size <= 0) {
            alive = false;
            size = 0;
        } else if (size < maxSize * 0.5) {
            mature = false; // Cần mọc lại
        }
        return nutrition;
    }

    @Override
    public double getTerrainSpeedModifier(TerrainType terrain) {
        return 0; // Thực vật không di chuyển
    }

    @Override
    public boolean canTraverse(TerrainType terrain) {
        return false; // Thực vật không di chuyển
    }

    // ===== Getters =====
    public double getNutritionValue() { return nutritionValue; }
    public double getGrowthRate() { return growthRate; }
    public double getMaxSize() { return maxSize; }
    public boolean isMature() { return mature; }
    public double getSpreadRadius() { return spreadRadius; }
}
