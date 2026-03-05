package com.example.pfe.article.dto;

import java.time.LocalDateTime;

public class ArticleDTO {

    private Long id;
    private String codeArticleERP;
    private String gtin;  // Nouveau champ pour GS1
    private String designation;
    private String description;
    private String category;
    private String uniteMesure;
    private double poids;
    private double volume;
    private String lotDefaut;
    private Integer dureeExpirationJours;
    private boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructeurs
    public ArticleDTO() {}

    // Getters et Setters pour tous les champs
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodeArticleERP() { return codeArticleERP; }
    public void setCodeArticleERP(String codeArticleERP) { this.codeArticleERP = codeArticleERP; }

    public String getGtin() { return gtin; }
    public void setGtin(String gtin) { this.gtin = gtin; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUniteMesure() { return uniteMesure; }
    public void setUniteMesure(String uniteMesure) { this.uniteMesure = uniteMesure; }

    public double getPoids() { return poids; }
    public void setPoids(double poids) { this.poids = poids; }

    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }

    public String getLotDefaut() { return lotDefaut; }
    public void setLotDefaut(String lotDefaut) { this.lotDefaut = lotDefaut; }

    public Integer getDureeExpirationJours() { return dureeExpirationJours; }
    public void setDureeExpirationJours(Integer dureeExpirationJours) { this.dureeExpirationJours = dureeExpirationJours; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}