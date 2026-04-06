package com.example.pfe.picking.repository;

import com.example.pfe.picking.entity.PickingTask;
import com.example.pfe.picking.enums.StatutPicking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PickingTaskRepository extends JpaRepository<PickingTask, Long> {
    List<PickingTask> findByAssignedToAndStatut(String assignedTo, StatutPicking statut);
    List<PickingTask> findByCommandeId(Long commandeId);
    List<PickingTask> findByStatut(StatutPicking statut);
}