package com.example.pfe.stock.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_stock")
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stock_source_id", nullable = false)
    private Stock stockSource;

    @ManyToOne
    @JoinColumn(name = "stock_destination_id", nullable = true)
    private Stock stockDestination;

    @Column(nullable = false)
    private String type; // ENTREE, SORTIE, TRANSFERT

    @Column(nullable = false)
    private int quantite;

    @Column(name = "ancienne_quantite_source")
    private Integer ancienneQuantiteSource;

    @Column(name = "nouvelle_quantite_source")
    private Integer nouvelleQuantiteSource;

    @Column(name = "ancienne_quantite_destination")
    private Integer ancienneQuantiteDestination;

    @Column(name = "nouvelle_quantite_destination")
    private Integer nouvelleQuantiteDestination;

    @Column(nullable = false)
    private String motif;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = true)
    private com.example.pfe.auth.entity.User utilisateur;

    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement;

    private String commentaire;

    @PrePersist
    protected void onCreate() {
        dateMouvement = LocalDateTime.now();
    }

    // --- Getters et Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Stock getStockSource() { return stockSource; }
    public void setStockSource(Stock stockSource) { this.stockSource = stockSource; }

    public Stock getStockDestination() { return stockDestination; }
    public void setStockDestination(Stock stockDestination) { this.stockDestination = stockDestination; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public Integer getAncienneQuantiteSource() { return ancienneQuantiteSource; }
    public void setAncienneQuantiteSource(Integer ancienneQuantiteSource) { this.ancienneQuantiteSource = ancienneQuantiteSource; }

    public Integer getNouvelleQuantiteSource() { return nouvelleQuantiteSource; }
    public void setNouvelleQuantiteSource(Integer nouvelleQuantiteSource) { this.nouvelleQuantiteSource = nouvelleQuantiteSource; }

    public Integer getAncienneQuantiteDestination() { return ancienneQuantiteDestination; }
    public void setAncienneQuantiteDestination(Integer ancienneQuantiteDestination) { this.ancienneQuantiteDestination = ancienneQuantiteDestination; }

    public Integer getNouvelleQuantiteDestination() { return nouvelleQuantiteDestination; }
    public void setNouvelleQuantiteDestination(Integer nouvelleQuantiteDestination) { this.nouvelleQuantiteDestination = nouvelleQuantiteDestination; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public com.example.pfe.auth.entity.User getUtilisateur() { return utilisateur; }
    public void setUtilisateur(com.example.pfe.auth.entity.User utilisateur) { this.utilisateur = utilisateur; }

    public LocalDateTime getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement = dateMouvement; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}