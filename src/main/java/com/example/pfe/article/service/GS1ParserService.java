package com.example.pfe.article.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service de parsing des codes-barres GS1
 * Permet d'extraire les différentes informations (GTIN, lot, date expiration, etc.)
 */
@Service
public class GS1ParserService {

    /**
     * Parse un code GS1 et retourne un objet contenant toutes les données extraites
     * @param gs1Code Le code GS1 scanné
     * @return GS1Data contenant les informations extraites
     */
    public GS1Data parseGS1Code(String gs1Code) {
        GS1Data data = new GS1Data();

        if (gs1Code == null || gs1Code.isEmpty()) {
            return data;
        }

        // Nettoyer le code des caractères spéciaux (FNC1, séparateurs)
        String cleanCode = gs1Code
                .replace("\\", "")
                .replace("\u001D", "")  // Caractère FNC1
                .replace("(", "")
                .replace(")", "");

        // Extraire toutes les paires AI:valeur
        Pattern pattern = Pattern.compile("(\\d{2,4})([A-Za-z0-9]+)");
        Matcher matcher = pattern.matcher(cleanCode);

        while (matcher.find()) {
            String ai = matcher.group(1);
            String value = matcher.group(2);

            switch(ai) {
                case "01":  // GTIN
                    data.setGtin(value);
                    break;
                case "02":  // GTIN du contenu
                    data.setGtinContenu(value);
                    break;
                case "10":  // Lot
                    data.setLot(value);
                    break;
                case "11":  // Date de production
                    data.setDateProduction(parseGS1Date(value));
                    break;
                case "15":  // Date de péremption
                    data.setDatePereption(parseGS1Date(value));
                    break;
                case "17":  // Date d'expiration
                    data.setDateExpiration(parseGS1Date(value));
                    break;
                case "21":  // Numéro de série
                    data.setNumeroSerie(value);
                    break;
                case "30":  // Quantité
                    data.setQuantite(Integer.parseInt(value));
                    break;
                case "3103": // Poids net
                    data.setPoids(Double.parseDouble(value) / 1000); // En kg
                    break;
                case "37":  // Quantité dans l'unité logistique
                    data.setQuantiteUniteLogistique(Integer.parseInt(value));
                    break;
                case "410": // Livrer à (GLN)
                case "411": // Facturer à (GLN)
                case "412": // Acheter à (GLN)
                    // Ne pas stocker, juste logger
                    System.out.println("GLN trouvé: " + ai + " = " + value);
                    break;
                default:
                    System.out.println("AI non géré: " + ai + " = " + value);
            }
        }

        return data;
    }

    /**
     * Parse une date au format GS1 (YYMMDD)
     */
    private LocalDate parseGS1Date(String dateStr) {
        if (dateStr == null || dateStr.length() != 6) {
            return null;
        }
        try {
            int year = 2000 + Integer.parseInt(dateStr.substring(0, 2));
            int month = Integer.parseInt(dateStr.substring(2, 4));
            int day = Integer.parseInt(dateStr.substring(4, 6));
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            System.err.println("Erreur parsing date GS1: " + dateStr);
            return null;
        }
    }

    /**
     * Classe interne pour stocker toutes les données extraites d'un code GS1
     */
    public static class GS1Data {
        private String gtin;           // AI 01
        private String gtinContenu;     // AI 02
        private String lot;             // AI 10
        private LocalDate dateProduction;   // AI 11
        private LocalDate datePereption;    // AI 15
        private LocalDate dateExpiration;   // AI 17
        private String numeroSerie;     // AI 21
        private Integer quantite;       // AI 30
        private Double poids;           // AI 3103
        private Integer quantiteUniteLogistique; // AI 37

        // Constructeur
        public GS1Data() {}

        // Getters et Setters
        public String getGtin() { return gtin; }
        public void setGtin(String gtin) { this.gtin = gtin; }

        public String getGtinContenu() { return gtinContenu; }
        public void setGtinContenu(String gtinContenu) { this.gtinContenu = gtinContenu; }

        public String getLot() { return lot; }
        public void setLot(String lot) { this.lot = lot; }

        public LocalDate getDateProduction() { return dateProduction; }
        public void setDateProduction(LocalDate dateProduction) { this.dateProduction = dateProduction; }

        public LocalDate getDatePereption() { return datePereption; }
        public void setDatePereption(LocalDate datePereption) { this.datePereption = datePereption; }

        public LocalDate getDateExpiration() { return dateExpiration; }
        public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }

        public String getNumeroSerie() { return numeroSerie; }
        public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

        public Integer getQuantite() { return quantite; }
        public void setQuantite(Integer quantite) { this.quantite = quantite; }

        public Double getPoids() { return poids; }
        public void setPoids(Double poids) { this.poids = poids; }

        public Integer getQuantiteUniteLogistique() { return quantiteUniteLogistique; }
        public void setQuantiteUniteLogistique(Integer quantiteUniteLogistique) { this.quantiteUniteLogistique = quantiteUniteLogistique; }

        @Override
        public String toString() {
            return "GS1Data{" +
                    "gtin='" + gtin + '\'' +
                    ", lot='" + lot + '\'' +
                    ", dateExpiration=" + dateExpiration +
                    ", quantite=" + quantite +
                    '}';
        }
    }
}