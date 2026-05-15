package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.ActivityDto;
import com.smartrestaurant.backend.entity.Activity;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.repository.ActivityRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.service.ActivityService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ActivityServiceImpl implements ActivityService {

    private final RestaurantRepository restaurantRepository;
    private final ActivityRepository activityRepository;

    public ActivityServiceImpl(RestaurantRepository restaurantRepository,
                               ActivityRepository activityRepository) {
        this.restaurantRepository = restaurantRepository;
        this.activityRepository = activityRepository;
    }

    // ORIGINAL: log using restaurantCode (for restaurant users)
    @Override
    public void log(String restaurantCode, Activity.Type type, String message, String actorName) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        Activity activity = Activity.builder()
                .restaurant(restaurant)
                .type(type)
                .message(message)
                .actorName(actorName)
                .createdAt(LocalDateTime.now())
                .build();

        activityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityDto> getRecentActivities(String restaurantCode) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        return activityRepository.findTop50ByRestaurantOrderByCreatedAtDesc(restaurant)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // every hour
    public void purgeOldActivities() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(2);
        activityRepository.deleteByCreatedAtBefore(cutoff);
    }

    private ActivityDto toDto(Activity a) {
        ActivityDto dto = new ActivityDto();
        dto.setId(a.getId());
        dto.setType(a.getType().name());
        dto.setMessage(a.getMessage());
        dto.setActorName(a.getActorName());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
