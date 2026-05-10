package com.example.pfe.prevision.scheduler;

import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
import com.example.pfe.prevision.service.CollecteurMetriquesService;
import com.example.pfe.prevision.service.PrevisionCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PrevisionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PrevisionScheduler.class);

    private final CollecteurMetriquesService collecteurMetriquesService;
    private final PrevisionCacheService previsionCacheService;
    private final WarehouseRepository warehouseRepository;

    @Value("${prevision.collecte.cron:0 0 * * * *}")
    private String collecteCron;

    @Value("${prevision.recalcul.cron:0 0 */6 * * *}")
    private String recalculCron;

    public PrevisionScheduler(CollecteurMetriquesService collecteurMetriquesService,
                              PrevisionCacheService previsionCacheService,
                              WarehouseRepository warehouseRepository) {
        this.collecteurMetriquesService = collecteurMetriquesService;
        this.previsionCacheService = previsionCacheService;
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * Tâche planifiée : Collecte des métriques pour la veille
     * Exécution : toutes les heures (configurable)
     */
    @Scheduled(cron = "${prevision.collecte.cron:0 0 * * * *}")
    public void collecterMetriquesQuotidiennes() {
        logger.info("🔄 EXÉCUTION SCHEDULER: Collecte des métriques quotidiennes");
        try {
            collecteurMetriquesService.collecterMetriquesPourTousEntrepots();
            logger.info("✅ Collecte des métriques terminée avec succès");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la collecte des métriques: {}", e.getMessage(), e);
        }
    }

    /**
     * Tâche planifiée : Recalcul des prévisions pour tous les entrepôts
     * Exécution : toutes les 6 heures (configurable)
     */
    @Scheduled(cron = "${prevision.recalcul.cron:0 0 */6 * * *}")
    public void recalculerPrevisions() {
        logger.info("🔄 EXÉCUTION SCHEDULER: Recalcul des prévisions pour tous les entrepôts");
        try {
            List<Warehouse> entrepots = warehouseRepository.findAll();

            for (Warehouse entrepot : entrepots) {
                logger.info("Recalcul des prévisions pour l'entrepôt: {}", entrepot.getNom());

                // Invalider le cache existant
                previsionCacheService.invalidateCache(entrepot.getId());

                // Recalculer et recacher (sera fait à la prochaine demande)
                // On force le calcul en appelant la méthode
                previsionCacheService.calculerEtStockerPrevisions(entrepot.getId());
            }

            logger.info("✅ Recalcul des prévisions terminé pour {} entrepôts", entrepots.size());
        } catch (Exception e) {
            logger.error("❌ Erreur lors du recalcul des prévisions: {}", e.getMessage(), e);
        }
    }

    /**
     * Tâche de rattrapage : Si des métriques manquent pour une date spécifique
     * Exécution : tous les jours à 01:00 (pour rattraper le jour précédent si nécessaire)
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void rattraperMetriquesManquantes() {
        logger.info("🔄 EXÉCUTION SCHEDULER: Rattrapage des métriques manquantes");
        try {
            LocalDate hier = LocalDate.now().minusDays(1);
            collecteurMetriquesService.collecterMetriquePourDateSpecifique(hier);
            logger.info("✅ Rattrapage des métriques terminé");
        } catch (Exception e) {
            logger.error("❌ Erreur lors du rattrapage des métriques: {}", e.getMessage(), e);
        }
    }
}