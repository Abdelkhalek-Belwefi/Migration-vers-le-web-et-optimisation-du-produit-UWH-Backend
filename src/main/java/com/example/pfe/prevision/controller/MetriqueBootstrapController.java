package com.example.pfe.prevision.controller;

import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
import com.example.pfe.prevision.entity.MetriqueQuotidienne;
import com.example.pfe.prevision.repository.MetriqueQuotidienneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/previsions/bootstrap")
@CrossOrigin(origins = "http://localhost:5173")
public class MetriqueBootstrapController {

    private static final Logger logger = LoggerFactory.getLogger(MetriqueBootstrapController.class);

    private final MetriqueQuotidienneRepository metriqueRepository;
    private final WarehouseRepository warehouseRepository;

    public MetriqueBootstrapController(MetriqueQuotidienneRepository metriqueRepository,
                                       WarehouseRepository warehouseRepository) {
        this.metriqueRepository = metriqueRepository;
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * Initialise les métriques historiques pour tous les entrepôts
     * Génère des données réalistes pour les 90 derniers jours
     */
    @PostMapping("/init")
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<Map<String, Object>> initialiserMetriques() {
        logger.info("=== DÉBUT INITIALISATION DES MÉTRIQUES ===");

        List<Warehouse> entrepots = warehouseRepository.findAll();

        if (entrepots.isEmpty()) {
            logger.warn("⚠️ Aucun entrepôt trouvé dans la base de données");
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Aucun entrepôt trouvé");
            response.put("status", "WARNING");
            return ResponseEntity.ok(response);
        }

        Random random = new Random();
        int totalMetriquesCrees = 0;
        int totalMetriquesExistantes = 0;

        for (Warehouse entrepot : entrepots) {
            int metriquesCrees = 0;
            int metriquesExistantes = 0;

            // Générer des données pour les 90 derniers jours
            for (int i = 90; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);

                // Vérifier si les données existent déjà
                if (metriqueRepository.findByEntrepotIdAndDateMetrique(entrepot.getId(), date).isPresent()) {
                    metriquesExistantes++;
                    continue;
                }

                MetriqueQuotidienne metrique = new MetriqueQuotidienne(entrepot, date);

                // Générer des données réalistes
                int jourSemaine = date.getDayOfWeek().getValue();
                double base = 50 + random.nextInt(150);

                // Variation selon le jour de semaine
                if (jourSemaine == 1) { // Lundi
                    base = base * 1.3;
                } else if (jourSemaine == 2 || jourSemaine == 3 || jourSemaine == 4) { // Mardi, Mercredi, Jeudi
                    base = base * 1.1;
                } else if (jourSemaine == 5) { // Vendredi
                    base = base * 1.2;
                } else if (jourSemaine == 6) { // Samedi
                    base = base * 0.6;
                } else if (jourSemaine == 7) { // Dimanche
                    base = base * 0.3;
                }

                metrique.setNbCommandes((int) (base / 3) + random.nextInt(20));
                metrique.setNbReceptions((int) (base / 4) + random.nextInt(15));
                metrique.setNbExpeditions((int) (base / 5) + random.nextInt(10));
                metrique.setNbConnexions((int) (base / 2) + random.nextInt(30));
                metrique.setNbRequetesApi((int) base + random.nextInt(50));
                metrique.calculerChargeTravail();

                metriqueRepository.save(metrique);
                metriquesCrees++;
            }

            totalMetriquesCrees += metriquesCrees;
            totalMetriquesExistantes += metriquesExistantes;

            logger.info("Entrepôt '{}' : {} métriques créées, {} déjà existantes",
                    entrepot.getNom(), metriquesCrees, metriquesExistantes);
        }

        logger.info("=== INITIALISATION TERMINÉE ===");
        logger.info("Total: {} métriques créées, {} déjà existantes", totalMetriquesCrees, totalMetriquesExistantes);

        Map<String, Object> response = new HashMap<>();
        response.put("message", String.format("Initialisation terminée : %d metriques créées, %d déjà existantes",
                totalMetriquesCrees, totalMetriquesExistantes));
        response.put("status", "OK");
        response.put("metriquesCrees", totalMetriquesCrees);
        response.put("metriquesExistantes", totalMetriquesExistantes);
        response.put("nombreEntrepots", entrepots.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Vérifie l'état des métriques dans la base de données
     */
    @GetMapping("/status")
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<Map<String, Object>> getStatusMetriques() {
        List<Warehouse> entrepots = warehouseRepository.findAll();
        long totalMetriques = metriqueRepository.count();

        Map<String, Object> response = new HashMap<>();
        response.put("totalMetriques", totalMetriques);
        response.put("nombreEntrepots", entrepots.size());
        response.put("entrepots", entrepots.stream()
                .map(w -> {
                    LocalDate debut = LocalDate.now().minusDays(90);
                    List<MetriqueQuotidienne> metriques = metriqueRepository.getHistoriqueDepuis(w.getId(), debut);
                    return Map.of(
                            "id", w.getId(),
                            "nom", w.getNom(),
                            "nombreMetriques", metriques != null ? metriques.size() : 0
                    );
                })
                .toList());

        return ResponseEntity.ok(response);
    }
}