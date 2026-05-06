package com.example.pfe.chatbot.service;

import com.example.pfe.article.dto.ArticleDTO;
import com.example.pfe.article.service.ArticleService;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.chatbot.dto.ChatResponseDTO;
import com.example.pfe.commande.dto.CommandeDTO;
import com.example.pfe.commande.service.CommandeService;
import com.example.pfe.livraison.dto.LivraisonDTO;
import com.example.pfe.livraison.service.LivraisonService;
import com.example.pfe.reception.dto.ReceptionDTO;
import com.example.pfe.reception.service.ReceptionService;
import com.example.pfe.stock.dto.StockDTO;
import com.example.pfe.stock.service.StockService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActionExecutorService {

    private final StockService stockService;
    private final CommandeService commandeService;
    private final LivraisonService livraisonService;
    private final ReceptionService receptionService;
    private final ArticleService articleService;
    private final UserRepository userRepository;

    public ActionExecutorService(StockService stockService,
                                 CommandeService commandeService,
                                 LivraisonService livraisonService,
                                 ReceptionService receptionService,
                                 ArticleService articleService,
                                 UserRepository userRepository) {
        this.stockService = stockService;
        this.commandeService = commandeService;
        this.livraisonService = livraisonService;
        this.receptionService = receptionService;
        this.articleService = articleService;
        this.userRepository = userRepository;
    }

    public ChatResponseDTO executeAction(String intent, String userRole, Long userId,
                                         String commandeNumero, String articleCode, String lotNumber) {

        switch (intent) {
            case "STOCK_FAIBLE":
                return getStocksFaibles(userId);
            case "STOCK_ARTICLE":
                return getStockArticle(articleCode, userId);
            case "COMMANDE_STATUT":
                return getCommandeStatut(commandeNumero, userId);
            case "COMMANDE_A_EXPEDIER":
                return getCommandesAExpedier(userId);
            case "TRANSFERT_DEMANDES":
                return getDemandesTransfert(userId);
            case "RECEPTION_ATTENTE":
                return getReceptionsAttente(userId);
            case "LIVRAISON_JOUR":
                return getLivraisonsJour(userId);
            case "RANGEMENT_TACHES":
                return getTachesRangement(userId);
            case "AIDE":
                return getHelpMessage(userRole);
            case "SALUTATION":
                return getSalutation(userRole);
            case "NON_AUTORISE":
                return getNonAutoriseMessage();
            default:
                return getDefaultMessage();
        }
    }

    private ChatResponseDTO getStocksFaibles(Long userId) {
        try {
            List<StockDTO> stocks = stockService.getStocksFaiblesFiltered(20);
            if (stocks.isEmpty()) {
                return new ChatResponseDTO("✅ Aucun stock faible à signaler. Tous les stocks sont suffisants.", "TEXT");
            }

            List<Map<String, Object>> cards = new ArrayList<>();
            for (StockDTO stock : stocks.stream().limit(5).collect(Collectors.toList())) {
                Map<String, Object> card = new HashMap<>();
                card.put("title", stock.getArticleDesignation());
                card.put("subtitle", "Code: " + stock.getArticleCode() + " | Lot: " + stock.getLot());

                Map<String, String> fields = new HashMap<>();
                fields.put("📦 Quantité", String.valueOf(stock.getQuantite()) + " unités");
                fields.put("📍 Emplacement", stock.getEmplacement());
                fields.put("⚠️ Seuil", "20 unités");
                if (stock.getDateExpiration() != null) {
                    fields.put("📅 Expiration", stock.getDateExpiration().toLocalDate().toString());
                }
                card.put("fields", fields);
                card.put("buttonLabel", "Voir détails");
                card.put("buttonAction", "/stock?article=" + stock.getArticleId());
                cards.add(card);
            }

            ChatResponseDTO response = new ChatResponseDTO(
                    String.format("📊 Voici les %d articles avec stock faible (<20 unités) :", stocks.size()),
                    "CARDS"
            );
            response.setCards(cards);
            return response;
        } catch (Exception e) {
            return new ChatResponseDTO("❌ Désolé, une erreur est survenue lors de la récupération des stocks.", "TEXT");
        }
    }

    private ChatResponseDTO getStockArticle(String articleCode, Long userId) {
        if (articleCode == null) {
            return new ChatResponseDTO("ℹ️ Veuillez préciser le code article. Exemple: 'stock de ART-01'", "TEXT");
        }

        try {
            ArticleDTO article = articleService.getArticleByCodeERP(articleCode);
            List<StockDTO> stocks = stockService.getStocksByArticleFiltered(article.getId());
            int quantiteTotale = stocks.stream().mapToInt(StockDTO::getQuantite).sum();

            ChatResponseDTO response = new ChatResponseDTO(
                    String.format("📦 Article: %s (Code: %s)\n📊 Stock total: %d unités",
                            article.getDesignation(), article.getCodeArticleERP(), quantiteTotale),
                    "TEXT"
            );

            if (!stocks.isEmpty()) {
                List<Map<String, Object>> cards = new ArrayList<>();
                for (StockDTO stock : stocks) {
                    Map<String, Object> card = new HashMap<>();
                    card.put("title", "Lot: " + stock.getLot());
                    Map<String, String> fields = new HashMap<>();
                    fields.put("📍 Emplacement", stock.getEmplacement());
                    fields.put("📦 Quantité", String.valueOf(stock.getQuantite()) + " unités");
                    fields.put("📅 Réception", stock.getDateReception() != null ?
                            stock.getDateReception().toLocalDate().toString() : "-");
                    if (stock.getDateExpiration() != null) {
                        fields.put("⚠️ Expiration", stock.getDateExpiration().toLocalDate().toString());
                    }
                    card.put("fields", fields);
                    cards.add(card);
                }
                response.setType("CARDS");
                response.setCards(cards);
            }
            return response;
        } catch (Exception e) {
            return new ChatResponseDTO("❌ Article non trouvé avec le code: " + articleCode, "TEXT");
        }
    }

    private ChatResponseDTO getCommandeStatut(String commandeNumero, Long userId) {
        if (commandeNumero == null) {
            return new ChatResponseDTO("ℹ️ Veuillez préciser le numéro de commande. Exemple: 'statut commande CMD-123'", "TEXT");
        }

        try {
            List<CommandeDTO> commandes = commandeService.getAllCommandesFiltered();
            Optional<CommandeDTO> commandeOpt = commandes.stream()
                    .filter(c -> c.getNumeroCommande().equalsIgnoreCase(commandeNumero))
                    .findFirst();

            if (!commandeOpt.isPresent()) {
                return new ChatResponseDTO("❌ Commande non trouvée: " + commandeNumero, "TEXT");
            }

            CommandeDTO commande = commandeOpt.get();
            String statutMessage = getStatutMessage(commande.getStatut().name());

            return new ChatResponseDTO(
                    String.format("📋 Commande %s\n📅 Date: %s\n📌 Statut: %s\n👤 Client: %s",
                            commande.getNumeroCommande(),
                            commande.getDateCommande().toLocalDate().toString(),
                            statutMessage,
                            commande.getClientNom() != null ? commande.getClientNom() : "N/A"),
                    "TEXT"
            );
        } catch (Exception e) {
            return new ChatResponseDTO("❌ Erreur lors de la recherche de la commande.", "TEXT");
        }
    }

    private ChatResponseDTO getCommandesAExpedier(Long userId) {
        try {
            List<CommandeDTO> commandes = commandeService.getCommandesAExpedierFiltered();
            if (commandes.isEmpty()) {
                return new ChatResponseDTO("✅ Aucune commande à expédier pour le moment.", "TEXT");
            }

            List<Map<String, Object>> cards = new ArrayList<>();
            for (CommandeDTO commande : commandes.stream().limit(5).collect(Collectors.toList())) {
                Map<String, Object> card = new HashMap<>();
                card.put("title", "Commande " + commande.getNumeroCommande());
                card.put("subtitle", "Client: " + (commande.getClientNom() != null ? commande.getClientNom() : "N/A"));
                Map<String, String> fields = new HashMap<>();
                fields.put("📦 Articles", String.valueOf(commande.getLignes().size()));
                fields.put("📅 Date", commande.getDateCommande().toLocalDate().toString());
                if (commande.getDateLivraisonSouhaitee() != null) {
                    fields.put("🚚 Livraison souhaitée", commande.getDateLivraisonSouhaitee().toString());
                }
                card.put("fields", fields);
                card.put("buttonLabel", "Préparer l'expédition");
                card.put("buttonAction", "/expedier?commande=" + commande.getId());
                cards.add(card);
            }

            ChatResponseDTO response = new ChatResponseDTO(
                    String.format("🚚 %d commande(s) en attente d'expédition :", commandes.size()),
                    "CARDS"
            );
            response.setCards(cards);
            return response;
        } catch (Exception e) {
            return new ChatResponseDTO("❌ Erreur lors de la récupération des commandes.", "TEXT");
        }
    }

    private ChatResponseDTO getDemandesTransfert(Long userId) {
        return new ChatResponseDTO(
                "📤 Pour consulter les demandes de transfert, rendez-vous dans le menu 'Demandes reçues'.",
                "TEXT"
        );
    }

    private ChatResponseDTO getReceptionsAttente(Long userId) {
        try {
            List<ReceptionDTO> receptions = receptionService.getReceptionsByStatutFiltered(
                    com.example.pfe.reception.entity.ReceptionStatut.EN_ATTENTE);

            if (receptions.isEmpty()) {
                return new ChatResponseDTO("✅ Aucune réception en attente de validation.", "TEXT");
            }

            List<Map<String, Object>> cards = new ArrayList<>();
            for (ReceptionDTO reception : receptions.stream().limit(5).collect(Collectors.toList())) {
                Map<String, Object> card = new HashMap<>();
                card.put("title", "PO: " + reception.getNumeroPO());
                card.put("subtitle", "Fournisseur: " + (reception.getFournisseur() != null ? reception.getFournisseur() : "N/A"));
                Map<String, String> fields = new HashMap<>();
                fields.put("📦 Lignes", String.valueOf(reception.getLignes().size()));
                fields.put("📅 Date", reception.getDateReception().toLocalDate().toString());
                fields.put("👤 Créé par", reception.getCreateurNom() != null ? reception.getCreateurNom() : "N/A");
                card.put("fields", fields);
                card.put("buttonLabel", "Valider la réception");
                card.put("buttonAction", "/reception/" + reception.getId());
                cards.add(card);
            }

            ChatResponseDTO response = new ChatResponseDTO(
                    String.format("📦 %d réception(s) en attente de validation :", receptions.size()),
                    "CARDS"
            );
            response.setCards(cards);
            return response;
        } catch (Exception e) {
            return new ChatResponseDTO("❌ Erreur lors de la récupération des réceptions.", "TEXT");
        }
    }

    private ChatResponseDTO getLivraisonsJour(Long userId) {
        try {
            List<LivraisonDTO> livraisons = livraisonService.getLivraisonsPourTransporteur();
            if (livraisons.isEmpty()) {
                return new ChatResponseDTO("🚚 Aucune livraison programmée pour aujourd'hui.", "TEXT");
            }

            List<Map<String, Object>> cards = new ArrayList<>();
            for (LivraisonDTO livraison : livraisons) {
                Map<String, Object> card = new HashMap<>();
                card.put("title", "BL: " + livraison.getNumeroBL());
                card.put("subtitle", "Client: " + livraison.getClientNom());
                Map<String, String> fields = new HashMap<>();
                fields.put("📍 Adresse", livraison.getAdresseLivraison());
                fields.put("📅 Assignée le", livraison.getDateAssignation().toLocalDate().toString());
                card.put("fields", fields);
                card.put("buttonLabel", "Voir itinéraire");
                card.put("buttonAction", "/transporteur");
                cards.add(card);
            }

            ChatResponseDTO response = new ChatResponseDTO(
                    String.format("🚚 Vous avez %d livraison(s) en cours :", livraisons.size()),
                    "CARDS"
            );
            response.setCards(cards);
            return response;
        } catch (Exception e) {
            return new ChatResponseDTO("❌ Erreur lors de la récupération des livraisons.", "TEXT");
        }
    }

    private ChatResponseDTO getTachesRangement(Long userId) {
        return new ChatResponseDTO(
                "📋 Pour voir vos tâches de rangement, rendez-vous dans le menu 'Rangement'.\n" +
                        "Vous pouvez également scanner un code-barres pour commencer une tâche.",
                "TEXT"
        );
    }

    private ChatResponseDTO getHelpMessage(String userRole) {
        String helpMessage = getHelpByRole(userRole);
        return new ChatResponseDTO(helpMessage, "TEXT");
    }

    private String getHelpByRole(String userRole) {
        switch (userRole) {
            case "RESPONSABLE_ENTREPOT":
                return "🤖 Commandes disponibles pour vous :\n" +
                        "• 'Quels sont les stocks faibles ?' - Voir les articles sous seuil\n" +
                        "• 'Stock de ART-01' - Voir le stock d'un article\n" +
                        "• 'Commandes à expédier' - Voir les commandes prêtes\n" +
                        "• 'Réceptions en attente' - Voir les réceptions à valider\n" +
                        "• 'Livraisons aujourd'hui' - Voir les livraisons\n" +
                        "• 'Aide' - Afficher cette aide";
            case "OPERATEUR_ENTREPOT":
                return "🤖 Commandes disponibles pour vous :\n" +
                        "• 'Tâches de rangement' - Voir les tâches à faire\n" +
                        "• 'Réceptions en attente' - Voir les réceptions\n" +
                        "• 'Aide' - Afficher cette aide";
            case "TRANSPORTEUR":
                return "🤖 Commandes disponibles pour vous :\n" +
                        "• 'Livraisons aujourd'hui' - Voir vos livraisons\n" +
                        "• 'Aide' - Afficher cette aide";
            case "SERVICE_COMMERCIAL":
                return "🤖 Commandes disponibles pour vous :\n" +
                        "• 'Statut commande CMD-XXX' - Voir l'état d'une commande\n" +
                        "• 'Commandes à expédier' - Voir les commandes prêtes\n" +
                        "• 'Aide' - Afficher cette aide";
            default:
                return "🤖 Commandes disponibles :\n" +
                        "• 'Aide' - Afficher cette aide\n" +
                        "• 'Bonjour' - Me saluer";
        }
    }

    private ChatResponseDTO getSalutation(String userRole) {
        String roleName = "";
        switch (userRole) {
            case "RESPONSABLE_ENTREPOT": roleName = "Responsable d'entrepôt"; break;
            case "OPERATEUR_ENTREPOT": roleName = "Opérateur"; break;
            case "TRANSPORTEUR": roleName = "Transporteur"; break;
            case "SERVICE_COMMERCIAL": roleName = "Commercial"; break;
            default: roleName = "Utilisateur";
        }

        return new ChatResponseDTO(
                "👋 Bonjour " + roleName + " ! Je suis votre assistant L-Mobile Smart-Assist.\n\n" +
                        "Je peux vous aider à consulter les stocks, suivre les commandes et gérer vos livraisons.\n" +
                        "Tapez 'Aide' pour voir toutes les commandes disponibles.",
                "TEXT"
        );
    }

    private ChatResponseDTO getNonAutoriseMessage() {
        return new ChatResponseDTO(
                "⛔ Désolé, vous n'avez pas les droits pour accéder à cette information.\n" +
                        "Contactez votre administrateur si nécessaire.",
                "TEXT"
        );
    }

    private ChatResponseDTO getDefaultMessage() {
        return new ChatResponseDTO(
                "🤔 Désolé, je n'ai pas compris votre demande.\n" +
                        "Tapez 'Aide' pour voir toutes les commandes disponibles.",
                "TEXT"
        );
    }

    private String getStatutMessage(String statut) {
        switch (statut) {
            case "EN_ATTENTE": return "⏳ En attente de validation";
            case "EN_PREPARATION": return "📦 En cours de préparation";
            case "VALIDEE": return "✅ Validée - Prête à expédier";
            case "EXPEDIEE": return "🚚 Expédiée - En cours de livraison";
            default: return statut;
        }
    }
}