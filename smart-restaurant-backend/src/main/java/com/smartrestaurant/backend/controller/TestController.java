package com.smartrestaurant.backend.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> testProtected(Authentication authentication, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "You are authenticated!");
        response.put("username", authentication.getName());
        response.put("role", request.getAttribute("userRole"));
        response.put("restaurantCode", request.getAttribute("restaurantCode"));
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/owner-only")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Map<String, String>> testOwnerOnly(Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome Owner: " + authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chef-only")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<Map<String, String>> testChefOnly(Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome Chef: " + authentication.getName());
        return ResponseEntity.ok(response);
    }
}
