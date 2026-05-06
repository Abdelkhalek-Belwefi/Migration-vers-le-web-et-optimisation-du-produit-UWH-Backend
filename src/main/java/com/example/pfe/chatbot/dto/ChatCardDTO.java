package com.example.pfe.chatbot.dto;

import java.util.Map;

public class ChatCardDTO {
    private String title;
    private String subtitle;
    private String imageUrl;
    private Map<String, String> fields;
    private String buttonLabel;
    private String buttonAction;

    public ChatCardDTO() {}

    // Getters et setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Map<String, String> getFields() { return fields; }
    public void setFields(Map<String, String> fields) { this.fields = fields; }

    public String getButtonLabel() { return buttonLabel; }
    public void setButtonLabel(String buttonLabel) { this.buttonLabel = buttonLabel; }

    public String getButtonAction() { return buttonAction; }
    public void setButtonAction(String buttonAction) { this.buttonAction = buttonAction; }
}