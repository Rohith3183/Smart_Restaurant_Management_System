package com.smartrestaurant.backend.dto;

import java.util.List;

public class ChatRequestDto {

    private List<ChatMessageDto> messages;

    public ChatRequestDto() {
    }

    public ChatRequestDto(List<ChatMessageDto> messages) {
        this.messages = messages;
    }

    public List<ChatMessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDto> messages) {
        this.messages = messages;
    }
}
