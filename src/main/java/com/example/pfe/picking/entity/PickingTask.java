package com.example.pfe.picking.entity;

import com.example.pfe.article.entity.Article;
import com.example.pfe.commande.entity.Commande;
import com.example.pfe.commande.entity.LigneCommande;
import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.picking.enums.StatutPicking;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "picking_tasks")
public class PickingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "ligne_commande_id", nullable = false)
    private LigneCommande ligneCommande;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    private Integer quantiteCommandee;
    private Integer quantitePicked = 0;

    @Enumerated(EnumType.STRING)
    private StatutPicking statut = StatutPicking.A_PREPARER;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== NOUVEAU : AJOUT DE L'ENTREPÔT ==========
    @ManyToOne
    @JoinColumn(name = "entrepot_id", nullable = false)
    private Warehouse entrepot;

    public PickingTask() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters et setters existants (inchangés)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }

    public LigneCommande getLigneCommande() { return ligneCommande; }
    public void setLigneCommande(LigneCommande ligneCommande) { this.ligneCommande = ligneCommande; }

    public Article getArticle() { return article; }
    public void setArticle(Article article) { this.article = article; }

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

    // ========== NOUVEAU GETTER/SETTER ==========
    public Warehouse getEntrepot() { return entrepot; }
    public void setEntrepot(Warehouse entrepot) { this.entrepot = entrepot; }
}