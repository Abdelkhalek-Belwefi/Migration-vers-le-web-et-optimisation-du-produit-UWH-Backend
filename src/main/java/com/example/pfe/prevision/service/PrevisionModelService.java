package com.example.pfe.prevision.service;

import com.example.pfe.prevision.dto.PrevisionQuotidienneDTO;
import com.example.pfe.prevision.entity.MetriqueQuotidienne;
import com.example.pfe.prevision.repository.MetriqueQuotidienneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrevisionModelService {

    private static final Logger logger = LoggerFactory.getLogger(PrevisionModelService.class);

    private final MetriqueQuotidienneRepository metriqueRepository;

    @Value("${prevision.historique.jours:90}")
    private int historiqueJours;

    @Value("${prevision.horizon.jours:7}")
    private int horizonJours;

    @Value("${prevision.seuil.pic:50}")
    private double seuilPic;

    public PrevisionModelService(MetriqueQuotidienneRepository metriqueRepository) {
        this.metriqueRepository = metriqueRepository;
    }

    /**
     * Calcule les prévisions de charge pour un entrepôt sur les 7 prochains jours
     * Utilise un modèle de lissage exponentiel simple (Holt-Winters simplifié)
     */
    public List<PrevisionQuotidienneDTO> calculerPrevisions(Long entrepotId) {
        logger.info("=== CALCUL DES PRÉVISIONS POUR ENTREPÔT ID: {} ===", entrepotId);

        LocalDate dateFin = LocalDate.now().minusDays(1);
        LocalDate dateDebut = dateFin.minusDays(historiqueJours);

        List<MetriqueQuotidienne> historique = metriqueRepository.getHistoriqueDepuis(entrepotId, dateDebut);

        if (historique == null || historique.size() < 5) {
            logger.warn("Historique insuffisant ou nul pour l'entrepôt {}: {} jours (minimum 5 requis)",
                    entrepotId, historique == null ? 0 : historique.size());
            return genererPrevisionsParDefaut();
        }

        List<Double> valeursList = new ArrayList<>();
        for (MetriqueQuotidienne m : historique) {
            Double charge = m.getChargeTravail();
            if (charge != null && !charge.isNaN() && !charge.isInfinite() && charge > 0) {
                valeursList.add(charge);
            }
        }

        if (valeursList.size() < 5) {
            logger.warn("Pas assez de valeurs valides pour l'entrepôt {}: {} valeurs", entrepotId, valeursList.size());
            return genererPrevisionsParDefaut();
        }

        double[] valeurs = valeursList.stream().mapToDouble(Double::doubleValue).toArray();

        double moyenne = calculerMoyenne(valeurs);
        if (Double.isNaN(moyenne) || moyenne <= 0) {
            logger.warn("Moyenne invalide pour l'entrepôt {}, utilisation valeur par défaut", entrepotId);
            return genererPrevisionsParDefaut();
        }

        double ecartType = calculerEcartType(valeurs, moyenne);
        if (Double.isNaN(ecartType) || ecartType <= 0) {
            ecartType = moyenne * 0.2;
        }

        double tendance = calculerTendance(valeurs);
        double[] saisonnalite = calculerSaisonnalite(valeurs);

        List<PrevisionQuotidienneDTO> previsions = new ArrayList<>();
        double derniereValeur = valeurs[valeurs.length - 1];

        for (int i = 1; i <= horizonJours; i++) {
            LocalDate datePrevue = LocalDate.now().plusDays(i);
            int jourSemaine = datePrevue.getDayOfWeek().getValue();
            int indexSaison = (jourSemaine % 7);

            double facteurSaisonnier = (indexSaison >= 0 && indexSaison < saisonnalite.length && saisonnalite[indexSaison] > 0)
                    ? saisonnalite[indexSaison] : 1.0;

            double valeurTendance = tendance * i;
            double chargePrevue = (derniereValeur * facteurSaisonnier) + valeurTendance;
            double variation = 0.95 + (Math.random() * 0.1);
            chargePrevue = chargePrevue * variation;
            chargePrevue = Math.max(10, chargePrevue);

            double chargeMin = Math.max(5, chargePrevue - ecartType);
            double chargeMax = chargePrevue + ecartType;

            // 🔧 MODIFICATION : datePrevue.toString() au lieu de datePrevue
            PrevisionQuotidienneDTO prevision = new PrevisionQuotidienneDTO(
                    datePrevue.toString(), chargePrevue, chargeMin, chargeMax
            );

            boolean estPic = detecterPic(chargePrevue, moyenne, ecartType);
            prevision.setEstPic(estPic);
            prevision.setEcartType(ecartType);

            if (estPic) {
                prevision.setCommentaire("⚠️ Pic de charge prévu !");
            }

            previsions.add(prevision);
            logger.debug("Prévision J+{}: {} (min: {}, max: {}, pic: {})",
                    i, Math.round(chargePrevue), Math.round(chargeMin), Math.round(chargeMax), estPic);
        }

        logger.info("✅ Prévisions calculées pour {} jours", previsions.size());
        return previsions;
    }

    private double calculerMoyenne(double[] valeurs) {
        if (valeurs == null || valeurs.length == 0) return 100.0;
        double somme = 0;
        int compteur = 0;
        for (double v : valeurs) {
            if (!Double.isNaN(v) && !Double.isInfinite(v)) {
                somme += v;
                compteur++;
            }
        }
        return compteur > 0 ? somme / compteur : 100.0;
    }

    private double calculerEcartType(double[] valeurs, double moyenne) {
        if (valeurs == null || valeurs.length == 0 || Double.isNaN(moyenne)) return 20.0;
        double sommeCarres = 0;
        int compteur = 0;
        for (double v : valeurs) {
            if (!Double.isNaN(v) && !Double.isInfinite(v)) {
                sommeCarres += Math.pow(v - moyenne, 2);
                compteur++;
            }
        }
        double variance = compteur > 0 ? sommeCarres / compteur : 0;
        double ecart = Math.sqrt(variance);
        return (Double.isNaN(ecart) || ecart <= 0) ? moyenne * 0.2 : ecart;
    }

    private double calculerTendance(double[] valeurs) {
        if (valeurs == null || valeurs.length < 2) return 0;
        int n = Math.min(30, valeurs.length);
        if (n < 2) return 0;

        double sommeX = 0, sommeY = 0, sommeXY = 0, sommeX2 = 0;
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = valeurs[valeurs.length - n + i];
            if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                sommeX += x;
                sommeY += y;
                sommeXY += x * y;
                sommeX2 += x * x;
            }
        }
        double denominateur = n * sommeX2 - sommeX * sommeX;
        if (denominateur == 0) return 0;
        double pente = (n * sommeXY - sommeX * sommeY) / denominateur;
        return Double.isNaN(pente) ? 0 : Math.max(-20, Math.min(20, pente));
    }

    private double[] calculerSaisonnalite(double[] valeurs) {
        double[] facteurs = new double[7];
        int[] compteurs = new int[7];
        int debut = Math.max(0, valeurs.length - 60);

        for (int i = debut; i < valeurs.length; i++) {
            int position = i % 7;
            double valeur = valeurs[i];
            if (!Double.isNaN(valeur) && !Double.isInfinite(valeur)) {
                facteurs[position] += valeur;
                compteurs[position]++;
            }
        }

        for (int i = 0; i < 7; i++) {
            if (compteurs[i] > 0 && facteurs[i] > 0) {
                facteurs[i] = facteurs[i] / compteurs[i];
            } else {
                facteurs[i] = 1.0;
            }
        }

        double moyenneFacteurs = 0;
        int compteurFacteurs = 0;
        for (int i = 0; i < 7; i++) {
            if (facteurs[i] > 0 && !Double.isNaN(facteurs[i])) {
                moyenneFacteurs += facteurs[i];
                compteurFacteurs++;
            }
        }
        moyenneFacteurs = compteurFacteurs > 0 ? moyenneFacteurs / compteurFacteurs : 1.0;

        for (int i = 0; i < 7; i++) {
            if (moyenneFacteurs > 0) {
                facteurs[i] = facteurs[i] / moyenneFacteurs;
            } else {
                facteurs[i] = 1.0;
            }
            facteurs[i] = Math.max(0.5, Math.min(2.0, facteurs[i]));
        }
        return facteurs;
    }

    private boolean detecterPic(double valeur, double moyenne, double ecartType) {
        if (Double.isNaN(valeur) || Double.isNaN(moyenne) || moyenne <= 0) return false;
        double seuil = 1 + (seuilPic / 100.0);
        double limite = moyenne + (Math.abs(ecartType) * seuil);
        return valeur > limite;
    }

    private List<PrevisionQuotidienneDTO> genererPrevisionsParDefaut() {
        List<PrevisionQuotidienneDTO> previsions = new ArrayList<>();
        double valeurParDefaut = 100.0;

        for (int i = 1; i <= horizonJours; i++) {
            LocalDate datePrevue = LocalDate.now().plusDays(i);
            // 🔧 MODIFICATION : datePrevue.toString()
            PrevisionQuotidienneDTO prevision = new PrevisionQuotidienneDTO(
                    datePrevue.toString(), valeurParDefaut, valeurParDefaut * 0.8, valeurParDefaut * 1.2
            );
            prevision.setCommentaire("Historique insuffisant - prévision basée sur valeur par défaut");
            prevision.setEcartType(20.0);
            prevision.setEstPic(false);
            previsions.add(prevision);
        }
        logger.info("✅ Génération des prévisions par défaut (historique insuffisant)");
        return previsions;
    }
}