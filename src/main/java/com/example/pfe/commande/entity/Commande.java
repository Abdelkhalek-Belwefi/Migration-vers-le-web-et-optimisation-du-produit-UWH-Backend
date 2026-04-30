package com.example.pfe.commande.entity;

import com.example.pfe.client.entity.Client;
import com.example.pfe.commande.enums.StatutCommande;
import com.example.pfe.commande.enums.TypeCommande;
import com.example.pfe.entrepot.entity.Warehouse;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commandes")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numeroCommande;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)  // nullable true pour transfert
    private Client client;

    @Column(name = "date_commande", nullable = false)
    private LocalDateTime dateCommande;

    @Column(name = "date_livraison_souhaitee")
    private LocalDate dateLivraisonSouhaitee;

    @Enumerated(EnumType.STRING)
    private StatutCommande statut;

    private String notes;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== ENTREPÔT EXISTANT ==========
    @ManyToOne
    @JoinColumn(name = "entrepot_id", nullable = false)
    private Warehouse entrepot;

    // ========== NOUVEAUX CHAMPS POUR TRANSFERT ==========
    @Enumerated(EnumType.STRING)
    @Column(name = "type_commande", nullable = false)
    private TypeCommande typeCommande = TypeCommande.CLIENT;  // Valeur par défaut

    @ManyToOne
    @JoinColumn(name = "entrepot_source_id", nullable = true)
    private Warehouse entrepotSource;

    @ManyToOne
    @JoinColumn(name = "entrepot_destination_id", nullable = true)
    private Warehouse entrepotDestination;

    public Commande() {
        this.dateCommande = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.statut = StatutCommande.EN_ATTENTE;
    }

    // Getters et setters existants (inchangés)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroCommande() { return numeroCommande; }
    public void setNumeroCommande(String numeroCommande) { this.numeroCommande = numeroCommande; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }

    public LocalDate getDateLivraisonSouhaitee() { return dateLivraisonSouhaitee; }
    public void setDateLivraisonSouhaitee(LocalDate dateLivraisonSouhaitee) { this.dateLivraisonSouhaitee = dateLivraisonSouhaitee; }

    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande statut) { this.statut = statut; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<LigneCommande> getLignes() { return lignes; }
    public void setLignes(List<LigneCommande> lignes) { this.lignes = lignes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Warehouse getEntrepot() { return entrepot; }
    public void setEntrepot(Warehouse entrepot) { this.entrepot = entrepot; }

    // Nouveaux getters et setters
    public TypeCommande getTypeCommande() { return typeCommande; }
    public void setTypeCommande(TypeCommande typeCommande) { this.typeCommande = typeCommande; }

    public Warehouse getEntrepotSource() { return entrepotSource; }
    public void setEntrepotSource(Warehouse entrepotSource) { this.entrepotSource = entrepotSource; }

    public Warehouse getEntrepotDestination() { return entrepotDestination; }
    public void setEntrepotDestination(Warehouse entrepotDestination) { this.entrepotDestination = entrepotDestination; }
}