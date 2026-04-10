package com.example.pfe.expedition.entity;

import com.example.pfe.auth.entity.User;
import com.example.pfe.commande.entity.Commande;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "expeditions")
public class Expedition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "commande_id", nullable = false, unique = true)
    private Commande commande;

    @Column(name = "numero_bl", unique = true)
    private String numeroBL;

    @Enumerated(EnumType.STRING)
    private ExpeditionStatut statut;

    @Column(name = "date_expedition")
    private LocalDateTime dateExpedition;

    @ManyToOne
    @JoinColumn(name = "prepare_par_id")
    private User preparePar;

    @ManyToOne
    @JoinColumn(name = "valide_par_id")
    private User validePar;

    private String transporteur;

    @Column(name = "numero_suivi")
    private String numeroSuivi;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== NOUVEAUX CHAMPS POUR STOCKER LE PDF ==========
    @Lob
    @Column(name = "pdf_document", columnDefinition = "LONGBLOB")
    private byte[] pdfDocument;

    @Column(name = "pdf_generated_at")
    private LocalDateTime pdfGeneratedAt;

    public Expedition() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.statut = ExpeditionStatut.EN_PREPARATION;
    }

    // Getters et setters existants
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }

    public String getNumeroBL() { return numeroBL; }
    public void setNumeroBL(String numeroBL) { this.numeroBL = numeroBL; }

    public ExpeditionStatut getStatut() { return statut; }
    public void setStatut(ExpeditionStatut statut) { this.statut = statut; }

    public LocalDateTime getDateExpedition() { return dateExpedition; }
    public void setDateExpedition(LocalDateTime dateExpedition) { this.dateExpedition = dateExpedition; }

    public User getPreparePar() { return preparePar; }
    public void setPreparePar(User preparePar) { this.preparePar = preparePar; }

    public User getValidePar() { return validePar; }
    public void setValidePar(User validePar) { this.validePar = validePar; }

    public String getTransporteur() { return transporteur; }
    public void setTransporteur(String transporteur) { this.transporteur = transporteur; }

    public String getNumeroSuivi() { return numeroSuivi; }
    public void setNumeroSuivi(String numeroSuivi) { this.numeroSuivi = numeroSuivi; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ========== NOUVEAUX GETTERS ET SETTERS ==========
    public byte[] getPdfDocument() { return pdfDocument; }
    public void setPdfDocument(byte[] pdfDocument) { this.pdfDocument = pdfDocument; }

    public LocalDateTime getPdfGeneratedAt() { return pdfGeneratedAt; }
    public void setPdfGeneratedAt(LocalDateTime pdfGeneratedAt) { this.pdfGeneratedAt = pdfGeneratedAt; }
}