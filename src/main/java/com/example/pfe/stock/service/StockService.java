package com.example.pfe.stock.service;

import com.example.pfe.article.entity.Article;
import com.example.pfe.article.repository.ArticleRepository;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
import com.example.pfe.notification.enums.NotificationType;
import com.example.pfe.notification.service.NotificationService;
import com.example.pfe.stock.dto.StockDTO;
import com.example.pfe.stock.entity.MouvementStock;
import com.example.pfe.stock.entity.Stock;
import com.example.pfe.stock.entity.StockStatut;
import com.example.pfe.stock.repository.MouvementStockRepository;
import com.example.pfe.stock.repository.StockRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final MouvementStockRepository mouvementRepository;
    private final NotificationService notificationService;

    private static final int SEUIL_CRITIQUE = 10;
    private static final int SEUIL_ALERTE = 20;
    private static final int SEUIL_BLOCAGE = 1000;

    public StockService(StockRepository stockRepository,
                        ArticleRepository articleRepository,
                        UserRepository userRepository,
                        WarehouseRepository warehouseRepository,
                        MouvementStockRepository mouvementRepository,
                        NotificationService notificationService) {
        this.stockRepository = stockRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
        this.mouvementRepository = mouvementRepository;
        this.notificationService = notificationService;
    }

    // ========== MÉTHODES EXISTANTES (INCHANGÉES) ==========

    public List<StockDTO> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StockDTO getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé avec l'id: " + id));
        return convertToDTO(stock);
    }

    public List<StockDTO> getStocksByArticle(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new RuntimeException("Article non trouvé");
        }
        return stockRepository.findByArticleId(articleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StockDTO getStockByLot(String lot) {
        List<Stock> stocks = stockRepository.findByLot(lot);
        if (stocks.isEmpty()) {
            throw new RuntimeException("Aucun stock trouvé pour le lot: " + lot);
        }
        if (stocks.size() > 1) {
            System.out.println("⚠️ Attention : plusieurs stocks trouvés pour le lot " + lot +
                    ". Retour du premier (ID: " + stocks.get(0).getId() + ").");
        }
        return convertToDTO(stocks.get(0));
    }

    public List<StockDTO> searchStocks(Long articleId, String lot, String emplacement, StockStatut statut) {
        return stockRepository.searchStocks(articleId, lot, emplacement, statut).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StockDTO augmenterQuantite(Long articleId, String lot, String emplacement, int quantite,
                                      LocalDateTime dateExpiration, StockStatut statut) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        Stock stock = stockRepository.findByLot(lot)
                .stream()
                .filter(s -> s.getArticle().getId().equals(articleId)
                        && s.getEmplacement().equalsIgnoreCase(emplacement))
                .findFirst()
                .orElse(null);

        int nouvelleQuantite;
        if (stock != null) {
            nouvelleQuantite = stock.getQuantite() + quantite;
            stock.setQuantite(nouvelleQuantite);
            if (dateExpiration != null) stock.setDateExpiration(dateExpiration);
        } else {
            nouvelleQuantite = quantite;
            stock = new Stock();
            stock.setArticle(article);
            stock.setLot(lot);
            stock.setEmplacement(emplacement);
            stock.setQuantite(quantite);
            stock.setDateReception(LocalDateTime.now());
            stock.setDateExpiration(dateExpiration);
        }

        if (nouvelleQuantite >= SEUIL_BLOCAGE) {
            stock.setStatut(StockStatut.BLOQUE);
        } else {
            stock.setStatut(statut != null ? statut : StockStatut.DISPONIBLE);
        }

        return convertToDTO(stockRepository.save(stock));
    }

    @Transactional
    public StockDTO augmenterQuantiteAvecEntrepot(Long articleId, String lot, String emplacement, int quantite,
                                                  LocalDateTime dateExpiration, StockStatut statut, Long entrepotId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        Warehouse entrepot = warehouseRepository.findById(entrepotId)
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));

        Stock stock = stockRepository.findByArticleIdAndEntrepotId(articleId, entrepotId)
                .stream()
                .filter(s -> s.getEmplacement().equalsIgnoreCase(emplacement))
                .findFirst()
                .orElse(null);

        int nouvelleQuantite;
        if (stock != null) {
            nouvelleQuantite = stock.getQuantite() + quantite;
            stock.setQuantite(nouvelleQuantite);
            if (dateExpiration != null) stock.setDateExpiration(dateExpiration);
            if (lot != null && !lot.isEmpty()) {
                stock.setLot(lot);
            }
            System.out.println("🔄 Fusion des lots pour l'article " + articleId + " à l'emplacement " + emplacement);
        } else {
            nouvelleQuantite = quantite;
            stock = new Stock();
            stock.setArticle(article);
            stock.setLot(lot);
            stock.setEmplacement(emplacement);
            stock.setQuantite(quantite);
            stock.setDateReception(LocalDateTime.now());
            stock.setDateExpiration(dateExpiration);
            stock.setEntrepot(entrepot);
        }

        if (nouvelleQuantite >= SEUIL_BLOCAGE) {
            stock.setStatut(StockStatut.BLOQUE);
        } else {
            stock.setStatut(statut != null ? statut : StockStatut.DISPONIBLE);
        }

        return convertToDTO(stockRepository.save(stock));
    }

    @Transactional
    public StockDTO diminuerQuantite(Long stockId, int quantite) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

        if (stock.getStatut() == StockStatut.BLOQUE) {
            throw new RuntimeException("Impossible de diminuer un stock bloqué");
        }

        if (stock.getQuantite() < quantite) {
            throw new RuntimeException("Quantité insuffisante. Disponible: " + stock.getQuantite());
        }

        int nouvelleQuantite = stock.getQuantite() - quantite;
        stock.setQuantite(nouvelleQuantite);

        // 🔹 ON NE SUPPRIME PLUS, ON GARDE AVEC QUANTITÉ 0 POUR L'HISTORIQUE
        if (nouvelleQuantite == 0) {
            System.out.println("📦 Stock épuisé pour l'article " + stock.getArticle().getId() + ", lot " + stock.getLot() + " (conservé pour historique)");

            // 🔔 NOTIFICATION : Stock épuisé
            envoyerNotificationStockEpuise(stock);
        }

        return convertToDTO(stockRepository.save(stock));
    }

    @Transactional
    public StockDTO changerStatut(Long stockId, StockStatut nouveauStatut) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));
        stock.setStatut(nouveauStatut);
        return convertToDTO(stockRepository.save(stock));
    }

    // ========== MÉTHODE DECREMENT STOCK AVEC STRATÉGIE FEFO (First Expired, First Out) ==========
    // 🔹 MODIFIÉE : NE SUPPRIME PAS LES STOCKS À 0, LES GARDE POUR L'HISTORIQUE

    @Transactional
    public void decrementStock(Long articleId, int quantity, Long entrepotId, String lot) {
        // Récupérer tous les stocks disponibles pour cet article dans l'entrepôt
        List<Stock> stocks = stockRepository.findByArticleIdAndEntrepotId(articleId, entrepotId)
                .stream()
                .filter(s -> s.getQuantite() > 0)
                .collect(Collectors.toList());

        if (stocks.isEmpty()) {
            throw new RuntimeException("Aucun stock disponible pour l'article " + articleId +
                    " dans l'entrepôt " + entrepotId);
        }

        // 🔹 TRI FEFO : Priorité aux lots qui expirent le plus tôt
        stocks.sort((s1, s2) -> {
            if (s1.getDateExpiration() != null && s2.getDateExpiration() != null) {
                return s1.getDateExpiration().compareTo(s2.getDateExpiration());
            }
            if (s1.getDateExpiration() != null) return -1;
            if (s2.getDateExpiration() != null) return 1;
            if (s1.getDateReception() != null && s2.getDateReception() != null) {
                return s1.getDateReception().compareTo(s2.getDateReception());
            }
            return 0;
        });

        // 🔹 Afficher l'ordre de priorité des lots
        System.out.println("📋 Ordre de priorité FEFO pour l'article " + articleId + " :");
        for (int i = 0; i < stocks.size(); i++) {
            Stock s = stocks.get(i);
            String expiration = s.getDateExpiration() != null ? s.getDateExpiration().toLocalDate().toString() : "Sans date";
            System.out.println("   " + (i+1) + ". Lot: " + s.getLot() + " | Qté: " + s.getQuantite() + " | Expiration: " + expiration);
        }

        // 🔹 CALCULER LA QUANTITÉ TOTALE DISPONIBLE
        int quantiteTotale = stocks.stream().mapToInt(Stock::getQuantite).sum();

        if (quantiteTotale < quantity) {
            throw new RuntimeException("Stock insuffisant pour l'article " + articleId +
                    ". Disponible: " + quantiteTotale + " unités au total");
        }

        // 🔹 DIMINUER EN PRIORISANT LES LOTS QUI EXPIRE LE PLUS TÔT (FEFO)
        int remainingToDecrement = quantity;

        for (Stock stock : stocks) {
            if (remainingToDecrement <= 0) break;

            int decrementFromThisStock = Math.min(stock.getQuantite(), remainingToDecrement);
            int nouvelleQuantite = stock.getQuantite() - decrementFromThisStock;
            stock.setQuantite(nouvelleQuantite);
            stock.setUpdatedAt(LocalDateTime.now());

            // Enregistrer le mouvement pour ce lot
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                MouvementStock mouvement = new MouvementStock();
                mouvement.setStockSource(stock);
                mouvement.setType("SORTIE");
                mouvement.setQuantite(decrementFromThisStock);
                mouvement.setMotif("VENTE");
                mouvement.setUtilisateur(currentUser);
                mouvement.setAncienneQuantiteSource(stock.getQuantite() + decrementFromThisStock);
                mouvement.setNouvelleQuantiteSource(nouvelleQuantite);
                mouvement.setDateMouvement(LocalDateTime.now());
                mouvementRepository.save(mouvement);
                String expiration = stock.getDateExpiration() != null ? stock.getDateExpiration().toLocalDate().toString() : "Sans date";
                System.out.println("📦 Mouvement FEFO : sortie de " + decrementFromThisStock +
                        " unités pour l'article " + articleId +
                        " (lot: " + stock.getLot() +
                        ", expiration: " + expiration + ")");
            }

            // 🔹 SAUVEGARDER LE STOCK (MÊME À 0, ON LE GARDE POUR L'HISTORIQUE)
            stockRepository.save(stock);

            if (nouvelleQuantite == 0) {
                System.out.println("📦 Stock épuisé pour l'article " + articleId + ", lot " + stock.getLot() + " (conservé pour historique)");
                // 🔔 NOTIFICATION : Stock épuisé
                envoyerNotificationStockEpuise(stock);
            } else if (nouvelleQuantite <= SEUIL_CRITIQUE) {
                // 🔔 NOTIFICATION : Stock critique
                envoyerNotificationStockCritique(stock);
            } else if (nouvelleQuantite <= SEUIL_ALERTE) {
                // 🔔 NOTIFICATION : Stock faible
                envoyerNotificationStockFaible(stock);
            }

            remainingToDecrement -= decrementFromThisStock;
        }

        System.out.println("📦 Stock total diminué pour l'article " + articleId + " : " + quantity + " unités");
    }

    // ========== MÉTHODES DE NOTIFICATION POUR LES STOCKS ==========

    private void envoyerNotificationStockFaible(Stock stock) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null && currentUser.getEntrepot() != null) {
                List<User> responsables = userRepository.findByRole(com.example.pfe.auth.entity.Role.RESPONSABLE_ENTREPOT);
                for (User responsable : responsables) {
                    if (responsable.getEntrepot() != null && responsable.getEntrepot().getId().equals(currentUser.getEntrepot().getId())) {
                        notificationService.createNotification(
                                responsable.getId(),
                                "⚠️ Stock faible",
                                String.format("L'article %s (lot: %s) a un stock de %d unités (seuil: %d)",
                                        stock.getArticle().getDesignation(),
                                        stock.getLot(),
                                        stock.getQuantite(),
                                        SEUIL_ALERTE),
                                NotificationType.ALERTE,
                                "/dashboard?tab=stock",
                                stock.getId(),
                                "STOCK"
                        );
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la notification stock faible: " + e.getMessage());
        }
    }

    private void envoyerNotificationStockCritique(Stock stock) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null && currentUser.getEntrepot() != null) {
                List<User> responsables = userRepository.findByRole(com.example.pfe.auth.entity.Role.RESPONSABLE_ENTREPOT);
                for (User responsable : responsables) {
                    if (responsable.getEntrepot() != null && responsable.getEntrepot().getId().equals(currentUser.getEntrepot().getId())) {
                        notificationService.createNotification(
                                responsable.getId(),
                                "🔴 Stock critique",
                                String.format("URGENT: L'article %s (lot: %s) a un stock critique de %d unités (seuil: %d)",
                                        stock.getArticle().getDesignation(),
                                        stock.getLot(),
                                        stock.getQuantite(),
                                        SEUIL_CRITIQUE),
                                NotificationType.ALERTE,
                                "/dashboard?tab=stock",
                                stock.getId(),
                                "STOCK"
                        );
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la notification stock critique: " + e.getMessage());
        }
    }

    private void envoyerNotificationStockEpuise(Stock stock) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null && currentUser.getEntrepot() != null) {
                List<User> responsables = userRepository.findByRole(com.example.pfe.auth.entity.Role.RESPONSABLE_ENTREPOT);
                for (User responsable : responsables) {
                    if (responsable.getEntrepot() != null && responsable.getEntrepot().getId().equals(currentUser.getEntrepot().getId())) {
                        notificationService.createNotification(
                                responsable.getId(),
                                "❌ Stock épuisé",
                                String.format("L'article %s (lot: %s) est épuisé. Réapprovisionnement nécessaire.",
                                        stock.getArticle().getDesignation(),
                                        stock.getLot()),
                                NotificationType.ERREUR,
                                "/dashboard?tab=stock",
                                stock.getId(),
                                "STOCK"
                        );
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la notification stock épuisé: " + e.getMessage());
        }
    }

    // ========== MÉTHODES AVEC FILTRE PAR ENTREPÔT ==========
    // ⚠️ CES MÉTHODES N'ONT PAS D'ANNOTATIONS @Cacheable POUR ÉVITER LES ERREURS SpEL

    private User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                String email = ((UserDetails) principal).getUsername();
                return userRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            System.out.println("Erreur récupération utilisateur: " + e.getMessage());
        }
        return null;
    }

    private Long getCurrentUserEntrepotId() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null && currentUser.getEntrepot() != null) {
                return currentUser.getEntrepot().getId();
            }
        } catch (Exception e) {
            System.out.println("Erreur récupération entrepôt utilisateur: " + e.getMessage());
        }
        return null;
    }

    public List<StockDTO> getAllStocksFiltered() {
        Long entrepotId = getCurrentUserEntrepotId();
        List<Stock> stocks;
        if (entrepotId != null) {
            stocks = stockRepository.findByEntrepotId(entrepotId);
        } else {
            stocks = stockRepository.findAll();
        }
        return stocks.stream()
                .filter(stock -> stock.getQuantite() > 0)  // 🔹 IGNORER LES STOCKS À 0
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StockDTO> getStocksByArticleFiltered(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new RuntimeException("Article non trouvé");
        }
        Long entrepotId = getCurrentUserEntrepotId();
        List<Stock> stocks;
        if (entrepotId != null) {
            stocks = stockRepository.findByArticleIdAndEntrepotId(articleId, entrepotId);
        } else {
            stocks = stockRepository.findByArticleId(articleId);
        }
        return stocks.stream()
                .filter(stock -> stock.getQuantite() > 0)  // 🔹 IGNORER LES STOCKS À 0
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StockDTO> searchStocksFiltered(Long articleId, String lot, String emplacement, StockStatut statut) {
        Long entrepotId = getCurrentUserEntrepotId();
        List<Stock> stocks = stockRepository.searchStocksWithEntrepot(entrepotId, articleId, lot, emplacement, statut);
        return stocks.stream()
                .filter(stock -> stock.getQuantite() > 0)  // 🔹 IGNORER LES STOCKS À 0
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========== NOUVELLE MÉTHODE : Récupérer les stocks faibles ==========
    public List<StockDTO> getStocksFaiblesFiltered(int seuil) {
        Long entrepotId = getCurrentUserEntrepotId();
        List<Stock> stocks;

        if (entrepotId != null) {
            stocks = stockRepository.findByEntrepotId(entrepotId);
        } else {
            stocks = stockRepository.findAll();
        }

        return stocks.stream()
                .filter(stock -> stock.getQuantite() > 0)  // 🔹 IGNORER LES STOCKS À 0
                .filter(stock -> stock.getQuantite() <= seuil)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StockDTO getStockByArticleAndEntrepot(Long articleId, Long entrepotId) {
        List<Stock> stocks = stockRepository.findByArticleIdAndEntrepotId(articleId, entrepotId);
        if (stocks.isEmpty()) {
            throw new RuntimeException("Aucun stock trouvé pour cet article dans l'entrepôt " + entrepotId);
        }

        // Filtrer les stocks avec quantité > 0
        List<Stock> stocksPositifs = stocks.stream()
                .filter(stock -> stock.getQuantite() > 0)
                .collect(Collectors.toList());

        if (stocksPositifs.isEmpty()) {
            throw new RuntimeException("Aucun stock disponible (quantité > 0) pour cet article dans l'entrepôt " + entrepotId);
        }

        int quantiteTotale = stocksPositifs.stream()
                .mapToInt(Stock::getQuantite)
                .sum();

        Stock premierStock = stocksPositifs.get(0);

        StockDTO dto = convertToDTO(premierStock);
        dto.setQuantite(quantiteTotale);

        System.out.println("📊 Stock total pour article " + articleId + " dans entrepôt " + entrepotId + " : " + quantiteTotale + " unités (sur " + stocksPositifs.size() + " lots)");

        return dto;
    }

    private StockDTO convertToDTO(Stock stock) {
        StockDTO dto = new StockDTO();
        dto.setId(stock.getId());
        dto.setArticleId(stock.getArticle().getId());
        dto.setArticleCode(stock.getArticle().getCodeArticleERP());
        dto.setArticleDesignation(stock.getArticle().getDesignation());
        dto.setLot(stock.getLot());
        dto.setEmplacement(stock.getEmplacement());
        dto.setQuantite(stock.getQuantite());
        dto.setStatut(stock.getStatut());
        dto.setDateReception(stock.getDateReception());
        dto.setDateExpiration(stock.getDateExpiration());
        dto.setCreatedAt(stock.getCreatedAt());
        dto.setUpdatedAt(stock.getUpdatedAt());
        return dto;
    }
}