package com.example.pfe.article.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_article_erp", unique = true, nullable = false)
    private String codeArticleERP;

    @Column(name = "gtin", length = 14)
    private String gtin;

    // 🔴 CORRECTION IMPORTANTE - Vérifiez cette annotation
    @Column(name = "num_serie", length = 50, unique = true)
    private String numSerie;  // Le nom de la variable doit correspondre au getter/setter

    @Column(nullable = false)
    private String designation;

    @Column(length = 500)
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "unite_mesure")
    private String uniteMesure;

    @Column(name = "poids")
    private double poids;

    @Column(name = "volume")
    private double volume;

    @Column(name = "lot_defaut", length = 20)
    private String lotDefaut;

    @Column(name = "duree_expiration_jours")
    private Integer dureeExpirationJours;

    @Column(name = "actif")
    private boolean actif = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructeurs
    public Article() {}

    public Article(String codeArticleERP, String gtin, String numSerie, String designation,
                   String category, String uniteMesure) {
        this.codeArticleERP = codeArticleERP;
        this.gtin = gtin;
        this.numSerie = numSerie;
        this.designation = designation;
        this.category = category;
        this.uniteMesure = uniteMesure;
    }

    // Getters et Setters - TOUS DOIVENT EXISTER
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodeArticleERP() { return codeArticleERP; }
    public void setCodeArticleERP(String codeArticleERP) { this.codeArticleERP = codeArticleERP; }

    public String getGtin() { return gtin; }
    public void setGtin(String gtin) { this.gtin = gtin; }

    // 🔴 Getter/Setter pour numSerie - Vérifiez ces méthodes
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
}