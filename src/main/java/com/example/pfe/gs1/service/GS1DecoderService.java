package com.example.pfe.gs1.service;

import com.example.pfe.gs1.dto.GS1DataDTO;
import org.springframework.stereotype.Service;

@Service
public class GS1DecoderService {

    public GS1DataDTO decodeGS1(String code) {
        GS1DataDTO result = new GS1DataDTO();

        // Nettoyer le code (enlever les parenthèses, espaces, etc.)
        String cleanCode = code.replaceAll("[()\\s]", "");
        System.out.println("🔍 Décodage GS1 - code nettoyé : " + cleanCode);

        // Détecter le format
        if (cleanCode.length() == 13) {
            result.setGtin(cleanCode);
            result.setFormat("EAN-13");
            return result;
        } else if (cleanCode.length() == 14) {
            result.setGtin(cleanCode);
            result.setFormat("GTIN-14");
            return result;
        } else {
            result.setFormat("GS1-128");

            // Extraire GTIN (AI 01) - 14 chiffres
            if (cleanCode.startsWith("01") && cleanCode.length() >= 16) {
                result.setGtin(cleanCode.substring(2, 16));
            }

            // Extraire LOT (AI 10) – ignorer les occurrences de "10" à l'intérieur du lot
            int lotIndex = -1;
            int gtinEnd = (result.getGtin() != null) ? 2 + result.getGtin().length() : 0;
            for (int i = gtinEnd; i < cleanCode.length() - 1; i++) {
                if (cleanCode.startsWith("10", i)) {
                    lotIndex = i;
                    break;
                }
            }

            if (lotIndex >= 0) {
                // Chercher le prochain AI valide, en ignorant le "10" lui‑même
                int lotEnd = findNextAI(cleanCode, lotIndex + 2, "10");
                String lotValue = cleanCode.substring(lotIndex + 2, lotEnd);
                if (lotValue != null && !lotValue.isEmpty() && !lotValue.equals(result.getGtin())) {
                    result.setLot(lotValue);
                    System.out.println("📦 Lot extrait : " + lotValue);
                }
            }

            // Extraire date expiration (AI 17)
            int expIndex = cleanCode.indexOf("17");
            if (expIndex >= 0) {
                try {
                    String expDate = cleanCode.substring(expIndex + 2, expIndex + 8);
                    String year = "20" + expDate.substring(0, 2);
                    String month = expDate.substring(2, 4);
                    String day = expDate.substring(4, 6);
                    result.setDateExpiration(String.format("%s-%s-%s", year, month, day));
                } catch (Exception e) {
                    System.err.println("Erreur extraction date expiration : " + e.getMessage());
                }
            }

            // Extraire quantité (AI 30)
            int qtyIndex = cleanCode.indexOf("30");
            if (qtyIndex >= 0) {
                try {
                    int qtyEnd = findNextAI(cleanCode, qtyIndex + 2, "30");
                    result.setQuantite(Integer.parseInt(cleanCode.substring(qtyIndex + 2, qtyEnd)));
                } catch (NumberFormatException e) {
                    System.err.println("Erreur extraction quantité : " + e.getMessage());
                }
            }
        }

        return result;
    }

    /**
     * Trouve le prochain AI valide à partir de start, en ignorant un AI spécifique (skipAI).
     * Si aucun n'est trouvé, retourne la longueur de la chaîne.
     */
    private int findNextAI(String code, int start, String skipAI) {
        for (int i = start; i < code.length() - 1; i++) {
            String possibleAI = code.substring(i, i + 2);
            if (possibleAI.equals(skipAI)) {
                continue; // ignorer l'AI que l'on est en train de traiter
            }
            // Vérifier si c'est un AI valide
            if (possibleAI.equals("01") || possibleAI.equals("10") || possibleAI.equals("11") ||
                    possibleAI.equals("17") || possibleAI.equals("21") || possibleAI.equals("30")) {
                return i;
            }
        }
        return code.length();
    }
}