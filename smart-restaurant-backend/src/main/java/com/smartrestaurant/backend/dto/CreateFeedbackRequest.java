package com.smartrestaurant.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class CreateFeedbackRequest {

    @Min(1) @Max(5)
    private int food;

    @Min(1) @Max(5)
    private int ambiance;

    @Min(1) @Max(5)
    private int ingredients;

    @Min(1) @Max(5)
    private int service;

    @Min(1) @Max(5)
    private int cleanliness;

    @Min(1) @Max(5)
    private int valueForMoney;

    @Min(1) @Max(5)
    private int overall;

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getAmbiance() {
        return ambiance;
    }

    public void setAmbiance(int ambiance) {
        this.ambiance = ambiance;
    }

    public int getIngredients() {
        return ingredients;
    }

    public void setIngredients(int ingredients) {
        this.ingredients = ingredients;
    }

    public int getService() {
        return service;
    }

    public void setService(int service) {
        this.service = service;
    }

    public int getCleanliness() {
        return cleanliness;
    }

    public void setCleanliness(int cleanliness) {
        this.cleanliness = cleanliness;
    }

    public int getValueForMoney() {
        return valueForMoney;
    }

    public void setValueForMoney(int valueForMoney) {
        this.valueForMoney = valueForMoney;
    }

    public int getOverall() {
        return overall;
    }

    public void setOverall(int overall) {
        this.overall = overall;
    }
}
