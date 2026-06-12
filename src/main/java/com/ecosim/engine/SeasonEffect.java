package com.ecosim.engine;

import com.ecosim.model.Season;

/**
 * Bảng hiệu ứng sinh thái theo mùa.
 * Các hệ số này được SimulationEngine áp vào Animal/Plant mỗi tick.
 */
public class SeasonEffect {
    private final double plantGrowthMultiplier;
    private final double fruitGrowthMultiplier;
    private final double hungerDecayMultiplier;
    private final double thirstDecayMultiplier;
    private final double reproductionChanceMultiplier;
    private final double plantSpreadChance;
    private final boolean harshForPlants;

    private SeasonEffect(
        double plantGrowthMultiplier,
        double fruitGrowthMultiplier,
        double hungerDecayMultiplier,
        double thirstDecayMultiplier,
        double reproductionChanceMultiplier,
        double plantSpreadChance,
        boolean harshForPlants
    ) {
        this.plantGrowthMultiplier = plantGrowthMultiplier;
        this.fruitGrowthMultiplier = fruitGrowthMultiplier;
        this.hungerDecayMultiplier = hungerDecayMultiplier;
        this.thirstDecayMultiplier = thirstDecayMultiplier;
        this.reproductionChanceMultiplier = reproductionChanceMultiplier;
        this.plantSpreadChance = plantSpreadChance;
        this.harshForPlants = harshForPlants;
    }

    public static SeasonEffect forSeason(Season season) {
        return switch (season) {
            case SPRING -> new SeasonEffect(1.5, 1.5, 0.9, 1.0, 1.0, 0.035, false);
            case SUMMER -> new SeasonEffect(0.4, 0.5, 1.0, 1.8, 0.25, 0.008, true);
            case AUTUMN -> new SeasonEffect(0.8, 1.2, 1.0, 1.0, 0.15, 0.006, false);
            case WINTER -> new SeasonEffect(0.1, 0.0, 1.3, 0.8, 0.0, 0.0, true);
        };
    }

    public double getPlantGrowthMultiplier() { return plantGrowthMultiplier; }
    public double getFruitGrowthMultiplier() { return fruitGrowthMultiplier; }
    public double getHungerDecayMultiplier() { return hungerDecayMultiplier; }
    public double getThirstDecayMultiplier() { return thirstDecayMultiplier; }
    public double getReproductionChanceMultiplier() { return reproductionChanceMultiplier; }
    public double getPlantSpreadChance() { return plantSpreadChance; }
    public boolean isHarshForPlants() { return harshForPlants; }
}
