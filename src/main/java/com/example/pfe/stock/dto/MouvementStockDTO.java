package com.example.pfe.stock.dto;

import java.time.LocalDateTime;

public class MouvementStockDTO {

    private Long id;
    private Long stockSourceId;
    private String articleDesignation;
    private String articleCode;
    private String lotSource;
    private String emplacementSource;

    private Long stockDestinationId;
    private String emplacementDestination;
    private String lotDestination;

    private String type;
    private int quantite;

    private Integer ancienneQuantiteSource;
    private Integer nouvelleQuantiteSource;
    private Integer ancienneQuantiteDestination;
    private Integer nouvelleQuantiteDestination;

    private String motif;
    private String utilisateurNom;
    private LocalDateTime dateMouvement;
    private String commentaire;

    // --- Getters et Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStockSourceId() { return stockSourceId; }
    public void setStockSourceId(Long stockSourceId) { this.stockSourceId = stockSourceId; }

    public String getArticleDesignation() { return articleDesignation; }
    public void setArticleDesignation(String articleDesignation) { this.articleDesignation = articleDesignation; }

    public String getArticleCode() { return articleCode; }
    public void setArticleCode(String articleCode) { this.articleCode = articleCode; }

    public String getLotSource() { return lotSource; }
    public void setLotSource(String lotSource) { this.lotSource = lotSource; }

    public String getEmplacementSource() { return emplacementSource; }
    public void setEmplacementSource(String emplacementSource) { this.emplacementSource = emplacementSource; }

    public Long getStockDestinationId() { return stockDestinationId; }
    public void setStockDestinationId(Long stockDestinationId) { this.stockDestinationId = stockDestinationId; }

    public String getEmplacementDestination() { return emplacementDestination; }
    public void setEmplacementDestination(String emplacementDestination) { this.emplacementDestination = emplacementDestination; }

    public String getLotDestination() { return lotDestination; }
    public void setLotDestination(String lotDestination) { this.lotDestination = lotDestination; }

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

    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }

    public LocalDateTime getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement = dateMouvement; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}