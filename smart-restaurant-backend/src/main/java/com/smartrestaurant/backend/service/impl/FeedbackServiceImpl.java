package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.CreateFeedbackRequest;
import com.smartrestaurant.backend.dto.FeedbackAnalyticsDto;
import com.smartrestaurant.backend.entity.Feedback;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.repository.FeedbackRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FeedbackServiceImpl implements FeedbackService {

    private final RestaurantRepository restaurantRepository;
    private final FeedbackRepository feedbackRepository;

    public FeedbackServiceImpl(RestaurantRepository restaurantRepository,
                               FeedbackRepository feedbackRepository) {
        this.restaurantRepository = restaurantRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public void submitFeedback(String restaurantCode, CreateFeedbackRequest request) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        Feedback feedback = Feedback.builder()
                .restaurant(restaurant)
                .food(request.getFood())
                .ambiance(request.getAmbiance())
                .ingredients(request.getIngredients())
                .service(request.getService())
                .cleanliness(request.getCleanliness())
                .valueForMoney(request.getValueForMoney())
                .overall(request.getOverall())
                .createdAt(LocalDateTime.now())
                .build();

        feedbackRepository.save(feedback);
    }

    @Override
    public FeedbackAnalyticsDto getFeedbackAnalytics(String restaurantCode) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        List<Feedback> all = feedbackRepository.findByRestaurant(restaurant);
        FeedbackAnalyticsDto dto = new FeedbackAnalyticsDto();

        if (all.isEmpty()) {
            dto.setRestaurantRating(0.0);
            return dto;
        }

        int n = all.size();
        double sumFood = 0, sumAmb = 0, sumIng = 0, sumServ = 0,
               sumClean = 0, sumValue = 0, sumOverall = 0;

        for (Feedback f : all) {
            sumFood += f.getFood();
            sumAmb += f.getAmbiance();
            sumIng += f.getIngredients();
            sumServ += f.getService();
            sumClean += f.getCleanliness();
            sumValue += f.getValueForMoney();
            sumOverall += f.getOverall();
        }

        dto.setAvgFood(sumFood / n);
        dto.setAvgAmbiance(sumAmb / n);
        dto.setAvgIngredients(sumIng / n);
        dto.setAvgService(sumServ / n);
        dto.setAvgCleanliness(sumClean / n);
        dto.setAvgValueForMoney(sumValue / n);
        dto.setAvgOverall(sumOverall / n);

        dto.setFoodHistogram(toHistogram(feedbackRepository.countFoodRatings(restaurant)));
        dto.setAmbianceHistogram(toHistogram(feedbackRepository.countAmbianceRatings(restaurant)));
        dto.setIngredientsHistogram(toHistogram(feedbackRepository.countIngredientsRatings(restaurant)));
        dto.setServiceHistogram(toHistogram(feedbackRepository.countServiceRatings(restaurant)));
        dto.setCleanlinessHistogram(toHistogram(feedbackRepository.countCleanlinessRatings(restaurant)));
        dto.setValueHistogram(toHistogram(feedbackRepository.countValueRatings(restaurant)));
        dto.setOverallHistogram(toHistogram(feedbackRepository.countOverallRatings(restaurant)));

        double restaurantRating =
                (dto.getAvgFood()
                        + dto.getAvgAmbiance()
                        + dto.getAvgIngredients()
                        + dto.getAvgService()
                        + dto.getAvgCleanliness()
                        + dto.getAvgValueForMoney()
                        + dto.getAvgOverall()) / 7.0;

        dto.setRestaurantRating(restaurantRating);

        return dto;
    }

    private Map<Integer, Long> toHistogram(List<Object[]> raw) {
        Map<Integer, Long> map = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            map.put(i, 0L);
        }
        for (Object[] row : raw) {
            Integer rating = ((Number) row[0]).intValue();
            Long cnt = ((Number) row[1]).longValue();
            map.put(rating, cnt);
        }
        return map;
    }
}
