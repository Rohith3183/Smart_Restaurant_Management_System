package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.AiInsightDto;
import com.smartrestaurant.backend.dto.ChatRequestDto;
import com.smartrestaurant.backend.dto.ChatResponseDto;

public interface FranchiseAiService {

    AiInsightDto generateInsights(String franchiseCode);

    ChatResponseDto chat(String franchiseCode, ChatRequestDto request);
}
