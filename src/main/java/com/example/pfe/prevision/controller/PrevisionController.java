package com.example.pfe.prevision.controller;

import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
import com.example.pfe.prevision.dto.PrevisionChargeDTO;
import com.example.pfe.prevision.service.PrevisionCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/previsions")
@CrossOrigin(origins = "http://localhost:5173")
public class PrevisionController {

    private static final Logger logger = LoggerFactory.getLogger(PrevisionController.class);

    private final PrevisionCacheService previsionCacheService;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;

    public PrevisionController(PrevisionCacheService previsionCacheService,
                               UserRepository userRepository,
                               WarehouseRepository warehouseRepository) {
        this.previsionCacheService = previsionCacheService;
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * Récupère les prévisions de charge pour l'entrepôt de l'utilisateur connecté
     * Si l'utilisateur est ADMIN, prend le premier entrepôt disponible
     */
    @GetMapping("/charge/7jours")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT')")
    public ResponseEntity<?> getPrevisionsCharge() {
        logger.info("=== APPEL API /charge/7jours ===");

        try {
            Long entrepotId = getCurrentUserEntrepotId();

            // Si l'utilisateur est ADMIN et n'a pas d'entrepôt, prendre le premier entrepôt
            if (entrepotId == null && isAdmin()) {
                List<Warehouse> entrepots = warehouseRepository.findAll();
                if (!entrepots.isEmpty()) {
                    entrepotId = entrepots.get(0).getId();
                    logger.info("ADMIN - Utilisation du premier entrepôt: ID {}", entrepotId);
                } else {
                    logger.warn("⚠️ Aucun entrepôt trouvé dans la base de données");
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Aucun entrepôt trouvé");
                    error.put("message", "Veuillez créer un entrepôt avant d'utiliser les prévisions");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            if (entrepotId == null) {
                logger.warn("⚠️ Utilisateur non lié à un entrepôt");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Utilisateur non lié à un entrepôt");
                error.put("message", "Veuillez contacter l'administrateur pour associer votre compte à un entrepôt");
                return ResponseEntity.badRequest().body(error);
            }

            PrevisionChargeDTO previsions = previsionCacheService.getPrevisionsWithCache(entrepotId);
            logger.info("✅ Prévisions récupérées avec succès");
            return ResponseEntity.ok(previsions);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des prévisions: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Récupère les prévisions pour un entrepôt spécifique (réservé ADMIN)
     */
    @GetMapping("/charge/7jours/entrepot/{entrepotId}")
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<PrevisionChargeDTO> getPrevisionsChargeByEntrepot(@PathVariable Long entrepotId) {
        logger.info("=== APPEL API /charge/7jours/entrepot/{} ===", entrepotId);
        PrevisionChargeDTO previsions = previsionCacheService.getPrevisionsWithCache(entrepotId);
        return ResponseEntity.ok(previsions);
    }

    /**
     * ENDPOINT DE TEST - Retourne des prévisions simulées
     */
    @GetMapping("/charge/simple")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT')")
    public ResponseEntity<Map<String, Object>> getSimplePrevisions() {
        logger.info("=== APPEL API /charge/simple ===");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "API des prévisions accessible");
        response.put("timestamp", LocalDateTime.now().toString());

        List<Map<String, Object>> previsions = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", LocalDate.now().plusDays(i).toString());
            day.put("chargePrevue", 100.0 + (i * 10));
            day.put("chargeMin", 80.0 + (i * 8));
            day.put("chargeMax", 120.0 + (i * 12));
            day.put("estPic", i == 3 || i == 5);
            previsions.add(day);
        }
        response.put("previsions", previsions);
        response.put("chargeMoyennePrevue", 150.0);
        response.put("chargeMaxPrevue", 220.0);
        response.put("alertePicProche", true);
        response.put("messageAlerte", "⚠️ Pic de charge prévu les jours 3 et 5");
        response.put("entrepotNom", "Entrepôt Principal");

        return ResponseEntity.ok(response);
    }

    /**
     * Vérifie si l'utilisateur connecté est ADMIN
     */
    private boolean isAdmin() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                String email = ((UserDetails) principal).getUsername();
                User user = userRepository.findByEmail(email).orElse(null);
                return user != null && "ADMINISTRATEUR".equals(user.getRole().name());
            }
        } catch (Exception e) {
            logger.error("Erreur vérification rôle admin: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Récupère l'ID de l'entrepôt de l'utilisateur connecté
     */
    private Long getCurrentUserEntrepotId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                String email = ((UserDetails) principal).getUsername();
                logger.info("Utilisateur connecté: {}", email);
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    logger.info("Utilisateur trouvé: {}, rôle: {}, entrepotId: {}",
                            user.getEmail(),
                            user.getRole().name(),
                            user.getEntrepot() != null ? user.getEntrepot().getId() : "null");
                    if (user.getEntrepot() != null) {
                        return user.getEntrepot().getId();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur récupération entrepôt utilisateur: {}", e.getMessage(), e);
        }
        return null;
    }
}