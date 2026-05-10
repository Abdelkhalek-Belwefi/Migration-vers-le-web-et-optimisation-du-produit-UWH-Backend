package com.example.pfe.prevision.entity;

import com.example.pfe.entrepot.entity.Warehouse;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "metriques_quotidiennes")
public class MetriqueQuotidienne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "entrepot_id", nullable = false)
    private Warehouse entrepot;

    @Column(name = "date_metrique", nullable = false)
    private LocalDate dateMetrique;

    // Métriques de charge
    @Column(name = "nb_commandes")
    private Integer nbCommandes = 0;

    @Column(name = "nb_receptions")
    private Integer nbReceptions = 0;

    @Column(name = "nb_expeditions")
    private Integer nbExpeditions = 0;

    @Column(name = "nb_connexions")
    private Integer nbConnexions = 0;

    @Column(name = "nb_requetes_api")
    private Integer nbRequetesApi = 0;

    @Column(name = "temps_reponse_moyen_ms")
    private Double tempsReponseMoyenMs = 0.0;

    @Column(name = "charge_travail")
    private Double chargeTravail = 0.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructeurs
    public MetriqueQuotidienne() {}

    public MetriqueQuotidienne(Warehouse entrepot, LocalDate dateMetrique) {
        this.entrepot = entrepot;
        this.dateMetrique = dateMetrique;
    }

    // Méthode utilitaire pour calculer la charge de travail
    public void calculerChargeTravail() {
        // Formule simple : commandes * 3 + receptions * 2 + expeditions * 4 + connexions * 1
        this.chargeTravail = (nbCommandes * 3.0) + (nbReceptions * 2.0) + (nbExpeditions * 4.0) + (nbConnexions * 1.0);
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Warehouse getEntrepot() { return entrepot; }
    public void setEntrepot(Warehouse entrepot) { this.entrepot = entrepot; }

    public LocalDate getDateMetrique() { return dateMetrique; }
    public void setDateMetrique(LocalDate dateMetrique) { this.dateMetrique = dateMetrique; }

    public Integer getNbCommandes() { return nbCommandes; }
    public void setNbCommandes(Integer nbCommandes) { this.nbCommandes = nbCommandes; }

    public Integer getNbReceptions() { return nbReceptions; }
    public void setNbReceptions(Integer nbReceptions) { this.nbReceptions = nbReceptions; }

    public Integer getNbExpeditions() { return nbExpeditions; }
    public void setNbExpeditions(Integer nbExpeditions) { this.nbExpeditions = nbExpeditions; }

    public Integer getNbConnexions() { return nbConnexions; }
    public void setNbConnexions(Integer nbConnexions) { this.nbConnexions = nbConnexions; }

    public Integer getNbRequetesApi() { return nbRequetesApi; }
    public void setNbRequetesApi(Integer nbRequetesApi) { this.nbRequetesApi = nbRequetesApi; }

    public Double getTempsReponseMoyenMs() { return tempsReponseMoyenMs; }
    public void setTempsReponseMoyenMs(Double tempsReponseMoyenMs) { this.tempsReponseMoyenMs = tempsReponseMoyenMs; }

    public Double getChargeTravail() { return chargeTravail; }
    public void setChargeTravail(Double chargeTravail) { this.chargeTravail = chargeTravail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}