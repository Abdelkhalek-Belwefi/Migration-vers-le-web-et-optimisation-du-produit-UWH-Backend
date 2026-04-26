package com.example.pfe.reception.repository;

import com.example.pfe.reception.entity.Reception;
import com.example.pfe.reception.entity.ReceptionStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReceptionRepository extends JpaRepository<Reception, Long> {

    // ✅ Changé de Optional à List pour permettre plusieurs réceptions avec le même PO
    List<Reception> findByNumeroPO(String numeroPO);

    List<Reception> findByStatut(ReceptionStatut statut);

    List<Reception> findByDateReceptionBetween(LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT r FROM Reception r WHERE " +
            "(:numeroPO IS NULL OR r.numeroPO LIKE %:numeroPO%) AND " +
            "(:fournisseur IS NULL OR r.fournisseur LIKE %:fournisseur%) AND " +
            "(:statut IS NULL OR r.statut = :statut)")
    List<Reception> searchReceptions(@Param("numeroPO") String numeroPO,
                                     @Param("fournisseur") String fournisseur,
                                     @Param("statut") ReceptionStatut statut);

    // ========== NOUVELLES MÉTHODES POUR LE FILTRAGE PAR ENTREPÔT ==========

    /**
     * Récupère toutes les réceptions d'un entrepôt spécifique
     */
    List<Reception> findByEntrepotId(Long entrepotId);

    /**
     * Récupère les réceptions par statut dans un entrepôt spécifique
     */
    List<Reception> findByStatutAndEntrepotId(ReceptionStatut statut, Long entrepotId);

    /**
     * Récupère les réceptions par numéro PO dans un entrepôt spécifique
     */
    List<Reception> findByNumeroPOAndEntrepotId(String numeroPO, Long entrepotId);

    /**
     * Récupère les réceptions par fournisseur dans un entrepôt spécifique
     */
    List<Reception> findByFournisseurContainingIgnoreCaseAndEntrepotId(String fournisseur, Long entrepotId);

    /**
     * Recherche avancée des réceptions avec filtre par entrepôt
     */
    @Query("SELECT r FROM Reception r WHERE " +
            "(:entrepotId IS NULL OR r.entrepot.id = :entrepotId) AND " +
            "(:numeroPO IS NULL OR r.numeroPO LIKE %:numeroPO%) AND " +
            "(:fournisseur IS NULL OR r.fournisseur LIKE %:fournisseur%) AND " +
            "(:statut IS NULL OR r.statut = :statut)")
    List<Reception> searchReceptionsWithEntrepot(@Param("entrepotId") Long entrepotId,
                                                 @Param("numeroPO") String numeroPO,
                                                 @Param("fournisseur") String fournisseur,
                                                 @Param("statut") ReceptionStatut statut);

    /**
     * Vérifie si une réception avec ce numéro PO existe dans un entrepôt spécifique
     */
    boolean existsByNumeroPOAndEntrepotId(String numeroPO, Long entrepotId);
}