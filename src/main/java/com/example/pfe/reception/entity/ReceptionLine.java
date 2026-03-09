package com.example.pfe.reception.entity;

import com.example.pfe.article.entity.Article;
import jakarta.persistence.*;
import java.time.LocalDateTime;  // ← IMPORT MANQUANT AJOUTÉ

@Entity
@Table(name = "reception_lines")
public class ReceptionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reception_id", nullable = false)
    private Reception reception;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "quantite_attendue")
    private int quantiteAttendue;      // Quantité commandée

    @Column(name = "quantite_recue")
    private int quantiteRecue;          // Quantité réellement reçue

    @Column(name = "lot")
    private String lot;                  // Numéro de lot (optionnel)

    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration; // ← Correction : import ajouté

    @Column(name = "emplacement_destination")
    private String emplacementDestination; // Où ranger la marchandise

    @Column(name = "statut")
    private String statut;                // EN_ATTENTE, RECU, PARTIEL

    // --- Getters et Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Reception getReception() { return reception; }
    public void setReception(Reception reception) { this.reception = reception; }

    public Article getArticle() { return article; }
    public void setArticle(Article article) { this.article = article; }

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