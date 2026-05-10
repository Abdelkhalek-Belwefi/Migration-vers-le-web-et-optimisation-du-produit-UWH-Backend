package com.example.pfe.prevision.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrevisionChargeDTO {

    private Long id;
    private Long entrepotId;
    private String entrepotNom;
    private String dateCalcul;
    private String dateDebutPrevision;
    private String dateFinPrevision;
    private List<PrevisionQuotidienneDTO> previsions;
    private Double chargeMoyennePrevue;
    private Double chargeMaxPrevue;
    private String datePicMax;
    private Boolean alertePicProche;
    private String messageAlerte;

    // Constructeurs
    public PrevisionChargeDTO() {
        this.previsions = new ArrayList<>();
        this.dateCalcul = LocalDateTime.now().toString();
        this.alertePicProche = false;
    }

    public PrevisionChargeDTO(Long entrepotId, String entrepotNom, List<PrevisionQuotidienneDTO> previsions) {
        this.entrepotId = entrepotId;
        this.entrepotNom = entrepotNom;
        this.previsions = previsions != null ? previsions : new ArrayList<>();
        this.dateCalcul = LocalDateTime.now().toString();
        this.alertePicProche = false;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEntrepotId() { return entrepotId; }
    public void setEntrepotId(Long entrepotId) { this.entrepotId = entrepotId; }

    public String getEntrepotNom() { return entrepotNom; }
    public void setEntrepotNom(String entrepotNom) { this.entrepotNom = entrepotNom; }

    public String getDateCalcul() { return dateCalcul; }
    public void setDateCalcul(String dateCalcul) { this.dateCalcul = dateCalcul; }

    public String getDateDebutPrevision() { return dateDebutPrevision; }
    public void setDateDebutPrevision(String dateDebutPrevision) { this.dateDebutPrevision = dateDebutPrevision; }

    public String getDateFinPrevision() { return dateFinPrevision; }
    public void setDateFinPrevision(String dateFinPrevision) { this.dateFinPrevision = dateFinPrevision; }

    public List<PrevisionQuotidienneDTO> getPrevisions() {
        return previsions != null ? previsions : new ArrayList<>();
    }
    public void setPrevisions(List<PrevisionQuotidienneDTO> previsions) {
        this.previsions = previsions != null ? previsions : new ArrayList<>();
    }

    public Double getChargeMoyennePrevue() { return chargeMoyennePrevue != null ? chargeMoyennePrevue : 0.0; }
    public void setChargeMoyennePrevue(Double chargeMoyennePrevue) { this.chargeMoyennePrevue = chargeMoyennePrevue; }

    public Double getChargeMaxPrevue() { return chargeMaxPrevue != null ? chargeMaxPrevue : 0.0; }
    public void setChargeMaxPrevue(Double chargeMaxPrevue) { this.chargeMaxPrevue = chargeMaxPrevue; }

    public String getDatePicMax() { return datePicMax; }
    public void setDatePicMax(String datePicMax) { this.datePicMax = datePicMax; }

    public Boolean getAlertePicProche() { return alertePicProche != null ? alertePicProche : false; }
    public void setAlertePicProche(Boolean alertePicProche) { this.alertePicProche = alertePicProche; }

    public String getMessageAlerte() { return messageAlerte; }
    public void setMessageAlerte(String messageAlerte) { this.messageAlerte = messageAlerte; }
}