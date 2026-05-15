package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.SalaryDto;

import java.util.List;

public interface SalaryService {
    List<SalaryDto> getAllSalaries(String restaurantCode);
    void paySalary(String restaurantCode, Long employeeId);
}
