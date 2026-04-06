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

    @GetMapping
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<List<CommandeDTO>> getAllCommandes() {
        return ResponseEntity.ok(commandeService.getAllCommandes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<CommandeDTO> getCommandeById(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.getCommandeById(id));
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<List<CommandeDTO>> getCommandesByStatut(@PathVariable String statut) {
        StatutCommande statutEnum = StatutCommande.valueOf(statut.toUpperCase());
        return ResponseEntity.ok(commandeService.getCommandesByStatut(statutEnum));
    }

    @GetMapping("/a-expedier")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<CommandeDTO>> getCommandesAExpedier() {
        return ResponseEntity.ok(commandeService.getCommandesAExpedier());
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<Void> deleteCommande(@PathVariable Long id) {
        commandeService.deleteCommande(id);
        return ResponseEntity.ok().build();
    }
}
