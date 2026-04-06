package com.example.pfe.reception.dto;

import java.time.LocalDateTime;

public class PutawayTaskDTO {

    private Long id;
    private Long articleId;
    private String articleDesignation;
    private String lot;
    private int quantite;
    private String emplacementSource;
    private String emplacementDestination;
    private String statut;
    private Long receptionId;
    private LocalDateTime createdAt;


    // --- Getters et Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getArticleDesignation() { return articleDesignation; }
    public void setArticleDesignation(String articleDesignation) { this.articleDesignation = articleDesignation; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public String getEmplacementSource() { return emplacementSource; }
    public void setEmplacementSource(String emplacementSource) { this.emplacementSource = emplacementSource; }

    public String getEmplacementDestination() { return emplacementDestination; }
    public void setEmplacementDestination(String emplacementDestination) { this.emplacementDestination = emplacementDestination; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Long getReceptionId() { return receptionId; }
    public void setReceptionId(Long receptionId) { this.receptionId = receptionId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}