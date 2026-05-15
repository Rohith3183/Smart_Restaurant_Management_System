package com.smartrestaurant.backend.dto;

public class LoginResponse {

    private String token;
    private String username;
    private String role;
    private String restaurantCode;
    private String restaurantName;

    // Constructor
    public LoginResponse(String token, String username, String role, String restaurantCode, String restaurantName) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.restaurantCode = restaurantCode;
        this.restaurantName = restaurantName;
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRestaurantCode() {
        return restaurantCode;
    }

    public void setRestaurantCode(String restaurantCode) {
        this.restaurantCode = restaurantCode;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
}
