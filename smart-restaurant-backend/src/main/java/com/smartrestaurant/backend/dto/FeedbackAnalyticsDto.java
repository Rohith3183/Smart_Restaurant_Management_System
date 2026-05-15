package com.smartrestaurant.backend.dto;

import java.util.Map;

public class FeedbackAnalyticsDto {

    // averages
    private double avgFood;
    private double avgAmbiance;
    private double avgIngredients;
    private double avgService;
    private double avgCleanliness;
    private double avgValueForMoney;
    private double avgOverall;

    // histograms
    private Map<Integer, Long> foodHistogram;
    private Map<Integer, Long> ambianceHistogram;
    private Map<Integer, Long> ingredientsHistogram;
    private Map<Integer, Long> serviceHistogram;
    private Map<Integer, Long> cleanlinessHistogram;
    private Map<Integer, Long> valueHistogram;
    private Map<Integer, Long> overallHistogram;

    private double restaurantRating;

    // getters & setters

    public double getAvgFood() { return avgFood; }
    public void setAvgFood(double avgFood) { this.avgFood = avgFood; }

    public double getAvgAmbiance() { return avgAmbiance; }
    public void setAvgAmbiance(double avgAmbiance) { this.avgAmbiance = avgAmbiance; }

    public double getAvgIngredients() { return avgIngredients; }
    public void setAvgIngredients(double avgIngredients) { this.avgIngredients = avgIngredients; }

    public double getAvgService() { return avgService; }
    public void setAvgService(double avgService) { this.avgService = avgService; }

    public double getAvgCleanliness() { return avgCleanliness; }
    public void setAvgCleanliness(double avgCleanliness) { this.avgCleanliness = avgCleanliness; }

    public double getAvgValueForMoney() { return avgValueForMoney; }
    public void setAvgValueForMoney(double avgValueForMoney) { this.avgValueForMoney = avgValueForMoney; }

    public double getAvgOverall() { return avgOverall; }
    public void setAvgOverall(double avgOverall) { this.avgOverall = avgOverall; }

    public Map<Integer, Long> getFoodHistogram() { return foodHistogram; }
    public void setFoodHistogram(Map<Integer, Long> foodHistogram) { this.foodHistogram = foodHistogram; }

    public Map<Integer, Long> getAmbianceHistogram() { return ambianceHistogram; }
    public void setAmbianceHistogram(Map<Integer, Long> ambianceHistogram) { this.ambianceHistogram = ambianceHistogram; }

    public Map<Integer, Long> getIngredientsHistogram() { return ingredientsHistogram; }
    public void setIngredientsHistogram(Map<Integer, Long> ingredientsHistogram) { this.ingredientsHistogram = ingredientsHistogram; }

    public Map<Integer, Long> getServiceHistogram() { return serviceHistogram; }
    public void setServiceHistogram(Map<Integer, Long> serviceHistogram) { this.serviceHistogram = serviceHistogram; }

    public Map<Integer, Long> getCleanlinessHistogram() { return cleanlinessHistogram; }
    public void setCleanlinessHistogram(Map<Integer, Long> cleanlinessHistogram) {
        this.cleanlinessHistogram = cleanlinessHistogram;
    }

    public Map<Integer, Long> getValueHistogram() { return valueHistogram; }
    public void setValueHistogram(Map<Integer, Long> valueHistogram) { this.valueHistogram = valueHistogram; }

    public Map<Integer, Long> getOverallHistogram() { return overallHistogram; }
    public void setOverallHistogram(Map<Integer, Long> overallHistogram) {
        this.overallHistogram = overallHistogram;
    }

    public double getRestaurantRating() { return restaurantRating; }
    public void setRestaurantRating(double restaurantRating) { this.restaurantRating = restaurantRating; }
}
