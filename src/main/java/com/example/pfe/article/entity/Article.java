package com.example.pfe.article.entity;

import com.example.pfe.entrepot.entity.Warehouse;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "code_article_erp")
    private String codeArticleERP;

    private String gtin;
    private String numSerie;
    private String designation;
    private String description;
    private String category;
    private String uniteMesure;
    private double poids;
    private double volume;
    private String lotDefaut;
    private Integer dureeExpirationJours;
    private boolean actif = true;
    private Double prixUnitaire;   // champ ajouté

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== NOUVEAU : AJOUT DE L'ENTREPÔT (CENTRAL) ==========
    @ManyToOne
    @JoinColumn(name = "entrepot_id", nullable = false)
    private Warehouse entrepot;

    public Article() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters et setters existants (inchangés)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCodeArticleERP() { return codeArticleERP; }
    public void setCodeArticleERP(String codeArticleERP) { this.codeArticleERP = codeArticleERP; }

    public String getGtin() { return gtin; }
    public void setGtin(String gtin) { this.gtin = gtin; }

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

    public Double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(Double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ========== NOUVEAU GETTER/SETTER ==========
    public Warehouse getEntrepot() { return entrepot; }
    public void setEntrepot(Warehouse entrepot) { this.entrepot = entrepot; }

    @PrePersist
    public void prePersist() {
        if (this.code == null && this.codeArticleERP != null) {
            this.code = this.codeArticleERP;
        } else if (this.code == null) {
            this.code = "ART-" + System.currentTimeMillis();
        }
    }
}