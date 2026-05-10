package com.example.pfe.prevision.service;

import com.example.pfe.commande.repository.CommandeRepository;
import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
import com.example.pfe.expedition.repository.ExpeditionRepository;
import com.example.pfe.prevision.entity.MetriqueQuotidienne;
import com.example.pfe.prevision.repository.MetriqueQuotidienneRepository;
import com.example.pfe.reception.repository.ReceptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class CollecteurMetriquesService {

    private static final Logger logger = LoggerFactory.getLogger(CollecteurMetriquesService.class);

    private final MetriqueQuotidienneRepository metriqueRepository;
    private final WarehouseRepository warehouseRepository;
    private final CommandeRepository commandeRepository;
    private final ReceptionRepository receptionRepository;
    private final ExpeditionRepository expeditionRepository;

    public CollecteurMetriquesService(MetriqueQuotidienneRepository metriqueRepository,
                                      WarehouseRepository warehouseRepository,
                                      CommandeRepository commandeRepository,
                                      ReceptionRepository receptionRepository,
                                      ExpeditionRepository expeditionRepository) {
        this.metriqueRepository = metriqueRepository;
        this.warehouseRepository = warehouseRepository;
        this.commandeRepository = commandeRepository;
        this.receptionRepository = receptionRepository;
        this.expeditionRepository = expeditionRepository;
    }

    @Transactional
    public void collecterMetriquesPourTousEntrepots() {
        logger.info("=== DÉBUT COLLECTE DES MÉTRIQUES QUOTIDIENNES ===");

        List<Warehouse> entrepots = warehouseRepository.findAll();
        LocalDate hier = LocalDate.now().minusDays(1);
        LocalDateTime debutJournee = hier.atStartOfDay();
        LocalDateTime finJournee = hier.atTime(LocalTime.MAX);

        for (Warehouse entrepot : entrepots) {
            collecterMetriquesPourEntrepot(entrepot, hier, debutJournee, finJournee);
        }

        logger.info("=== FIN COLLECTE DES MÉTRIQUES QUOTIDIENNES ===");
    }

    private void collecterMetriquesPourEntrepot(Warehouse entrepot, LocalDate date,
                                                LocalDateTime debut, LocalDateTime fin) {
        try {
            // Vérifier si les métriques existent déjà pour cette date
            if (metriqueRepository.existsByEntrepotIdAndDateMetrique(entrepot.getId(), date)) {
                logger.info("Métriques déjà existantes pour l'entrepôt {} à la date {}", entrepot.getNom(), date);
                return;
            }

            MetriqueQuotidienne metrique = new MetriqueQuotidienne(entrepot, date);

            // Collecte des données
            int nbCommandes = commandeRepository.findByEntrepotId(entrepot.getId()).size();
            int nbReceptions = receptionRepository.findByEntrepotId(entrepot.getId()).size();
            int nbExpeditions = expeditionRepository.findByEntrepotId(entrepot.getId()).size();

            metrique.setNbCommandes(nbCommandes);
            metrique.setNbReceptions(nbReceptions);
            metrique.setNbExpeditions(nbExpeditions);

            // Calcul automatique de la charge de travail
            metrique.calculerChargeTravail();

            metriqueRepository.save(metrique);

            logger.info("✅ Métriques collectées pour l'entrepôt {} : Commandes={}, Réceptions={}, Expéditions={}, Charge={}",
                    entrepot.getNom(), nbCommandes, nbReceptions, nbExpeditions, metrique.getChargeTravail());

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la collecte des métriques pour l'entrepôt {}: {}",
                    entrepot.getNom(), e.getMessage(), e);
        }
    }

    @Transactional
    public void collecterMetriquePourDateSpecifique(LocalDate date) {
        logger.info("Collecte des métriques pour la date spécifique: {}", date);

        List<Warehouse> entrepots = warehouseRepository.findAll();
        LocalDateTime debutJournee = date.atStartOfDay();
        LocalDateTime finJournee = date.atTime(LocalTime.MAX);

        for (Warehouse entrepot : entrepots) {
            collecterMetriquesPourEntrepot(entrepot, date, debutJournee, finJournee);
        }
    }
}