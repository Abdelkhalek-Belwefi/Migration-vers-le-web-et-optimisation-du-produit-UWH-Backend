package com.example.pfe.commande.controller;

import com.example.pfe.commande.dto.CommandeDTO;
import com.example.pfe.commande.enums.StatutCommande;
import com.example.pfe.commande.service.CommandeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@CrossOrigin(origins = "http://localhost:5173")
public class CommandeController {

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    // ========== MÉTHODES EXISTANTES (INCHANGÉES) ==========

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<CommandeDTO> getCommandeById(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.getCommandeById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<CommandeDTO> createCommande(@RequestBody CommandeDTO commandeDTO) {
        return ResponseEntity.ok(commandeService.createCommande(commandeDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<CommandeDTO> updateCommande(@PathVariable Long id, @RequestBody CommandeDTO commandeDTO) {
        return ResponseEntity.ok(commandeService.updateCommande(id, commandeDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<Void> deleteCommande(@PathVariable Long id) {
        commandeService.deleteCommande(id);
        return ResponseEntity.ok().build();
    }

    // ========== MÉTHODES MODIFIÉES (FILTRAGE PAR ENTREPÔT) ==========

    /**
     * Récupère toutes les commandes
     * - SERVICE_COMMERCIAL / ADMIN : voit tout (pas de filtre)
     * - RESPONSABLE_ENTREPOT / OPERATEUR_ENTREPOT : voit uniquement son entrepôt
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT', 'OPERATEUR_ENTREPOT')")
    public ResponseEntity<List<CommandeDTO>> getAllCommandes() {
        // Pour SERVICE_COMMERCIAL et ADMIN, on garde le comportement original (tout voir)
        String role = getCurrentUserRole();
        if ("SERVICE_COMMERCIAL".equals(role) || "ADMINISTRATEUR".equals(role)) {
            return ResponseEntity.ok(commandeService.getAllCommandes());
        }
        // Pour RESPONSABLE_ENTREPOT et OPERATEUR_ENTREPOT, on filtre par entrepôt
        return ResponseEntity.ok(commandeService.getAllCommandesFiltered());
    }

    /**
     * Récupère les commandes par statut
     * - SERVICE_COMMERCIAL / ADMIN : voit tout
     * - RESPONSABLE_ENTREPOT / OPERATEUR_ENTREPOT : voit uniquement son entrepôt
     */
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<List<CommandeDTO>> getCommandesByStatut(@PathVariable String statut) {
        StatutCommande statutEnum = StatutCommande.valueOf(statut.toUpperCase());
        String role = getCurrentUserRole();

        if ("SERVICE_COMMERCIAL".equals(role) || "ADMINISTRATEUR".equals(role)) {
            return ResponseEntity.ok(commandeService.getCommandesByStatut(statutEnum));
        }
        return ResponseEntity.ok(commandeService.getCommandesByStatutFiltered(statutEnum));
    }

    /**
     * Récupère les commandes à expédier
     * - RESPONSABLE_ENTREPOT uniquement, filtre par son entrepôt
     */
    @GetMapping("/a-expedier")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<CommandeDTO>> getCommandesAExpedier() {
        String role = getCurrentUserRole();
        if ("ADMINISTRATEUR".equals(role)) {
            return ResponseEntity.ok(commandeService.getCommandesAExpedier());
        }
        return ResponseEntity.ok(commandeService.getCommandesAExpedierFiltered());
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<CommandeDTO> updateStatut(@PathVariable Long id, @RequestParam String statut) {
        StatutCommande statutEnum;
        try {
            String cleaned = statut.toUpperCase()
                    .replace("É", "E")
                    .replace("È", "E")
                    .replace("Ê", "E")
                    .replace(" ", "_");
            statutEnum = StatutCommande.valueOf(cleaned);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut invalide : " + statut);
        }
        return ResponseEntity.ok(commandeService.updateStatut(id, statutEnum));
    }

    // ========== NOUVEAUX ENDPOINTS POUR TRANSFERT ENTRE ENTREPÔTS ==========

    /**
     * Crée une commande de transfert entre entrepôts
     */
    @PostMapping("/transfert")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<CommandeDTO> createCommandeTransfert(@RequestBody CommandeDTO commandeDTO) {
        return ResponseEntity.ok(commandeService.createCommandeTransfert(commandeDTO));
    }

    /**
     * Récupère les demandes de transfert reçues par l'entrepôt de l'utilisateur connecté
     */
    @GetMapping("/transfert/recues")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<CommandeDTO>> getCommandesTransfertRecues() {
        return ResponseEntity.ok(commandeService.getCommandesTransfertRecues());
    }

    /**
     * Accepte une demande de transfert (la commande passe en VALIDEE)
     */
    @PatchMapping("/transfert/{id}/accepter")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<CommandeDTO> accepterDemandeTransfert(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.accepterDemandeTransfert(id));
    }

    /**
     * Refuse une demande de transfert
     */
    @PatchMapping("/transfert/{id}/refuser")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<CommandeDTO> refuserDemandeTransfert(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.refuserDemandeTransfert(id));
    }

    // ========== MÉTHODE UTILITAIRE POUR RÉCUPÉRER LE RÔLE ==========

    private String getCurrentUserRole() {
        org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .forEach(authority -> {
                    // Récupération du rôle
                });
        // Alternative plus simple
        return org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("");
    }
    @GetMapping("/transfert/source")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<CommandeDTO>> getCommandesTransfertSource() {
        return ResponseEntity.ok(commandeService.getCommandesTransfertSource());
    }

    // Dans CommandeController.java - Ajouter cette méthode
    @GetMapping("/transfert/preparer")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<CommandeDTO>> getCommandesTransfertAPreparer() {
        return ResponseEntity.ok(commandeService.getCommandesTransfertAPreparer());
    }
}