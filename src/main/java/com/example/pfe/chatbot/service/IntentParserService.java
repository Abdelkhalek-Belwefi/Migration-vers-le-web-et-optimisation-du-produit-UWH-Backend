package com.example.pfe.chatbot.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IntentParserService {

    // Mapping des mots-clés par intention
    private static final Map<String, List<String>> INTENT_KEYWORDS = new HashMap<>();

    static {
        // Intentions pour les STOCKS
        INTENT_KEYWORDS.put("STOCK_FAIBLE", Arrays.asList(
                "stock faible", "stock bas", "rupture", "réapprovisionner", "article manquant",
                "stocks critiques", "alerte stock"
        ));

        INTENT_KEYWORDS.put("STOCK_ARTICLE", Arrays.asList(
                "stock de", "quantité de", "combien de", "disponibilité"
        ));

        // Intentions pour les COMMANDES
        INTENT_KEYWORDS.put("COMMANDE_STATUT", Arrays.asList(
                "statut commande", "commande est", "où est ma commande", "état commande",
                "commande prête", "commande livrée"
        ));

        INTENT_KEYWORDS.put("COMMANDE_A_EXPEDIER", Arrays.asList(
                "commandes à expédier", "à livrer", "en attente d'expédition", "prêtes à partir"
        ));

        // Intentions pour les TRANSFERTS
        INTENT_KEYWORDS.put("TRANSFERT_DEMANDES", Arrays.asList(
                "demandes de transfert", "transfert reçu", "demandes reçues"
        ));

        // Intentions pour les RÉCEPTIONS
        INTENT_KEYWORDS.put("RECEPTION_ATTENTE", Arrays.asList(
                "réceptions en attente", "à valider", "en attente de validation"
        ));

        // Intentions pour les LIVRAISONS
        INTENT_KEYWORDS.put("LIVRAISON_JOUR", Arrays.asList(
                "livraisons aujourd'hui", "livraisons du jour", "tournée"
        ));

        // Intentions pour les RANGEMENTS
        INTENT_KEYWORDS.put("RANGEMENT_TACHES", Arrays.asList(
                "tâches de rangement", "à ranger", "putaway"
        ));

        // Intentions générales
        INTENT_KEYWORDS.put("AIDE", Arrays.asList(
                "aide", "help", "commande", "que faire", "comment utiliser", "besoin d'aide"
        ));

        INTENT_KEYWORDS.put("SALUTATION", Arrays.asList(
                "bonjour", "salut", "hello", "coucou", "hey", "bonsoir"
        ));
    }

    public ParsedIntent parseIntent(String message, String userRole) {
        String lowerMessage = message.toLowerCase().trim();

        // Extraire les entités (numéros de commande, codes article, lots)
        String commandeNumero = extractCommandeNumero(lowerMessage);
        String articleCode = extractArticleCode(lowerMessage);
        String lotNumber = extractLotNumber(lowerMessage);

        // Détecter l'intention
        String intent = detectIntent(lowerMessage);

        // Adapter l'intention selon le rôle
        intent = adaptIntentToRole(intent, userRole);

        return new ParsedIntent(intent, commandeNumero, articleCode, lotNumber, lowerMessage);
    }

    private String detectIntent(String message) {
        for (Map.Entry<String, List<String>> entry : INTENT_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (message.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        return "INCONNU";
    }

    private String extractCommandeNumero(String message) {
        Pattern pattern = Pattern.compile("(?:commande|cmd)[\\s-]*([A-Z0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractArticleCode(String message) {
        Pattern pattern = Pattern.compile("(?:article|ref|code)[\\s-]*([A-Z0-9\\-]{3,15})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractLotNumber(String message) {
        Pattern pattern = Pattern.compile("(?:lot|lote)[\\s-]*([A-Z0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String adaptIntentToRole(String intent, String userRole) {
        // Certaines intentions ne sont pas accessibles selon le rôle
        if (userRole.equals("TRANSPORTEUR")) {
            if (intent.equals("STOCK_FAIBLE") || intent.equals("COMMANDE_A_EXPEDIER")) {
                return "NON_AUTORISE";
            }
        }
        if (userRole.equals("SERVICE_COMMERCIAL")) {
            if (intent.equals("RANGEMENT_TACHES") || intent.equals("RECEPTION_ATTENTE")) {
                return "NON_AUTORISE";
            }
        }
        return intent;
    }

    // Classe interne pour le résultat du parsing
    public static class ParsedIntent {
        private final String intent;
        private final String commandeNumero;
        private final String articleCode;
        private final String lotNumber;
        private final String originalMessage;

        public ParsedIntent(String intent, String commandeNumero, String articleCode, String lotNumber, String originalMessage) {
            this.intent = intent;
            this.commandeNumero = commandeNumero;
            this.articleCode = articleCode;
            this.lotNumber = lotNumber;
            this.originalMessage = originalMessage;
        }

        public String getIntent() { return intent; }
        public String getCommandeNumero() { return commandeNumero; }
        public String getArticleCode() { return articleCode; }
        public String getLotNumber() { return lotNumber; }
        public String getOriginalMessage() { return originalMessage; }
    }
}