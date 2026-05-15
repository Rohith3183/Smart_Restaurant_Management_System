package com.smartrestaurant.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public class RestaurantRegistrationRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    private String location;
    private String contact;
    private Integer tableCount;
    private Integer capacity;

    @NotBlank
    private String ownerUsername;

    @NotBlank
    private String ownerPassword;
    
    @Email  // ADD THIS
    @NotBlank  // ADD THIS
    private String ownerEmail;  // ADD THIS
    
    private boolean franchise;          // true if under a franchise
    private String franchiseCode;       // required when franchise == true

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Integer getTableCount() {
        return tableCount;
    }

    public void setTableCount(Integer tableCount) {
        this.tableCount = tableCount;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOwnerPassword() {
        return ownerPassword;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }
    
    public String getOwnerEmail() {  // ADD THIS
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {  // ADD THIS
        this.ownerEmail = ownerEmail;
    }
    
    public boolean isFranchise() { return franchise; }
    public void setFranchise(boolean franchise) { this.franchise = franchise; }

    public String getFranchiseCode() { return franchiseCode; }
    public void setFranchiseCode(String franchiseCode) { this.franchiseCode = franchiseCode; }
}
