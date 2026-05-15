package com.smartrestaurant.backend.service;

public interface ExpirySchedulerService {
    void scanAndHandleExpiringBatches();
}
