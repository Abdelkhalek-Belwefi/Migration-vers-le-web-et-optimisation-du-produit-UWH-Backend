package com.example.pfe.reception.dto;

import java.time.LocalDateTime;

public class ReceptionLineDTO {

    private Long id;
    private Long articleId;
    private String articleCode;
    private String articleDesignation;
    private int quantiteAttendue;
    private int quantiteRecue;
    private String lot;
    private LocalDateTime dateExpiration;  // ← Sans annotation
    private String emplacementDestination;
    private String statut;

    // Getters et Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getArticleCode() { return articleCode; }
    public void setArticleCode(String articleCode) { this.articleCode = articleCode; }

    public String getArticleDesignation() { return articleDesignation; }
    public void setArticleDesignation(String articleDesignation) { this.articleDesignation = articleDesignation; }

    public int getQuantiteAttendue() { return quantiteAttendue; }
    public void setQuantiteAttendue(int quantiteAttendue) { this.quantiteAttendue = quantiteAttendue; }

    public int getQuantiteRecue() { return quantiteRecue; }
    public void setQuantiteRecue(int quantiteRecue) { this.quantiteRecue = quantiteRecue; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }

    public String getEmplacementDestination() { return emplacementDestination; }
    public void setEmplacementDestination(String emplacementDestination) { this.emplacementDestination = emplacementDestination; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}