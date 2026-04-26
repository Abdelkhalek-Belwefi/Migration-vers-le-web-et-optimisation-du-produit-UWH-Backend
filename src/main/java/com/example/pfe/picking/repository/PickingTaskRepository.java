package com.example.pfe.picking.repository;

import com.example.pfe.picking.entity.PickingTask;
import com.example.pfe.picking.enums.StatutPicking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PickingTaskRepository extends JpaRepository<PickingTask, Long> {

    List<PickingTask> findByAssignedToAndStatut(String assignedTo, StatutPicking statut);

    List<PickingTask> findByCommandeId(Long commandeId);

    List<PickingTask> findByStatut(StatutPicking statut);

    // ========== NOUVELLES MÉTHODES POUR LE FILTRAGE PAR ENTREPÔT ==========

    /**
     * Récupère toutes les tâches de picking d'un entrepôt spécifique
     */
    List<PickingTask> findByEntrepotId(Long entrepotId);

    /**
     * Récupère les tâches de picking par statut dans un entrepôt spécifique
     */
    List<PickingTask> findByStatutAndEntrepotId(StatutPicking statut, Long entrepotId);

    /**
     * Récupère les tâches de picking assignées à un opérateur dans un entrepôt spécifique
     */
    List<PickingTask> findByAssignedToAndStatutAndEntrepotId(String assignedTo, StatutPicking statut, Long entrepotId);

    /**
     * Recherche des tâches de picking avec filtre par statut et entrepôt (version Query)
     */
    @Query("SELECT t FROM PickingTask t WHERE " +
            "(:entrepotId IS NULL OR t.entrepot.id = :entrepotId) AND " +
            "(:statut IS NULL OR t.statut = :statut)")
    List<PickingTask> findByStatutAndEntrepotIdNullable(@Param("statut") StatutPicking statut,
                                                        @Param("entrepotId") Long entrepotId);
}