package com.example.pfe.chatbot.dto;

import java.util.List;
import java.util.Map;

public class ChatResponseDTO {
    private String message;
    private String type;
    private List<Map<String, Object>> cards;
    private Object data;

    public ChatResponseDTO() {}

    public ChatResponseDTO(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<Map<String, Object>> getCards() { return cards; }
    public void setCards(List<Map<String, Object>> cards) { this.cards = cards; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}