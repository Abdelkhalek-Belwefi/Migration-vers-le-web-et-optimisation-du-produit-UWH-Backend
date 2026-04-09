package com.example.pfe.expedition.controller;

import com.example.pfe.expedition.dto.ExpeditionDTO;
import com.example.pfe.expedition.entity.ExpeditionStatut;
import com.example.pfe.expedition.service.ExpeditionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expeditions")
@CrossOrigin(origins = "http://localhost:5173")
public class ExpeditionController {

    private final ExpeditionService expeditionService;

    public ExpeditionController(ExpeditionService expeditionService) {
        this.expeditionService = expeditionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<ExpeditionDTO>> getAllExpeditions() {
        return ResponseEntity.ok(expeditionService.getAllExpeditions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ExpeditionDTO> getExpeditionById(@PathVariable Long id) {
        return ResponseEntity.ok(expeditionService.getExpeditionById(id));
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<ExpeditionDTO>> getExpeditionsByStatut(@PathVariable ExpeditionStatut statut) {
        return ResponseEntity.ok(expeditionService.getExpeditionsByStatut(statut));
    }

    @PostMapping("/expedier")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ExpeditionDTO> expedierCommande(@RequestParam Long commandeId, @RequestParam String transporteur) {
        return ResponseEntity.ok(expeditionService.expedierCommande(commandeId, transporteur));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ExpeditionDTO> updateStatut(@PathVariable Long id, @RequestParam ExpeditionStatut statut) {
        return ResponseEntity.ok(expeditionService.updateStatut(id, statut));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<Void> deleteExpedition(@PathVariable Long id) {
        expeditionService.deleteExpedition(id);
        return ResponseEntity.ok().build();
    }

    // ========== NOUVEL ENDPOINT POUR IMPRIMER LE BON DE LIVRAISON (HTML) ==========
    @GetMapping("/{id}/print")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<String> printExpedition(@PathVariable Long id) {
        String htmlContent = expeditionService.generateExpeditionPrintHtml(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(htmlContent, headers, HttpStatus.OK);
    }
}