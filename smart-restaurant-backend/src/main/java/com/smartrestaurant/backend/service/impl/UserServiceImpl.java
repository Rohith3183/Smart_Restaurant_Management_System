package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.CreateUserRequest;
import com.smartrestaurant.backend.dto.UpdateUserRequest;
import com.smartrestaurant.backend.dto.UserDto;
import com.smartrestaurant.backend.entity.Employee;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.entity.User;
import com.smartrestaurant.backend.repository.EmployeeRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.repository.UserRepository;
import com.smartrestaurant.backend.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(RestaurantRepository restaurantRepository,
                          UserRepository userRepository,
                          EmployeeRepository employeeRepository,
                          BCryptPasswordEncoder passwordEncoder) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto createUser(String restaurantCode, CreateUserRequest request) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.valueOf(request.getRole()))
                .restaurant(restaurant)
                .active(true)
                .build();

        User saved = userRepository.save(user);

        // Auto-create employee record
        Employee employee = Employee.builder()
                .restaurant(restaurant)
                .name(request.getUsername())
                .role(Employee.Role.valueOf(request.getRole()))
                .salary(25000.0)
                .active(true)
                .build();
        
        employeeRepository.save(employee);

        return toDto(saved);
    }

    @Override
    public UserDto updateUser(String restaurantCode, Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("User does not belong to this restaurant");
        }

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            Optional<User> existing = userRepository.findByUsername(request.getUsername());
            if (existing.isPresent() && !existing.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User updated = userRepository.save(user);
        return toDto(updated);
    }

    @Override
    public List<UserDto> getAllUsers(String restaurantCode) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        return userRepository.findAll().stream()
                .filter(u -> u.getRestaurant().getId().equals(restaurant.getId()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(String restaurantCode, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("User does not belong to this restaurant");
        }

        List<Employee> employees = employeeRepository.findByRestaurant(user.getRestaurant()).stream()
                .filter(e -> e.getName().equals(user.getUsername()))
                .collect(Collectors.toList());
        
        employees.forEach(employeeRepository::delete);

        userRepository.delete(user);
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());
        dto.setActive(user.isActive());
        return dto;
    }
}
