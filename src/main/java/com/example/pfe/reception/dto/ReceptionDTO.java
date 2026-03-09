package com.example.pfe.reception.dto;

import com.example.pfe.reception.entity.ReceptionStatut;
import java.time.LocalDateTime;
import java.util.List;

public class ReceptionDTO {

    private Long id;
    private String numeroPO;
    private LocalDateTime dateReception;
    private ReceptionStatut statut;
    private String fournisseur;
    private String bonLivraison;
    private String createurNom;
    private String valideurNom;
    private LocalDateTime createdAt;
    private LocalDateTime validatedAt;
    private List<ReceptionLineDTO> lignes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroPO() { return numeroPO; }
    public void setNumeroPO(String numeroPO) { this.numeroPO = numeroPO; }

    public LocalDateTime getDateReception() { return dateReception; }
    public void setDateReception(LocalDateTime dateReception) { this.dateReception = dateReception; }

    public ReceptionStatut getStatut() { return statut; }
    public void setStatut(ReceptionStatut statut) { this.statut = statut; }

    public String getFournisseur() { return fournisseur; }
    public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }

    public String getBonLivraison() { return bonLivraison; }
    public void setBonLivraison(String bonLivraison) { this.bonLivraison = bonLivraison; }

    public String getCreateurNom() { return createurNom; }
    public void setCreateurNom(String createurNom) { this.createurNom = createurNom; }

    public String getValideurNom() { return valideurNom; }
    public void setValideurNom(String valideurNom) { this.valideurNom = valideurNom; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getValidatedAt() { return validatedAt; }
    public void setValidatedAt(LocalDateTime validatedAt) { this.validatedAt = validatedAt; }

    public List<ReceptionLineDTO> getLignes() { return lignes; }
    public void setLignes(List<ReceptionLineDTO> lignes) { this.lignes = lignes; }
}