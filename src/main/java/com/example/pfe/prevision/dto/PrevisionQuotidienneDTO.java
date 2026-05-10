package com.example.pfe.prevision.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrevisionQuotidienneDTO {

    private String date;
    private Double chargePrevue;
    private Double chargeMin;
    private Double chargeMax;
    private Boolean estPic;
    private Double ecartType;
    private String commentaire;

    // Constructeurs
    public PrevisionQuotidienneDTO() {
        this.chargePrevue = 0.0;
        this.chargeMin = 0.0;
        this.chargeMax = 0.0;
        this.estPic = false;
        this.ecartType = 0.0;
    }

    public PrevisionQuotidienneDTO(String date, Double chargePrevue, Double chargeMin, Double chargeMax) {
        this.date = date;
        this.chargePrevue = chargePrevue != null ? chargePrevue : 0.0;
        this.chargeMin = chargeMin != null ? chargeMin : 0.0;
        this.chargeMax = chargeMax != null ? chargeMax : 0.0;
        this.estPic = false;
        this.ecartType = 0.0;
    }

    // Getters et Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Double getChargePrevue() { return chargePrevue != null ? chargePrevue : 0.0; }
    public void setChargePrevue(Double chargePrevue) { this.chargePrevue = chargePrevue; }

    public Double getChargeMin() { return chargeMin != null ? chargeMin : 0.0; }
    public void setChargeMin(Double chargeMin) { this.chargeMin = chargeMin; }

    public Double getChargeMax() { return chargeMax != null ? chargeMax : 0.0; }
    public void setChargeMax(Double chargeMax) { this.chargeMax = chargeMax; }

    public Boolean getEstPic() { return estPic != null ? estPic : false; }
    public void setEstPic(Boolean estPic) { this.estPic = estPic; }

    public Double getEcartType() { return ecartType != null ? ecartType : 0.0; }
    public void setEcartType(Double ecartType) { this.ecartType = ecartType; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}