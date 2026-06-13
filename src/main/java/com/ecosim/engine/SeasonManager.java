package com.ecosim.engine;

import com.ecosim.model.Season;
import com.ecosim.util.Constants;

/**
 * Quản lý hệ thống mùa (Season System).
 * Tự động chuyển mùa theo thời gian thực.
 */
public class SeasonManager {
    private Season currentSeason;
    private double seasonTimer;
    private final double seasonDuration;
    private int yearCount;

    public SeasonManager() {
        this.currentSeason = Season.SPRING;
        this.seasonTimer = 0;
        this.seasonDuration = Constants.SEASON_DURATION_SECONDS;
        this.yearCount = 1;
    }

    /**
     * Cập nhật mùa theo thời gian.
     * @param deltaTime thời gian kể từ tick trước (giây)
     * @return true nếu vừa đổi mùa
     */
    public boolean update(double deltaTime) {
        seasonTimer += deltaTime;
        if (seasonTimer >= seasonDuration) {
            seasonTimer = 0;
            currentSeason = currentSeason.next();

            // Đếm năm khi quay lại xuân
            if (currentSeason == Season.SPRING) {
                yearCount++;
            }
            return true;
        }
        return false;
    }

    /** Tiến độ của mùa hiện tại (0.0 → 1.0) */
    public double getSeasonProgress() {
        return seasonTimer / seasonDuration;
    }

    public Season getCurrentSeason() { return currentSeason; }
    public int getYearCount() { return yearCount; }
    public double getSeasonTimer() { return seasonTimer; }
    public double getSeasonDuration() { return seasonDuration; }
}
