package com.smartrestaurant.backend.dto;

public class MenuCategoryDto {
    private Long id;
    private String name;

    // Constructors
    public MenuCategoryDto() {}

    public MenuCategoryDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
