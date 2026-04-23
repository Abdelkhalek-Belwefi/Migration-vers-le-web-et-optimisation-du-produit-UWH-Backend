package com.example.pfe.admin.controller;

import com.example.pfe.admin.dto.UserDTO;
import com.example.pfe.admin.dto.RoleUpdateRequest;
import com.example.pfe.admin.service.AdminService;
import com.example.pfe.livraison.dto.LivraisonDTO;
import com.example.pfe.livraison.service.LivraisonService;   // ← AJOUT (correction)
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final AdminService adminService;
    private final LivraisonService livraisonService;          // ← AJOUT (correction)

    // Constructeur modifié pour inclure LivraisonService (ajout, pas de suppression)
    public AdminController(AdminService adminService, LivraisonService livraisonService) {
        this.adminService = adminService;
        this.livraisonService = livraisonService;            // ← AJOUT
    }

    // ⬇️ TOUTES LES MÉTHODES EXISTANTES RESTENT IDENTIQUES ⬇️

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateUserRole(id, request.getRole()));
    }

    @PutMapping("/users/{id}/activer")
    public ResponseEntity<UserDTO> activerCompte(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.activerCompte(id));
    }

    @PutMapping("/users/{id}/desactiver")
    public ResponseEntity<UserDTO> desactiverCompte(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.desactiverCompte(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAllRoles() {
        return ResponseEntity.ok(adminService.getAllRoles());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(adminService.createUser(userDTO));
    }

    // ========== NOUVELLE MÉTHODE POUR ASSIGNER UN TRANSPORTEUR ==========
    @PostMapping("/expeditions/{expeditionId}/assigner-transporteur")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT')")
    public ResponseEntity<LivraisonDTO> assignerTransporteur(
            @PathVariable Long expeditionId,
            @RequestParam Long transporteurId) {
        return ResponseEntity.ok(livraisonService.assignerTransporteur(expeditionId, transporteurId));
    }
}