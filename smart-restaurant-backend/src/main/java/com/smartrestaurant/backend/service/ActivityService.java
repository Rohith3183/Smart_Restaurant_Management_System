package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.ActivityDto;
import com.smartrestaurant.backend.entity.Activity;

import java.util.List;

public interface ActivityService {

    void log(String restaurantCode, Activity.Type type, String message, String actorName);

    List<ActivityDto> getRecentActivities(String restaurantCode);

    void purgeOldActivities(); // delete > 2 days
}
