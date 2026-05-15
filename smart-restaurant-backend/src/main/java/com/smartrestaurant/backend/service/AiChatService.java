package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.AnalyticsDto;
import com.smartrestaurant.backend.dto.ChatMessageDto;
import com.smartrestaurant.backend.dto.ChatRequestDto;
import com.smartrestaurant.backend.dto.ChatResponseDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiChatService {

    private final AiModelClient aiModelClient;
    private final AnalyticsService analyticsService;

    public AiChatService(AiModelClient aiModelClient,
                         AnalyticsService analyticsService) {
        this.aiModelClient = aiModelClient;
        this.analyticsService = analyticsService;
    }

    public ChatResponseDto chat(String restaurantCode, ChatRequestDto req) {
        AnalyticsDto a = analyticsService.getAnalytics(restaurantCode);

        List<Map<String, String>> messages = new ArrayList<>();

        // Strong system message with context & rules
        messages.add(Map.of(
                "role", "system",
                "content",
                """
                You are an AI assistant for ONE specific restaurant owner.
                You have the following analytics data for THIS restaurant only:

                totalOrders=%d, totalMenuItems=%d, totalEmployees=%d, totalTables=%d,
                weekRevenue=%s, monthRevenue=%s, yearRevenue=%s,
                weekProfit=%s, monthProfit=%s, yearProfit=%s,
                avgOrderValue=%s, tableTurnover=%s,
                customerSatisfaction=%s, avgWaitTimeMinutes=%s,
                topSellingItems=%s, ordersByCategory=%s, peakHours=%s.

                RULES:
                - Always base your answers on this data when possible.
                - Do NOT invent specific numbers that are not in the data.
                - If you don't have enough data, say that clearly.
                - All monetary values are in Indian Rupees. Use the ₹ symbol, NOT $, when you mention money.
                - Answer in 3–6 short sentences, practical and clear, addressing the OWNER.
                """
                        .formatted(
                                a.getTotalOrders(),
                                a.getTotalMenuItems(),
                                a.getTotalEmployees(),
                                a.getTotalTables(),
                                a.getWeekRevenue(),
                                a.getMonthRevenue(),
                                a.getYearRevenue(),
                                a.getWeekProfit(),
                                a.getMonthProfit(),
                                a.getYearProfit(),
                                a.getAvgOrderValue(),
                                a.getTableTurnover(),
                                a.getCustomerSatisfaction(),
                                a.getAvgWaitTimeMinutes(),
                                a.getTopSellingItems(),
                                a.getOrdersByCategory(),
                                a.getPeakHours()
                        )
        ));

        if (req.getMessages() != null) {
            for (ChatMessageDto m : req.getMessages()) {
                messages.add(Map.of(
                        "role", m.getRole(),
                        "content", m.getContent()
                ));
            }
        }

        String reply = aiModelClient.chat(messages);
        return new ChatResponseDto(reply);
    }
}
