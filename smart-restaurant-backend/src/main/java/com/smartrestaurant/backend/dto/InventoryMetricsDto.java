package com.smartrestaurant.backend.dto;

public class InventoryMetricsDto {

    // percentage 0–100
    private Double wastagePercent;

    // number of stockouts this week
    private Integer stockoutsThisWeek;

    // e.g. "LOW", "MEDIUM", "HIGH"
    private String theftRiskLevel;

    // e.g. "LOW", "MEDIUM", "HIGH"
    private String expiryRiskLevel;

    public Double getWastagePercent() {
        return wastagePercent;
    }

    public void setWastagePercent(Double wastagePercent) {
        this.wastagePercent = wastagePercent;
    }

    public Integer getStockoutsThisWeek() {
        return stockoutsThisWeek;
    }

    public void setStockoutsThisWeek(Integer stockoutsThisWeek) {
        this.stockoutsThisWeek = stockoutsThisWeek;
    }

    public String getTheftRiskLevel() {
        return theftRiskLevel;
    }

    public void setTheftRiskLevel(String theftRiskLevel) {
        this.theftRiskLevel = theftRiskLevel;
    }

    public String getExpiryRiskLevel() {
        return expiryRiskLevel;
    }

    public void setExpiryRiskLevel(String expiryRiskLevel) {
        this.expiryRiskLevel = expiryRiskLevel;
    }
}
