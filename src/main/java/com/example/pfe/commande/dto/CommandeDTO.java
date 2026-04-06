package com.example.pfe.commande.dto;

import com.example.pfe.commande.enums.StatutCommande;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CommandeDTO {
    private Long id;
    private String numeroCommande;
    private Long clientId;
    private String clientNom;
    private LocalDateTime dateCommande;
    private LocalDate dateLivraisonSouhaitee;
    private StatutCommande statut;
    private String notes;
    private List<LigneCommandeDTO> lignes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroCommande() { return numeroCommande; }
    public void setNumeroCommande(String numeroCommande) { this.numeroCommande = numeroCommande; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }

    public LocalDate getDateLivraisonSouhaitee() { return dateLivraisonSouhaitee; }
    public void setDateLivraisonSouhaitee(LocalDate dateLivraisonSouhaitee) { this.dateLivraisonSouhaitee = dateLivraisonSouhaitee; }

    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande statut) { this.statut = statut; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<LigneCommandeDTO> getLignes() { return lignes; }
    public void setLignes(List<LigneCommandeDTO> lignes) { this.lignes = lignes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}