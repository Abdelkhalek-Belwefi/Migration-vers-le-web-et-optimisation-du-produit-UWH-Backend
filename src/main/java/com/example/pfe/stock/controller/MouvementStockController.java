package com.example.pfe.stock.controller;

import com.example.pfe.stock.dto.MouvementStockDTO;
import com.example.pfe.stock.service.MouvementStockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mouvements")
@CrossOrigin(origins = "http://localhost:5173")
public class MouvementStockController {

    private final MouvementStockService mouvementService;

    public MouvementStockController(MouvementStockService mouvementService) {
        this.mouvementService = mouvementService;
    }

    @PostMapping("/entree")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<MouvementStockDTO> entreeStock(
            @RequestParam Long stockId,
            @RequestParam int quantite,
            @RequestParam String motif,
            @RequestParam(required = false) String commentaire) {

        System.out.println("=== REQUÊTE ENTREE STOCK ===");
        System.out.println("stockId: " + stockId);
        System.out.println("quantite: " + quantite);
        System.out.println("motif: " + motif);

        return ResponseEntity.ok(mouvementService.entreeStock(stockId, quantite, motif, commentaire));
    }

    @PostMapping("/sortie")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<MouvementStockDTO> sortieStock(
            @RequestParam Long stockId,
            @RequestParam int quantite,
            @RequestParam String motif,
            @RequestParam(required = false) String commentaire) {

        System.out.println("=== REQUÊTE SORTIE STOCK ===");
        System.out.println("stockId: " + stockId);
        System.out.println("quantite: " + quantite);
        System.out.println("motif: " + motif);

        return ResponseEntity.ok(mouvementService.sortieStock(stockId, quantite, motif, commentaire));
    }

    @PostMapping("/transfert")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<?> transfererStock(
            @RequestParam Long stockIdSource,
            @RequestParam String emplacementDestination,
            @RequestParam int quantite,
            @RequestParam String motif,
            @RequestParam(required = false) String commentaire) {

        System.out.println("=== REQUÊTE TRANSFERT STOCK ===");
        System.out.println("stockIdSource: " + stockIdSource);
        System.out.println("emplacementDestination: " + emplacementDestination);
        System.out.println("quantite: " + quantite);
        System.out.println("motif: " + motif);
        System.out.println("commentaire: " + commentaire);

        // Validation des paramètres
        if (quantite <= 0) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "La quantité doit être positive");
            return ResponseEntity.badRequest().body(error);
        }

        if (emplacementDestination == null || emplacementDestination.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "L'emplacement destination est requis");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            MouvementStockDTO result = mouvementService.transfererStock(
                    stockIdSource, emplacementDestination.trim().toUpperCase(), quantite, motif, commentaire);
            System.out.println("✅ Transfert réussi, ID mouvement: " + result.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du transfert: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/stock/{stockId}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<?> getMouvementsByStock(@PathVariable Long stockId) {
        try {
            return ResponseEntity.ok(mouvementService.getMouvementsByStock(stockId));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<?> getAllMouvements() {
        try {
            return ResponseEntity.ok(mouvementService.getAllMouvements());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}