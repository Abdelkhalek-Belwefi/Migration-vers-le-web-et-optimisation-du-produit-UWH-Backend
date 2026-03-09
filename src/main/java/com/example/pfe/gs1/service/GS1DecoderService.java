package com.example.pfe.gs1.service;

import com.example.pfe.gs1.dto.GS1DataDTO;
import org.springframework.stereotype.Service;

@Service
public class GS1DecoderService {  // ← Nom modifié

    public GS1DataDTO decodeGS1(String code) {
        GS1DataDTO result = new GS1DataDTO();

        // Nettoyer le code
        String cleanCode = code.replaceAll("[()\\s]", "");

        // Détecter le format
        if (cleanCode.length() == 13) {
            result.setGtin(cleanCode);
            result.setFormat("EAN-13");
        } else if (cleanCode.length() == 14) {
            result.setGtin(cleanCode);
            result.setFormat("GTIN-14");
        } else {
            result.setFormat("GS1-128");

            // Extraire GTIN (AI 01)
            if (cleanCode.startsWith("01") && cleanCode.length() >= 16) {
                result.setGtin(cleanCode.substring(2, 16));
            }

            // Extraire LOT (AI 10)
            int lotIndex = cleanCode.indexOf("10");
            if (lotIndex >= 0) {
                int lotEnd = findNextAI(cleanCode, lotIndex + 2);
                result.setLot(cleanCode.substring(lotIndex + 2, lotEnd));
            }

            // Extraire date expiration (AI 17)
            int expIndex = cleanCode.indexOf("17");
            if (expIndex >= 0) {
                String expDate = cleanCode.substring(expIndex + 2, expIndex + 8);
                String year = "20" + expDate.substring(0, 2);
                String month = expDate.substring(2, 4);
                String day = expDate.substring(4, 6);
                result.setDateExpiration(String.format("%s-%s-%s", year, month, day));
            }

            // Extraire quantité (AI 30)
            int qtyIndex = cleanCode.indexOf("30");
            if (qtyIndex >= 0) {
                int qtyEnd = findNextAI(cleanCode, qtyIndex + 2);
                result.setQuantite(Integer.parseInt(cleanCode.substring(qtyIndex + 2, qtyEnd)));
            }
        }

        return result;
    }

    private int findNextAI(String code, int start) {
        for (int i = start; i < code.length() - 1; i++) {
            if (Character.isDigit(code.charAt(i)) && Character.isDigit(code.charAt(i + 1))) {
                String possibleAI = code.substring(i, i + 2);
                if (possibleAI.equals("10") || possibleAI.equals("11") ||
                        possibleAI.equals("17") || possibleAI.equals("30") ||
                        possibleAI.equals("21") || possibleAI.equals("01")) {
                    return i;
                }
            }
        }
        return code.length();
    }
}