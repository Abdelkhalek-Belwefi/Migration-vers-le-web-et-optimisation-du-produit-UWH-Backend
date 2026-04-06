package com.example.pfe.expedition.dto;

import com.example.pfe.expedition.entity.ExpeditionStatut;
import java.time.LocalDateTime;

public class ExpeditionDTO {
    private Long id;
    private Long commandeId;
    private String commandeNumero;
    private String clientNom;
    private String numeroBL;
    private ExpeditionStatut statut;
    private LocalDateTime dateExpedition;
    private String prepareParNom;
    private String transporteur;
    private String numeroSuivi;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCommandeId() { return commandeId; }
    public void setCommandeId(Long commandeId) { this.commandeId = commandeId; }

    public String getCommandeNumero() { return commandeNumero; }
    public void setCommandeNumero(String commandeNumero) { this.commandeNumero = commandeNumero; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public String getNumeroBL() { return numeroBL; }
    public void setNumeroBL(String numeroBL) { this.numeroBL = numeroBL; }

    public ExpeditionStatut getStatut() { return statut; }
    public void setStatut(ExpeditionStatut statut) { this.statut = statut; }

    public LocalDateTime getDateExpedition() { return dateExpedition; }
    public void setDateExpedition(LocalDateTime dateExpedition) { this.dateExpedition = dateExpedition; }

    public String getPrepareParNom() { return prepareParNom; }
    public void setPrepareParNom(String prepareParNom) { this.prepareParNom = prepareParNom; }

    public String getTransporteur() { return transporteur; }
    public void setTransporteur(String transporteur) { this.transporteur = transporteur; }

    public String getNumeroSuivi() { return numeroSuivi; }
    public void setNumeroSuivi(String numeroSuivi) { this.numeroSuivi = numeroSuivi; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}