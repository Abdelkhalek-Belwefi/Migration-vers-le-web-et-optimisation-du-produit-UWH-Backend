package com.example.pfe.rangement.controller;

import com.example.pfe.reception.dto.PutawayTaskDTO;
import com.example.pfe.rangement.service.RangementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rangement")
@CrossOrigin(origins = "http://localhost:5173")
public class RangementController {

    private final RangementService rangementService;

    public RangementController(RangementService rangementService) {
        this.rangementService = rangementService;
    }

    /**
     * Liste toutes les tâches à faire
     */
    @GetMapping("/a-faire")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<PutawayTaskDTO>> getTasksAFaire() {
        return ResponseEntity.ok(rangementService.getTasksAFaire());
    }

    /**
     * Liste toutes les tâches (pour supervision)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<PutawayTaskDTO>> getAllTasks() {
        return ResponseEntity.ok(rangementService.getAllTasks());
    }

    /**
     * Liste les tâches par statut
     */
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<PutawayTaskDTO>> getTasksByStatut(@PathVariable String statut) {
        return ResponseEntity.ok(rangementService.getTasksByStatut(statut));
    }

    /**
     * Commencer une tâche
     */
    @PutMapping("/{id}/commencer")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<PutawayTaskDTO> commencerTask(@PathVariable Long id) {
        return ResponseEntity.ok(rangementService.commencerTask(id));
    }

    /**
     * Terminer une tâche
     */
    @PutMapping("/{id}/terminer")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<PutawayTaskDTO> terminerTask(
            @PathVariable Long id,
            @RequestParam(required = false) String emplacementReel) {
        return ResponseEntity.ok(rangementService.terminerTask(id, emplacementReel));
    }
}