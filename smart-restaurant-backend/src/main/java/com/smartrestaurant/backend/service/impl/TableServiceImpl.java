package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.TableDto;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.entity.RestaurantTable;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.repository.RestaurantTableRepository;
import com.smartrestaurant.backend.service.TableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TableServiceImpl implements TableService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository tableRepository;

    public TableServiceImpl(RestaurantRepository restaurantRepository,
                           RestaurantTableRepository tableRepository) {
        this.restaurantRepository = restaurantRepository;
        this.tableRepository = tableRepository;
    }

    private Restaurant getRestaurant(String code) {
        return restaurantRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
    }

    @Override
    public List<TableDto> getAllTables(String restaurantCode) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        return tableRepository.findByRestaurant(restaurant).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TableDto createTable(String restaurantCode, TableDto dto) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        RestaurantTable table = RestaurantTable.builder()
                .restaurant(restaurant)
                .tableNumber(dto.getTableNumber())
                .capacity(dto.getCapacity())
                .status(RestaurantTable.Status.FREE)
                .build();
        RestaurantTable saved = tableRepository.save(table);
        return toDto(saved);
    }

    @Override
    public TableDto updateTable(String restaurantCode, Long id, TableDto dto) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        
        if (!table.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Table does not belong to this restaurant");
        }

        table.setTableNumber(dto.getTableNumber());
        table.setCapacity(dto.getCapacity());
        if (dto.getStatus() != null) {
            table.setStatus(RestaurantTable.Status.valueOf(dto.getStatus()));
        }

        RestaurantTable updated = tableRepository.save(table);
        return toDto(updated);
    }

    @Override
    public void deleteTable(String restaurantCode, Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        
        if (!table.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Table does not belong to this restaurant");
        }

        tableRepository.delete(table);
    }

    private TableDto toDto(RestaurantTable table) {
        TableDto dto = new TableDto();
        dto.setId(table.getId());
        dto.setTableNumber(table.getTableNumber());
        dto.setCapacity(table.getCapacity());
        dto.setStatus(table.getStatus().name());
        return dto;
    }
}
