package com.example.pfe.prevision.service;

import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
import com.example.pfe.prevision.dto.PrevisionChargeDTO;
import com.example.pfe.prevision.dto.PrevisionQuotidienneDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrevisionCacheService {

    private static final Logger logger = LoggerFactory.getLogger(PrevisionCacheService.class);

    private final PrevisionModelService previsionModelService;
    private final WarehouseRepository warehouseRepository;

    public PrevisionCacheService(PrevisionModelService previsionModelService,
                                 WarehouseRepository warehouseRepository) {
        this.previsionModelService = previsionModelService;
        this.warehouseRepository = warehouseRepository;
    }

    // LIGNE À MODIFIER (vers ligne 35 environ)
    @Cacheable(value = "previsions", key = "#entrepotId", condition = "false")
    public PrevisionChargeDTO getPrevisionsWithCache(Long entrepotId) {
        logger.info("🔄 Cache MISS - Calcul des prévisions pour l'entrepôt ID: {}", entrepotId);
        PrevisionChargeDTO result = calculerEtStockerPrevisions(entrepotId);

        if (result == null || result.getPrevisions() == null || result.getPrevisions().isEmpty()) {
            logger.warn("⚠️ Prévisions vides pour l'entrepôt {}, retour d'un objet par défaut", entrepotId);
            return getPrevisionsParDefaut(entrepotId);
        }
        return result;
    }

    public PrevisionChargeDTO calculerEtStockerPrevisions(Long entrepotId) {
        try {
            Warehouse entrepot = warehouseRepository.findById(entrepotId).orElse(null);
            if (entrepot == null) {
                logger.error("❌ Entrepôt non trouvé: {}", entrepotId);
                return getPrevisionsParDefaut(entrepotId);
            }

            List<PrevisionQuotidienneDTO> previsions = previsionModelService.calculerPrevisions(entrepotId);
            if (previsions == null) {
                previsions = new ArrayList<>();
            }

            PrevisionChargeDTO result = new PrevisionChargeDTO(entrepotId, entrepot.getNom(), previsions);

            // 🔧 MODIFICATION : conversion en String
            result.setDateDebutPrevision(LocalDate.now().atStartOfDay().toString());
            result.setDateFinPrevision(LocalDate.now().plusDays(7).atStartOfDay().toString());
            result.setDateCalcul(LocalDateTime.now().toString());

            double somme = 0;
            if (previsions != null && !previsions.isEmpty()) {
                somme = previsions.stream()
                        .mapToDouble(p -> p.getChargePrevue() != null ? p.getChargePrevue() : 0)
                        .sum();

                double moyenne = somme / previsions.size();
                result.setChargeMoyennePrevue(moyenne);

                double max = previsions.stream()
                        .mapToDouble(p -> p.getChargePrevue() != null ? p.getChargePrevue() : 0)
                        .max()
                        .orElse(100.0);
                result.setChargeMaxPrevue(max);

                previsions.stream()
                        .filter(p -> p.getEstPic() != null && p.getEstPic())
                        .max((p1, p2) -> {
                            double v1 = p1.getChargePrevue() != null ? p1.getChargePrevue() : 0;
                            double v2 = p2.getChargePrevue() != null ? p2.getChargePrevue() : 0;
                            return Double.compare(v1, v2);
                        })
                        .ifPresent(picMax -> {
                            // 🔧 MODIFICATION : picMax.getDate() est déjà un String
                            result.setDatePicMax(picMax.getDate());
                            result.setAlertePicProche(true);
                            result.setMessageAlerte(String.format("⚠️ Pic de charge prévu le %s avec %.0f unités de charge",
                                    picMax.getDate(), picMax.getChargePrevue()));
                        });
            } else {
                result.setChargeMoyennePrevue(100.0);
                result.setChargeMaxPrevue(150.0);
                result.setAlertePicProche(false);
                result.setMessageAlerte(null);
                result.setPrevisions(new ArrayList<>());
                logger.warn("⚠️ Aucune prévision générée pour l'entrepôt {}", entrepotId);
            }

            logger.info("✅ Prévisions calculées et prêtes pour l'entrepôt {} (moyenne: {:.2f}, max: {:.2f})",
                    entrepot.getNom(), result.getChargeMoyennePrevue(), result.getChargeMaxPrevue());

            return result;

        } catch (Exception e) {
            logger.error("❌ Erreur lors du calcul des prévisions pour l'entrepôt {}: {}", entrepotId, e.getMessage(), e);
            return getPrevisionsParDefaut(entrepotId);
        }
    }

    private PrevisionChargeDTO getPrevisionsParDefaut(Long entrepotId) {
        PrevisionChargeDTO dto = new PrevisionChargeDTO();
        dto.setEntrepotId(entrepotId);

        try {
            Warehouse entrepot = warehouseRepository.findById(entrepotId).orElse(null);
            dto.setEntrepotNom(entrepot != null ? entrepot.getNom() : "Entrepôt " + entrepotId);
        } catch (Exception e) {
            dto.setEntrepotNom("Entrepôt " + entrepotId);
        }

        dto.setChargeMoyennePrevue(100.0);
        dto.setChargeMaxPrevue(150.0);
        dto.setAlertePicProche(false);
        dto.setDateCalcul(LocalDateTime.now().toString());
        dto.setDateDebutPrevision(LocalDate.now().atStartOfDay().toString());
        dto.setDateFinPrevision(LocalDate.now().plusDays(7).atStartOfDay().toString());
        dto.setPrevisions(new ArrayList<>());

        logger.info("📊 Prévisions par défaut retournées pour l'entrepôt {}", entrepotId);
        return dto;
    }

    @CacheEvict(value = "previsions", key = "#entrepotId")
    public void invalidateCache(Long entrepotId) {
        logger.info("🗑️ Cache invalidé pour l'entrepôt ID: {}", entrepotId);
    }

    @CacheEvict(value = "previsions", allEntries = true)
    public void invalidateAllCache() {
        logger.info("🗑️ Tout le cache des prévisions a été invalidé");
    }
}