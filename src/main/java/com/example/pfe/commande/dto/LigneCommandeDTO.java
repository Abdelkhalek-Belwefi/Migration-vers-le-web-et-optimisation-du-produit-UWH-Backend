package com.example.pfe.commande.dto;

public class LigneCommandeDTO {
    private Long id;
    private Long commandeId;
    private Long articleId;
    private String articleCode;
    private String articleDesignation;
    private Integer quantite;
    private Double prixUnitaire;
    private String statutPreparation;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCommandeId() { return commandeId; }
    public void setCommandeId(Long commandeId) { this.commandeId = commandeId; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getArticleCode() { return articleCode; }
    public void setArticleCode(String articleCode) { this.articleCode = articleCode; }

    public String getArticleDesignation() { return articleDesignation; }
    public void setArticleDesignation(String articleDesignation) { this.articleDesignation = articleDesignation; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public Double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(Double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public String getStatutPreparation() { return statutPreparation; }
    public void setStatutPreparation(String statutPreparation) { this.statutPreparation = statutPreparation; }
}