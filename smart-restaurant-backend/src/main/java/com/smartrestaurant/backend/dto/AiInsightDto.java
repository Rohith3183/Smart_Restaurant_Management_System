package com.smartrestaurant.backend.dto;

public class AiInsightDto {
    private String summary;

    public AiInsightDto() {}

    public AiInsightDto(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
