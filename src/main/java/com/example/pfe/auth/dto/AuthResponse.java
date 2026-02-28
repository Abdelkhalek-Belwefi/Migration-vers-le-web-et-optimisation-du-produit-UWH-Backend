package com.example.pfe.auth.dto;

public class AuthResponse {
    private String token;
    private String role;
    private String nom;
    private String prenom;
    private String email;
    private boolean estActif;

    public AuthResponse() {}

    public AuthResponse(String token, String role, String nom, String prenom, String email, boolean estActif) {
        this.token = token;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.estActif = estActif;
    }

    // Getters et Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }
}