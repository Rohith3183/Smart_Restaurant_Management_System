package com.smartrestaurant.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiModelClient {

    private final ChatClient chatClient;
    private final boolean aiEnabled;

    public AiModelClient(
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            @Value("${ai.ollama.enabled:false}") boolean aiEnabled
    ) {
        this.aiEnabled = aiEnabled;
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        this.chatClient = (aiEnabled && builder != null) ? builder.build() : null;
    }

    /**
     * Chat completion for multi-turn conversations (owner chat bot)
     */
    public String chat(List<Map<String, String>> messages) {
        if (!aiEnabled || chatClient == null) {
            return "AI feature is temporarily disabled.";
        }

        List<Message> aiMessages = new ArrayList<>();

        for (Map<String, String> msg : messages) {
            String role = msg.get("role");
            String content = msg.get("content");

            if (role == null || content == null) {
                continue;
            }

            switch (role) {
                case "system" -> aiMessages.add(new SystemMessage(content));
                case "user" -> aiMessages.add(new UserMessage(content));
                case "assistant" -> aiMessages.add(new AssistantMessage(content));
            }
        }

        return chatClient.prompt()
                .messages(aiMessages)
                .call()
                .content();
    }

    /**
     * Single insights generation
     */
    public String generateInsights(String userPrompt) {
        if (!aiEnabled || chatClient == null) {
            return "AI feature is temporarily disabled.";
        }

        return chatClient.prompt()
                .system("You are a restaurant business analyst. Generate markdown insights + recommendations.")
                .user(userPrompt)
                .call()
                .content();
    }
}
