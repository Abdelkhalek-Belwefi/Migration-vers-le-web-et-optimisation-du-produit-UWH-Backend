package com.example.pfe.livraison.controller;

import com.example.pfe.livraison.dto.LivraisonDTO;
import com.example.pfe.livraison.dto.ValidationLivraisonRequest;
import com.example.pfe.livraison.service.LivraisonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transporteur")
@CrossOrigin(origins = "http://localhost:5173")
public class TransporteurController {

    private final LivraisonService livraisonService;

    public TransporteurController(LivraisonService livraisonService) {
        this.livraisonService = livraisonService;
    }

    @GetMapping("/livraisons/en-cours")
    @PreAuthorize("hasRole('TRANSPORTEUR')")
    public ResponseEntity<List<LivraisonDTO>> getLivraisonsEnCours() {
        return ResponseEntity.ok(livraisonService.getLivraisonsPourTransporteur());
    }

    @GetMapping("/livraisons/historique")
    @PreAuthorize("hasRole('TRANSPORTEUR')")
    public ResponseEntity<List<LivraisonDTO>> getHistoriqueLivraisons() {
        return ResponseEntity.ok(livraisonService.getHistoriqueLivraisons());
    }

    @PostMapping("/livraisons/{livraisonId}/valider")
    @PreAuthorize("hasRole('TRANSPORTEUR')")
    public ResponseEntity<LivraisonDTO> validerLivraison(
            @PathVariable Long livraisonId,
            @RequestBody ValidationLivraisonRequest request) {
        return ResponseEntity.ok(livraisonService.validerLivraison(livraisonId, request));
    }

    // ========== NOUVEAU ENDPOINT POUR LIVRAISONS EN ATTENTE (ENTREPÔT DEMANDEUR) ==========
    @GetMapping("/livraisons/entrepot/attente")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<LivraisonDTO>> getLivraisonsEnAttentePourEntrepot() {
        return ResponseEntity.ok(livraisonService.getLivraisonsEnAttentePourEntrepot());
    }
}