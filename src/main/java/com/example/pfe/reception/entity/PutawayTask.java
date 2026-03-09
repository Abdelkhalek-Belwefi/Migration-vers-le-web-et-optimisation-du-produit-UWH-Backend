package com.example.pfe.reception.entity;

import com.example.pfe.article.entity.Article;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "putaway_tasks")
public class PutawayTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "lot")
    private String lot;

    @Column(name = "quantite")
    private int quantite;

    @Column(name = "emplacement_source")
    private String emplacementSource;    // Zone de réception

    @Column(name = "emplacement_destination")
    private String emplacementDestination;

    @Column(name = "statut")
    private String statut;                // A_FAIRE, EN_COURS, TERMINEE

    @ManyToOne
    @JoinColumn(name = "reception_id")
    private Reception reception;          // Lien vers la réception d'origine

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- Getters et Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Article getArticle() { return article; }
    public void setArticle(Article article) { this.article = article; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public String getEmplacementSource() { return emplacementSource; }
    public void setEmplacementSource(String emplacementSource) { this.emplacementSource = emplacementSource; }

    public String getEmplacementDestination() { return emplacementDestination; }
    public void setEmplacementDestination(String emplacementDestination) { this.emplacementDestination = emplacementDestination; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Reception getReception() { return reception; }
    public void setReception(Reception reception) { this.reception = reception; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}