package com.smartrestaurant.backend.dto;

public class PasswordResetResponse {
    private String message;
    private String username;  // Return username after OTP verification

    public PasswordResetResponse(String message) {
        this.message = message;
    }

    public PasswordResetResponse(String message, String username) {
        this.message = message;
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
