package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.CreateFeedbackRequest;
import com.smartrestaurant.backend.dto.FeedbackAnalyticsDto;

public interface FeedbackService {

    void submitFeedback(String restaurantCode, CreateFeedbackRequest request);

    FeedbackAnalyticsDto getFeedbackAnalytics(String restaurantCode);
}
