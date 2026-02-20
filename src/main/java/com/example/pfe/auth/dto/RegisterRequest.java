package com.example.pfe.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nom;
    private String prenom;

    public String getNumTelephone() {
        return numTelephone;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    private String email;
    private String numTelephone;
    private String password;

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNumTelephone(String numTelephone) {
        this.numTelephone = numTelephone;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
