package com.example.pfe.article.dto;

import com.example.pfe.article.entity.Article;
import java.time.LocalDateTime;

public class ArticleDTO {
    private Long id;
    private String codeArticleERP;
    private String gtin;
    private String numSerie;  // 🔴 CHAMP AJOUTÉ
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

    // Constructeur par défaut
    public ArticleDTO() {}

    // Constructeur avec tous les champs
    public ArticleDTO(Long id, String codeArticleERP, String gtin, String numSerie,
                      String designation, String description, String category,
                      String uniteMesure, double poids, double volume, String lotDefaut,
                      Integer dureeExpirationJours, boolean actif) {
        this.id = id;
        this.codeArticleERP = codeArticleERP;
        this.gtin = gtin;
        this.numSerie = numSerie;  // 🔴
        this.designation = designation;
        this.description = description;
        this.category = category;
        this.uniteMesure = uniteMesure;
        this.poids = poids;
        this.volume = volume;
        this.lotDefaut = lotDefaut;
        this.dureeExpirationJours = dureeExpirationJours;
        this.actif = actif;
    }

    // Constructeur depuis l'entité (TRÈS IMPORTANT)
    public ArticleDTO(Article article) {
        this.id = article.getId();
        this.codeArticleERP = article.getCodeArticleERP();
        this.gtin = article.getGtin();
        this.numSerie = article.getNumSerie();  // 🔴 Vérifiez cette ligne
        this.designation = article.getDesignation();
        this.description = article.getDescription();
        this.category = article.getCategory();
        this.uniteMesure = article.getUniteMesure();
        this.poids = article.getPoids();
        this.volume = article.getVolume();
        this.lotDefaut = article.getLotDefaut();
        this.dureeExpirationJours = article.getDureeExpirationJours();
        this.actif = article.isActif();
        this.createdAt = article.getCreatedAt();
        this.updatedAt = article.getUpdatedAt();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodeArticleERP() { return codeArticleERP; }
    public void setCodeArticleERP(String codeArticleERP) { this.codeArticleERP = codeArticleERP; }

    public String getGtin() { return gtin; }
    public void setGtin(String gtin) { this.gtin = gtin; }

    // 🔴 Getter/Setter pour numSerie
    public String getNumSerie() { return numSerie; }
    public void setNumSerie(String numSerie) { this.numSerie = numSerie; }

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

    @Override
    public String toString() {
        return "ArticleDTO{" +
                "id=" + id +
                ", codeArticleERP='" + codeArticleERP + '\'' +
                ", gtin='" + gtin + '\'' +
                ", numSerie='" + numSerie + '\'' +  // 🔴 Pour voir dans les logs
                ", designation='" + designation + '\'' +
                ", category='" + category + '\'' +
                ", actif=" + actif +
                '}';
    }
}