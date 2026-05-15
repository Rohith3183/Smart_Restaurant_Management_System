package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.EmployeeDto;
import com.smartrestaurant.backend.entity.Activity;
import com.smartrestaurant.backend.service.ActivityService;
import com.smartrestaurant.backend.entity.Employee;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.repository.EmployeeRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final RestaurantRepository restaurantRepository;
    private final EmployeeRepository employeeRepository;
    private final ActivityService activityService;

    public EmployeeServiceImpl(RestaurantRepository restaurantRepository,
                              EmployeeRepository employeeRepository,
                              ActivityService activityService) {
        this.restaurantRepository = restaurantRepository;
        this.employeeRepository = employeeRepository;
        this.activityService = activityService;
    }

    private Restaurant getRestaurant(String code) {
        return restaurantRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
    }

    @Override
    public List<EmployeeDto> getAllEmployees(String restaurantCode) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        return employeeRepository.findByRestaurant(restaurant).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDto createEmployee(String restaurantCode, EmployeeDto dto) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        Employee employee = Employee.builder()
                .restaurant(restaurant)
                .name(dto.getName())
                .role(Employee.Role.valueOf(dto.getRole()))
                .salary(dto.getSalary())
                .active(dto.isActive())
                .build();
        Employee saved = employeeRepository.save(employee);
        activityService.log(
        	    restaurantCode,
        	    Activity.Type.EMPLOYEE_CREATED,
        	    String.format("Employee '%s' joined as %s (salary ₹%.0f)",
        	                  saved.getName(),
        	                  saved.getRole().name(),
        	                  saved.getSalary()),
        	    "OWNER"
        	);
        return toDto(saved);
    }

    @Override
    public EmployeeDto updateEmployee(String restaurantCode, Long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        
        if (!employee.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Employee does not belong to this restaurant");
        }

        employee.setName(dto.getName());
        employee.setRole(Employee.Role.valueOf(dto.getRole()));
        employee.setSalary(dto.getSalary());
        employee.setActive(dto.isActive());

        Employee updated = employeeRepository.save(employee);
        return toDto(updated);
    }

    @Override
    public void deleteEmployee(String restaurantCode, Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        
        if (!employee.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Employee does not belong to this restaurant");
        }
        
        activityService.log(
        	    restaurantCode,
        	    Activity.Type.EMPLOYEE_DELETED,
        	    String.format("Employee '%s' removed", employee.getName()),
        	    "OWNER"
        	);

        employeeRepository.delete(employee);
    }

    private EmployeeDto toDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setRole(employee.getRole().name());
        dto.setSalary(employee.getSalary());
        dto.setActive(employee.isActive());
        return dto;
    }
}
