package com.smartrestaurant.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateAlertRequest {
    @NotBlank
    private String message;
    
    @NotBlank
    private String fromRole;

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFromRole() {
        return fromRole;
    }

    public void setFromRole(String fromRole) {
        this.fromRole = fromRole;
    }
}
