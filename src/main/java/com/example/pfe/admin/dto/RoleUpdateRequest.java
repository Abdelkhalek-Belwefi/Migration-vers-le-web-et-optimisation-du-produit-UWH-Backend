package com.example.pfe.admin.dto;

public class RoleUpdateRequest {
    private String role;
    private Long entrepotId;  // ← NOUVEAU
    public RoleUpdateRequest() {}

    public RoleUpdateRequest(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Getters et setters
    public Long getEntrepotId() { return entrepotId; }
    public void setEntrepotId(Long entrepotId) { this.entrepotId = entrepotId; }
}