package com.example.pfe.reception.repository;

import com.example.pfe.reception.entity.PutawayTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PutawayTaskRepository extends JpaRepository<PutawayTask, Long> {

    List<PutawayTask> findByStatut(String statut);

    List<PutawayTask> findByReceptionId(Long receptionId);

    List<PutawayTask> findByEmplacementDestination(String emplacement);

    // ========== NOUVELLES MÉTHODES POUR LE FILTRAGE PAR ENTREPÔT ==========

    /**
     * Récupère toutes les tâches de rangement d'un entrepôt spécifique
     */
    List<PutawayTask> findByEntrepotId(Long entrepotId);

    /**
     * Récupère les tâches de rangement par statut dans un entrepôt spécifique
     */
    List<PutawayTask> findByStatutAndEntrepotId(String statut, Long entrepotId);

    /**
     * Récupère les tâches de rangement d'une réception dans un entrepôt spécifique
     */
    List<PutawayTask> findByReceptionIdAndEntrepotId(Long receptionId, Long entrepotId);

    /**
     * Récupère les tâches de rangement par emplacement destination dans un entrepôt spécifique
     */
    List<PutawayTask> findByEmplacementDestinationAndEntrepotId(String emplacement, Long entrepotId);

    /**
     * Récupère les tâches de rangement avec filtre par statut et entrepôt (version Query pour plus de flexibilité)
     */
    @Query("SELECT t FROM PutawayTask t WHERE " +
            "(:entrepotId IS NULL OR t.entrepot.id = :entrepotId) AND " +
            "(:statut IS NULL OR t.statut = :statut)")
    List<PutawayTask> findByStatutAndEntrepotIdNullable(@Param("statut") String statut,
                                                        @Param("entrepotId") Long entrepotId);
}