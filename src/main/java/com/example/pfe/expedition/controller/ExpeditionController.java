package com.example.pfe.expedition.controller;

import com.example.pfe.expedition.dto.ExpeditionDTO;
import com.example.pfe.expedition.entity.ExpeditionStatut;
import com.example.pfe.expedition.service.ExpeditionService;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.auth.entity.Role;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expeditions")
@CrossOrigin(origins = "http://localhost:5173")
public class ExpeditionController {

    private final ExpeditionService expeditionService;
    private final UserRepository userRepository;   // ← AJOUT

    public ExpeditionController(ExpeditionService expeditionService, UserRepository userRepository) {
        this.expeditionService = expeditionService;
        this.userRepository = userRepository;      // ← AJOUT
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

    @GetMapping("/{id}/print")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<String> printExpedition(@PathVariable Long id) {
        String htmlContent = expeditionService.generateExpeditionPrintHtml(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(htmlContent, headers, HttpStatus.OK);
    }

    @GetMapping("/mes-expeditions")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR', 'OPERATEUR_ENTREPOT')")
    public ResponseEntity<List<ExpeditionDTO>> getMesExpeditions() {
        return ResponseEntity.ok(expeditionService.getExpeditionsByCurrentUser());
    }

    @GetMapping("/liste")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<ExpeditionDTO>> getListeExpeditions() {
        return ResponseEntity.ok(expeditionService.getAllExpeditionsForList());
    }

    // ========== NOUVEAU : Récupérer la liste des transporteurs ==========
    @GetMapping("/transporteurs")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<TransporteurDTO>> getTransporteurs() {
        List<User> transporteurs = userRepository.findByRole(Role.TRANSPORTEUR);
        List<TransporteurDTO> dtos = transporteurs.stream()
                .map(u -> new TransporteurDTO(u.getId(), u.getPrenom(), u.getNom()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // DTO interne
    static class TransporteurDTO {
        private Long id;
        private String prenom;
        private String nom;

        public TransporteurDTO(Long id, String prenom, String nom) {
            this.id = id;
            this.prenom = prenom;
            this.nom = nom;
        }

        public Long getId() { return id; }
        public String getPrenom() { return prenom; }
        public String getNom() { return nom; }
    }
    // ajout ce methode
// Dans ExpeditionController.java, ajouter cette méthode après les autres

    @PostMapping("/expedier-avec-id")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ExpeditionDTO> expedierCommandeAvecId(
            @RequestParam Long commandeId,
            @RequestParam Long transporteurId) {
        return ResponseEntity.ok(expeditionService.expedierCommandeWithTransporteurId(commandeId, transporteurId));
    }
}