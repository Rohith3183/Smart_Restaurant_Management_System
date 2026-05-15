package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.TableDto;

import java.util.List;

public interface TableService {
    List<TableDto> getAllTables(String restaurantCode);
    TableDto createTable(String restaurantCode, TableDto dto);
    TableDto updateTable(String restaurantCode, Long id, TableDto dto);
    void deleteTable(String restaurantCode, Long id);
}
