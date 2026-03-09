package com.example.pfe.gs1.dto;

public class GS1DataDTO {

    private String gtin;
    private String lot;
    private String dateExpiration;
    private Integer quantite;
    private Double poids;
    private String format;

    public GS1DataDTO() {}

    public String getGtin() { return gtin; }
    public void setGtin(String gtin) { this.gtin = gtin; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public String getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(String dateExpiration) { this.dateExpiration = dateExpiration; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public Double getPoids() { return poids; }
    public void setPoids(Double poids) { this.poids = poids; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}