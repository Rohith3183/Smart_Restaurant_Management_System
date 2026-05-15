package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.TableDto;
import com.smartrestaurant.backend.service.TableService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
@CrossOrigin(origins = "*")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ACCOUNTANT', 'WAITER', 'INVENTORY')")
    public ResponseEntity<List<TableDto>> getAll(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(tableService.getAllTables(restaurantCode));
    }

    @PostMapping
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<TableDto> create(@RequestBody TableDto dto, 
                                            HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(tableService.createTable(restaurantCode, dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<TableDto> update(@PathVariable Long id,
                                            @RequestBody TableDto dto,
                                            HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(tableService.updateTable(restaurantCode, id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        tableService.deleteTable(restaurantCode, id);
        return ResponseEntity.noContent().build();
    }
}
