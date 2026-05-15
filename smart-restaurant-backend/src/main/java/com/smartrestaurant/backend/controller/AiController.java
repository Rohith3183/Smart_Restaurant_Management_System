package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.AiInsightDto;
import com.smartrestaurant.backend.dto.ChatRequestDto;
import com.smartrestaurant.backend.dto.ChatResponseDto;
import com.smartrestaurant.backend.service.AiChatService;
import com.smartrestaurant.backend.service.AiSalesInsightService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin
public class AiController {

    private final AiSalesInsightService salesInsightService;
    private final AiChatService aiChatService;

    public AiController(AiSalesInsightService salesInsightService,
                        AiChatService aiChatService) {
        this.salesInsightService = salesInsightService;
        this.aiChatService = aiChatService;
    }

    @GetMapping("/sales-insights")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<AiInsightDto> getSalesInsights(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(salesInsightService.generateInsights(restaurantCode));
    }

    @PostMapping("/chat")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ChatResponseDto> chat(
            HttpServletRequest request,
            @RequestBody ChatRequestDto body
    ) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(aiChatService.chat(restaurantCode, body));
    }
}
