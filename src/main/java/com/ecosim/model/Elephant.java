package com.ecosim.model;

import com.ecosim.strategy.PassiveStrategy;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.List;

/**
 * Voi - động vật đầu bảng với priority cao nhất.
 * Không sợ bất kỳ kẻ thù nào.
 * Mọi loài phải nhường đường.
 * Di chuyển chậm nhưng mạnh mẽ, gần như miễn nhiễm tấn công.
 */
public class Elephant extends Animal {

    /** Sức mạnh khi lao tới (knock back) */
    private final double chargeForce;

    /** Miễn nhiễm phần lớn sát thương */
    private final boolean immuneToSmallAttacks;

    public Elephant(Vector2D position) {
        super(
            "Voi",                      // name
            position,                    // position
            Constants.PRIORITY_ELEPHANT, // priority (CAO NHẤT)
            1.0,                         // size (lớn nhất)
            200.0,                       // maxHealth
            Constants.SPEED_ELEPHANT,    // maxSpeed (chậm nhất)
            30.0,                        // attackPower
            Constants.SIGHT_ELEPHANT     // sightRange
        );
        this.chargeForce = 50.0;
        this.immuneToSmallAttacks = true;

        // Không sợ ai
        this.naturalEnemies = List.of();

        // Ăn cỏ (ăn chay)
        this.preyTypes = List.of(Grass.class, FruitTree.class);

        // Strategy: Passive - đi lang thang ăn cỏ, không sợ ai
        this.defaultStrategy = new PassiveStrategy();
        this.strategy = this.defaultStrategy;
    }

    @Override
    public void takeDamage(double damage) {
        // Voi miễn nhiễm với sát thương nhỏ (< 20)
        if (immuneToSmallAttacks && damage < 20) {
            return; // Bỏ qua sát thương nhỏ
        }
        // Sát thương lớn chỉ gây nửa damage (da dày)
        super.takeDamage(damage * 0.5);
    }

    @Override
    public double getTerrainSpeedModifier(TerrainType terrain) {
        return switch (terrain) {
            case GRASSLAND -> 1.0;
            case FOREST -> 0.5;    // Voi rất to, khó đi trong rừng
            case BUSH -> 0.8;       // Voi giẫm bẹp bụi rậm
            case MUD -> 0.3;
            case WATER -> 0.6;      // Voi có thể qua nước nông
            case ROCK -> 0.0;
        };
    }

    @Override
    public boolean canTraverse(TerrainType terrain) {
        // Voi đi được hầu hết nơi, kể cả nước nông
        return terrain != TerrainType.ROCK;
    }

    @Override
    public String getTypeName() {
        return "Voi";
    }

    public double getChargeForce() { return chargeForce; }
    public boolean isImmuneToSmallAttacks() { return immuneToSmallAttacks; }
}
