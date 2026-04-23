package com.example.pfe.livraison.dto;

import com.example.pfe.livraison.entity.LivraisonStatut;
import java.time.LocalDateTime;

public class LivraisonDTO {
    private Long id;
    private Long expeditionId;
    private String numeroBL;
    private String clientNom;
    private String adresseLivraison;
    private String transporteurNom;
    private String codeOtp;
    private LivraisonStatut statut;
    private LocalDateTime dateAssignation;
    private LocalDateTime dateLivraison;

    // getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getExpeditionId() { return expeditionId; }
    public void setExpeditionId(Long expeditionId) { this.expeditionId = expeditionId; }

    public String getNumeroBL() { return numeroBL; }
    public void setNumeroBL(String numeroBL) { this.numeroBL = numeroBL; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public String getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(String adresseLivraison) { this.adresseLivraison = adresseLivraison; }

    public String getTransporteurNom() { return transporteurNom; }
    public void setTransporteurNom(String transporteurNom) { this.transporteurNom = transporteurNom; }

    public String getCodeOtp() { return codeOtp; }
    public void setCodeOtp(String codeOtp) { this.codeOtp = codeOtp; }

    public LivraisonStatut getStatut() { return statut; }
    public void setStatut(LivraisonStatut statut) { this.statut = statut; }

    public LocalDateTime getDateAssignation() { return dateAssignation; }
    public void setDateAssignation(LocalDateTime dateAssignation) { this.dateAssignation = dateAssignation; }

    public LocalDateTime getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDateTime dateLivraison) { this.dateLivraison = dateLivraison; }
}