package com.example.pfe.livraison.entity;

import com.example.pfe.auth.entity.User;
import com.example.pfe.expedition.entity.Expedition;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "livraisons")
public class Livraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "expedition_id", nullable = false, unique = true)
    private Expedition expedition;

    @ManyToOne
    @JoinColumn(name = "transporteur_id", nullable = false)
    private User transporteur;

    @Column(name = "code_otp", nullable = false, unique = true)
    private String codeOtp;

    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private LivraisonStatut statut;

    @Column(name = "date_assignation")
    private LocalDateTime dateAssignation;

    @Column(name = "date_livraison")
    private LocalDateTime dateLivraison;

    @Column(name = "latitude_validation")
    private Double latitudeValidation;

    @Column(name = "longitude_validation")
    private Double longitudeValidation;

    @Column(name = "commentaire")
    private String commentaire;

    // constructeurs, getters, setters
    public Livraison() {
        this.statut = LivraisonStatut.ASSIGNEE;
        this.dateAssignation = LocalDateTime.now();
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Expedition getExpedition() { return expedition; }
    public void setExpedition(Expedition expedition) { this.expedition = expedition; }

    public User getTransporteur() { return transporteur; }
    public void setTransporteur(User transporteur) { this.transporteur = transporteur; }

    public String getCodeOtp() { return codeOtp; }
    public void setCodeOtp(String codeOtp) { this.codeOtp = codeOtp; }

    public LivraisonStatut getStatut() { return statut; }
    public void setStatut(LivraisonStatut statut) { this.statut = statut; }

    public LocalDateTime getDateAssignation() { return dateAssignation; }
    public void setDateAssignation(LocalDateTime dateAssignation) { this.dateAssignation = dateAssignation; }

    public LocalDateTime getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDateTime dateLivraison) { this.dateLivraison = dateLivraison; }

    public Double getLatitudeValidation() { return latitudeValidation; }
    public void setLatitudeValidation(Double latitudeValidation) { this.latitudeValidation = latitudeValidation; }

    public Double getLongitudeValidation() { return longitudeValidation; }
    public void setLongitudeValidation(Double longitudeValidation) { this.longitudeValidation = longitudeValidation; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}