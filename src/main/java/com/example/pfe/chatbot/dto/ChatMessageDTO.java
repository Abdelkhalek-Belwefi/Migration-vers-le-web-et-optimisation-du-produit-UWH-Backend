package com.example.pfe.chatbot.dto;

public class ChatMessageDTO {
    private String message;
    private String role;
    private Long userId;
    private String sessionId;

    public ChatMessageDTO() {}

    public ChatMessageDTO(String message, String role, Long userId, String sessionId) {
        this.message = message;
        this.role = role;
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}