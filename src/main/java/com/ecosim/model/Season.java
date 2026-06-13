package com.ecosim.model;


/**
 * Các mùa trong năm, ảnh hưởng đến hệ sinh thái.
 * Mỗi mùa có hệ số sinh sản, tốc độ mọc thực vật, và lượng nước.
 */
public enum Season {
    /** Mùa xuân - sinh sản cao, cây mọc nhanh */
    SPRING("Mùa Xuân", "🌸", 2.0, 2.0, 1.0, "#98FB98"),

    /** Mùa hạ - bình thường nhưng nước giảm */
    SUMMER("Mùa Hạ", "☀️", 1.0, 1.0, 0.6, "#FFD700"),

    /** Mùa thu - sinh sản giảm, cây chậm lại */
    AUTUMN("Mùa Thu", "🍂", 0.5, 0.5, 0.8, "#FF8C00"),

    /** Mùa đông - rất ít sinh sản, thực vật khô héo */
    WINTER("Mùa Đông", "❄️", 0.2, 0.1, 0.3, "#B0C4DE");

    private final String displayName;
    private final String icon;
    private final double reproductionMultiplier;
    private final double plantGrowthMultiplier;
    private final double waterMultiplier;
    private final String themeHexColor;

    Season(String displayName, String icon, double reproductionMultiplier,
            double plantGrowthMultiplier, double waterMultiplier, String themeHexColor) {
        this.displayName = displayName;
        this.icon = icon;
        this.reproductionMultiplier = reproductionMultiplier;
        this.plantGrowthMultiplier = plantGrowthMultiplier;
        this.waterMultiplier = waterMultiplier;
        this.themeHexColor = themeHexColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public double getReproductionMultiplier() {
        return reproductionMultiplier;
    }

    public double getPlantGrowthMultiplier() {
        return plantGrowthMultiplier;
    }

    public double getWaterMultiplier() {
        return waterMultiplier;
    }

    public String getThemeHexColor() {
        return themeHexColor;
    }

    public Season next() {
        Season[] seasons = values();
        return seasons[(this.ordinal() + 1) % seasons.length];
    }
}
