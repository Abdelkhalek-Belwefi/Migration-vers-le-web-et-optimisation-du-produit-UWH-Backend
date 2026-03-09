package com.example.pfe.reception.entity;

import com.example.pfe.auth.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receptions")
public class Reception {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_po", nullable = false, unique = true)
    private String numeroPO;          // Purchase Order number

    @Column(name = "date_reception")
    private LocalDateTime dateReception;

    @Enumerated(EnumType.STRING)
    private ReceptionStatut statut;

    @Column(name = "fournisseur")
    private String fournisseur;

    @Column(name = "bon_livraison")
    private String bonLivraison;      // Delivery note number

    @ManyToOne
    @JoinColumn(name = "createur_id")
    private User createur;             // Opérateur qui a créé la réception

    @ManyToOne
    @JoinColumn(name = "valideur_id")
    private User valideur;             // Responsable qui a validé

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @OneToMany(mappedBy = "reception", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReceptionLine> lignes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (dateReception == null) {
            dateReception = LocalDateTime.now();
        }
    }

    // --- Getters et Setters ---
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

    public User getCreateur() { return createur; }
    public void setCreateur(User createur) { this.createur = createur; }

    public User getValideur() { return valideur; }
    public void setValideur(User valideur) { this.valideur = valideur; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getValidatedAt() { return validatedAt; }
    public void setValidatedAt(LocalDateTime validatedAt) { this.validatedAt = validatedAt; }

    public List<ReceptionLine> getLignes() { return lignes; }
    public void setLignes(List<ReceptionLine> lignes) { this.lignes = lignes; }
}