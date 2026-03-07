package com.example.pfe.stock.dto;

import com.example.pfe.stock.entity.StockStatut;
import java.time.LocalDateTime;

public class StockDTO {

    private Long id;
    private Long articleId;
    private String articleCode;
    private String articleDesignation;
    private String lot;
    private String emplacement;
    private int quantite;
    private StockStatut statut;
    private LocalDateTime dateReception;
    private LocalDateTime dateExpiration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StockDTO() {}

    // --- Getters et Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getArticleCode() { return articleCode; }
    public void setArticleCode(String articleCode) { this.articleCode = articleCode; }

    public String getArticleDesignation() { return articleDesignation; }
    public void setArticleDesignation(String articleDesignation) { this.articleDesignation = articleDesignation; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public String getEmplacement() { return emplacement; }
    public void setEmplacement(String emplacement) { this.emplacement = emplacement; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public StockStatut getStatut() { return statut; }
    public void setStatut(StockStatut statut) { this.statut = statut; }

    public LocalDateTime getDateReception() { return dateReception; }
    public void setDateReception(LocalDateTime dateReception) { this.dateReception = dateReception; }

    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}