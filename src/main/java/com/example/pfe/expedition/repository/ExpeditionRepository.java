package com.example.pfe.expedition.repository;

import com.example.pfe.expedition.entity.Expedition;
import com.example.pfe.expedition.entity.ExpeditionStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpeditionRepository extends JpaRepository<Expedition, Long> {

    List<Expedition> findByStatut(ExpeditionStatut statut);

    Optional<Expedition> findByCommandeId(Long commandeId);

    // ========== NOUVELLES MÉTHODES POUR LE FILTRAGE PAR ENTREPÔT ==========

    /**
     * Récupère toutes les expéditions d'un entrepôt spécifique
     */
    List<Expedition> findByEntrepotId(Long entrepotId);

    /**
     * Récupère les expéditions par statut dans un entrepôt spécifique
     */
    List<Expedition> findByStatutAndEntrepotId(ExpeditionStatut statut, Long entrepotId);

    /**
     * Récupère les expéditions préparées par un utilisateur dans un entrepôt spécifique
     */
    List<Expedition> findByPrepareParIdAndEntrepotId(Long prepareParId, Long entrepotId);

    /**
     * Vérifie si une expédition existe pour une commande dans un entrepôt spécifique
     */
    boolean existsByCommandeIdAndEntrepotId(Long commandeId, Long entrepotId);

    /**
     * Récupère l'expédition d'une commande dans un entrepôt spécifique
     */
    Optional<Expedition> findByCommandeIdAndEntrepotId(Long commandeId, Long entrepotId);

    /**
     * Recherche des expéditions par numéro BL (filtrée par entrepôt)
     */
    List<Expedition> findByNumeroBLContainingIgnoreCaseAndEntrepotId(String numeroBL, Long entrepotId);

    /**
     * Récupère les expéditions avec filtre par statut et entrepôt (version Query)
     */
    @Query("SELECT e FROM Expedition e WHERE " +
            "(:entrepotId IS NULL OR e.entrepot.id = :entrepotId) AND " +
            "(:statut IS NULL OR e.statut = :statut)")
    List<Expedition> findByStatutAndEntrepotIdNullable(@Param("statut") ExpeditionStatut statut,
                                                       @Param("entrepotId") Long entrepotId);
}