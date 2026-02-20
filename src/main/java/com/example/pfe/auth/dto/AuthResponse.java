package com.example.pfe.auth.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String role;

    public AuthResponse(String token, String name) {
    }
}
