package com.example.pfe.livraison.dto;

public class ValidationLivraisonRequest {
    private String codeOtp;
    private double latitude;
    private double longitude;
    private String commentaire;

    // getters et setters
    public String getCodeOtp() { return codeOtp; }
    public void setCodeOtp(String codeOtp) { this.codeOtp = codeOtp; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}