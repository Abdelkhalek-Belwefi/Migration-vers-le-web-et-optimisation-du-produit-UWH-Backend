package com.example.pfe.commande.entity;

import com.example.pfe.article.entity.Article;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lignes_commande")
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "article_code", nullable = false)
    private String articleCode;

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "prix_unitaire")
    private Double prixUnitaire;

    @Column(name = "statut_preparation")
    private String statutPreparation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public LigneCommande() {
        this.createdAt = LocalDateTime.now();
        this.statutPreparation = "A_PREPARER";
    }

    public void setArticle(Article article) {
        this.article = article;
        if (article != null) {
            this.articleCode = article.getCode();
        }
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }

    public Article getArticle() { return article; }
    // Ne pas exposer setArticle seul, utiliser la méthode ci-dessus

    public String getArticleCode() { return articleCode; }
    public void setArticleCode(String articleCode) { this.articleCode = articleCode; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public Double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(Double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public String getStatutPreparation() { return statutPreparation; }
    public void setStatutPreparation(String statutPreparation) { this.statutPreparation = statutPreparation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}