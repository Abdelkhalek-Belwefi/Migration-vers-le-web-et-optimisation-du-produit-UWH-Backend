package com.example.pfe.commande.repository;

import com.example.pfe.commande.entity.Commande;
import com.example.pfe.commande.enums.StatutCommande;
import com.example.pfe.commande.enums.TypeCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findByStatut(StatutCommande statut);

    @Query("SELECT c FROM Commande c WHERE c.statut = :statut AND NOT EXISTS (SELECT e FROM Expedition e WHERE e.commande = c)")
    List<Commande> findByStatutAndNoExpedition(@Param("statut") StatutCommande statut);

    // ========== NOUVELLES MÉTHODES POUR LE FILTRAGE PAR ENTREPÔT ==========

    /**
     * Récupère toutes les commandes d'un entrepôt spécifique
     */
    List<Commande> findByEntrepotId(Long entrepotId);

    /**
     * Récupère les commandes par statut dans un entrepôt spécifique
     */
    List<Commande> findByStatutAndEntrepotId(StatutCommande statut, Long entrepotId);

    /**
     * Récupère les commandes d'un client dans un entrepôt spécifique
     */
    List<Commande> findByClientIdAndEntrepotId(Long clientId, Long entrepotId);

    /**
     * Récupère les commandes à expédier (validées sans expédition) dans un entrepôt spécifique
     */
    @Query("SELECT c FROM Commande c WHERE c.statut = :statut AND c.entrepot.id = :entrepotId AND NOT EXISTS (SELECT e FROM Expedition e WHERE e.commande = c)")
    List<Commande> findByStatutAndEntrepotIdAndNoExpedition(@Param("statut") StatutCommande statut,
                                                            @Param("entrepotId") Long entrepotId);

    /**
     * Recherche des commandes par numéro (filtrée par entrepôt)
     */
    List<Commande> findByNumeroCommandeContainingIgnoreCaseAndEntrepotId(String numeroCommande, Long entrepotId);

    /**
     * Vérifie si une commande existe dans un entrepôt spécifique
     */
    boolean existsByIdAndEntrepotId(Long id, Long entrepotId);

    // ========== NOUVELLES MÉTHODES POUR LES TRANSFERTS ENTRE ENTREPÔTS ==========

    /**
     * Récupère les commandes par type (CLIENT ou TRANSFERT)
     */
    List<Commande> findByTypeCommande(TypeCommande typeCommande);

    /**
     * Récupère les commandes de transfert où l'entrepôt destination est celui spécifié
     */
    List<Commande> findByTypeCommandeAndEntrepotDestinationId(TypeCommande typeCommande, Long entrepotDestinationId);

    /**
     * Récupère les commandes de transfert où l'entrepôt source est celui spécifié
     */
    List<Commande> findByTypeCommandeAndEntrepotSourceId(TypeCommande typeCommande, Long entrepotSourceId);

    /**
     * Récupère les commandes de transfert par statut et entrepôt destination
     */
    List<Commande> findByTypeCommandeAndStatutAndEntrepotDestinationId(TypeCommande typeCommande,
                                                                       StatutCommande statut,
                                                                       Long entrepotDestinationId);

    /**
     * Récupère les commandes de transfert par statut et entrepôt source
     */
    List<Commande> findByTypeCommandeAndStatutAndEntrepotSourceId(TypeCommande typeCommande,
                                                                  StatutCommande statut,
                                                                  Long entrepotSourceId);

    /**
     * Récupère toutes les commandes de transfert (filtrées par entrepôt de l'utilisateur)
     */
    @Query("SELECT c FROM Commande c WHERE c.typeCommande = :typeCommande AND " +
            "(c.entrepotSource.id = :entrepotId OR c.entrepotDestination.id = :entrepotId)")
    List<Commande> findTransfertsByEntrepot(@Param("typeCommande") TypeCommande typeCommande,
                                            @Param("entrepotId") Long entrepotId);

    // Dans CommandeRepository.java
    List<Commande> findByTypeCommandeAndEntrepotSourceIdAndStatut(TypeCommande typeCommande,
                                                                  Long entrepotSourceId,
                                                                  StatutCommande statut);
}