package com.example.pfe.picking.dto;

import com.example.pfe.picking.enums.StatutPicking;
import java.time.LocalDateTime;

public class PickingTaskDTO {
    private Long id;
    private Long commandeId;
    private String numeroCommande;
    private Long ligneCommandeId;
    private Long articleId;
    private String articleCode;
    private String articleDesignation;
    private Integer quantiteCommandee;
    private Integer quantitePicked;
    private StatutPicking statut;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCommandeId() { return commandeId; }
    public void setCommandeId(Long commandeId) { this.commandeId = commandeId; }

    public String getNumeroCommande() { return numeroCommande; }
    public void setNumeroCommande(String numeroCommande) { this.numeroCommande = numeroCommande; }

    public Long getLigneCommandeId() { return ligneCommandeId; }
    public void setLigneCommandeId(Long ligneCommandeId) { this.ligneCommandeId = ligneCommandeId; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getArticleCode() { return articleCode; }
    public void setArticleCode(String articleCode) { this.articleCode = articleCode; }

    public String getArticleDesignation() { return articleDesignation; }
    public void setArticleDesignation(String articleDesignation) { this.articleDesignation = articleDesignation; }

    public Integer getQuantiteCommandee() { return quantiteCommandee; }
    public void setQuantiteCommandee(Integer quantiteCommandee) { this.quantiteCommandee = quantiteCommandee; }

    public Integer getQuantitePicked() { return quantitePicked; }
    public void setQuantitePicked(Integer quantitePicked) { this.quantitePicked = quantitePicked; }

    public StatutPicking getStatut() { return statut; }
    public void setStatut(StatutPicking statut) { this.statut = statut; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}