package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.CreateFeedbackRequest;
import com.smartrestaurant.backend.dto.FeedbackAnalyticsDto;
import com.smartrestaurant.backend.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // Public: customer submits feedback (restaurantCode in path)
    @PostMapping("/public/{restaurantCode}")
    public ResponseEntity<Void> submitPublicFeedback(
            @PathVariable String restaurantCode,
            @Valid @RequestBody CreateFeedbackRequest request) {
        feedbackService.submitFeedback(restaurantCode, request);
        return ResponseEntity.ok().build();
    }

    // Owner: get analytics for dashboard
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<FeedbackAnalyticsDto> getFeedbackAnalytics(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(feedbackService.getFeedbackAnalytics(restaurantCode));
    }
}
