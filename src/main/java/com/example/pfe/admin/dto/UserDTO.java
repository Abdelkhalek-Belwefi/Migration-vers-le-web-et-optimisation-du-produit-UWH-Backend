package com.example.pfe.admin.dto;

import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String role;
    private boolean estActif;
    private LocalDateTime createdAt;
    private Long entrepotId;
    private String entrepotNom;

    public UserDTO() {}

    // Getters et Setters
    // Getters et setters
    public Long getEntrepotId() { return entrepotId; }
    public void setEntrepotId(Long entrepotId) { this.entrepotId = entrepotId; }

    public String getEntrepotNom() { return entrepotNom; }
    public void setEntrepotNom(String entrepotNom) { this.entrepotNom = entrepotNom; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 