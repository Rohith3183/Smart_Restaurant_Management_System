package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.IngredientDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockUpdateNotifier {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public StockUpdateNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Broadcast stock update to all connected clients for a restaurant
     */
    public void notifyStockUpdate(String restaurantCode, IngredientDto ingredient) {
        StockUpdateMessage message = new StockUpdateMessage(
            "STOCK_UPDATE",
            ingredient,
            LocalDateTime.now()
        );
        
        // Send to topic specific to restaurant
        messagingTemplate.convertAndSend(
            "/topic/stock-updates/" + restaurantCode,
            message
        );
    }
    
    /**
     * Broadcast low stock alert
     */
    public void notifyLowStockAlert(String restaurantCode, List<IngredientDto> lowStockItems) {
        LowStockAlert alert = new LowStockAlert(
            "LOW_STOCK_ALERT",
            lowStockItems,
            LocalDateTime.now()
        );
        
        messagingTemplate.convertAndSend(
            "/topic/alerts/" + restaurantCode,
            alert
        );
    }
    
    /**
     * Broadcast batch expiry warning
     */
    public void notifyExpiryWarning(String restaurantCode, String message) {
        ExpiryWarning warning = new ExpiryWarning(
            "EXPIRY_WARNING",
            message,
            LocalDateTime.now()
        );
        
        messagingTemplate.convertAndSend(
            "/topic/expiry/" + restaurantCode,
            warning
        );
    }
    
    // Message DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockUpdateMessage {
        private String type;
        private IngredientDto ingredient;
        private LocalDateTime timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockAlert {
        private String type;
        private List<IngredientDto> items;
        private LocalDateTime timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpiryWarning {
        private String type;
        private String message;
        private LocalDateTime timestamp;
    }
}
