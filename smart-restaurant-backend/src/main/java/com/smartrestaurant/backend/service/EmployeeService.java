package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.EmployeeDto;

import java.util.List;

public interface EmployeeService {
    List<EmployeeDto> getAllEmployees(String restaurantCode);
    EmployeeDto createEmployee(String restaurantCode, EmployeeDto dto);
    EmployeeDto updateEmployee(String restaurantCode, Long id, EmployeeDto dto);
    void deleteEmployee(String restaurantCode, Long id);
}
