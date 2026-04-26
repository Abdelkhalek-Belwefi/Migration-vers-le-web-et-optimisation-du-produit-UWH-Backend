package com.example.pfe.reception.controller;

import com.example.pfe.reception.dto.PutawayTaskDTO;
import com.example.pfe.reception.dto.ReceptionDTO;
import com.example.pfe.reception.dto.ReceptionLineDTO;
import com.example.pfe.reception.entity.ReceptionStatut;
import com.example.pfe.reception.service.ReceptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reception")
@CrossOrigin(origins = "http://localhost:5173")
public class ReceptionController {

    private final ReceptionService receptionService;

    public ReceptionController(ReceptionService receptionService) {
        this.receptionService = receptionService;
    }

    // ========== MÉTHODES EXISTANTES (INCHANGÉES) ==========

    @PostMapping
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ReceptionDTO> createReception(@RequestBody ReceptionDTO receptionDTO) {
        System.out.println("=== CRÉATION RÉCEPTION ===");
        System.out.println("Numéro PO: " + receptionDTO.getNumeroPO());
        System.out.println("Nombre de lignes: " + (receptionDTO.getLignes() != null ? receptionDTO.getLignes().size() : 0));
        return ResponseEntity.ok(receptionService.createReception(receptionDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ReceptionDTO> getReceptionById(@PathVariable Long id) {
        return ResponseEntity.ok(receptionService.getReceptionById(id));
    }

    @GetMapping("/po/{numeroPO}")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ReceptionDTO> getReceptionByPO(@PathVariable String numeroPO) {
        return ResponseEntity.ok(receptionService.getReceptionByPO(numeroPO));
    }

    @PostMapping("/{receptionId}/lines")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ReceptionLineDTO> addLineToReception(
            @PathVariable Long receptionId,
            @RequestBody ReceptionLineDTO lineDTO) {
        return ResponseEntity.ok(receptionService.addLineToReception(receptionId, lineDTO));
    }

    @PutMapping("/lines/{lineId}")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ReceptionLineDTO> updateReceptionLine(
            @PathVariable Long lineId,
            @RequestParam int quantiteRecue,
            @RequestParam(required = false) String lot,
            @RequestParam(required = false) LocalDateTime dateExpiration,
            @RequestParam(required = false) String emplacement) {
        return ResponseEntity.ok(receptionService.updateReceptionLine(lineId, quantiteRecue,
                lot, dateExpiration, emplacement));
    }

    @PutMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ReceptionDTO> validerReception(@PathVariable Long id) {
        return ResponseEntity.ok(receptionService.validerReception(id));
    }

    @GetMapping("/{receptionId}/putaway")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<PutawayTaskDTO>> getPutawayTasks(@PathVariable Long receptionId) {
        return ResponseEntity.ok(receptionService.getPutawayTasksByReception(receptionId));
    }

    // ========== MÉTHODES MODIFIÉES (FILTRAGE PAR ENTREPÔT) ==========

    /**
     * Récupère toutes les réceptions (filtrées par entrepôt de l'utilisateur connecté)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<ReceptionDTO>> getAllReceptions() {
        return ResponseEntity.ok(receptionService.getAllReceptionsFiltered());
    }

    /**
     * Recherche avancée des réceptions (filtrée par entrepôt de l'utilisateur connecté)
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<ReceptionDTO>> searchReceptions(
            @RequestParam(required = false) String numeroPO,
            @RequestParam(required = false) String fournisseur,
            @RequestParam(required = false) ReceptionStatut statut) {
        return ResponseEntity.ok(receptionService.searchReceptionsFiltered(numeroPO, fournisseur, statut));
    }

    /**
     * Récupère toutes les tâches de rangement (filtrées par entrepôt de l'utilisateur connecté)
     */
    @GetMapping("/putaway")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<PutawayTaskDTO>> getAllPutawayTasks() {
        return ResponseEntity.ok(receptionService.getAllPutawayTasksFiltered());
    }

    // ========== MÉTHODE DOCUMENT (INCHANGÉE) ==========

    @GetMapping("/document/{code}")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ReceptionDTO> getDocumentInfo(@PathVariable String code) {
        System.out.println("=== RECHERCHE DOCUMENT ===");
        System.out.println("Code scanné: " + code);
        return ResponseEntity.ok(receptionService.getDocumentInfo(code));
    }
}