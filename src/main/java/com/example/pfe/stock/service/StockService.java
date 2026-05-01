package com.example.pfe.stock.service;

import com.example.pfe.article.entity.Article;
import com.example.pfe.article.repository.ArticleRepository;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final MouvementStockRepository mouvementRepository;

    private static final int SEUIL_BLOCAGE = 1000;

    public StockService(StockRepository stockRepository,
                        ArticleRepository articleRepository,
                        UserRepository userRepository,
                        WarehouseRepository warehouseRepository,
                        MouvementStockRepository mouvementRepository) {
        this.stockRepository = stockRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
        this.mouvementRepository = mouvementRepository;
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

        // 🔹 FUSION DES LOTS : chercher un stock avec le même article, même emplacement, même entrepôt
        // (peu importe le lot, on fusionne les quantités)
        Stock stock = stockRepository.findByArticleIdAndEntrepotId(articleId, entrepotId)
                .stream()
                .filter(s -> s.getEmplacement().equalsIgnoreCase(emplacement))
                .findFirst()
                .orElse(null);

        int nouvelleQuantite;
        if (stock != null) {
            // Fusion : on ajoute la quantité au stock existant
            nouvelleQuantite = stock.getQuantite() + quantite;
            stock.setQuantite(nouvelleQuantite);
            if (dateExpiration != null) stock.setDateExpiration(dateExpiration);
            // Optionnel : mettre à jour le lot avec le plus récent
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

        // 🔹 SI LA QUANTITÉ DEVIENT 0, ON SUPPRIME LA LIGNE DE STOCK
        if (nouvelleQuantite == 0) {
            System.out.println("🗑️ Suppression du stock ID " + stockId + " (quantité = 0)");
            stockRepository.delete(stock);
            return null;
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

    // ========== MÉTHODE DECREMENT STOCK CORRIGÉE (avec fallback sur n'importe quel lot) ==========

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

        // Chercher le lot spécifique d'abord
        Stock stock = stocks.stream()
                .filter(s -> s.getLot().equals(lot))
                .findFirst()
                .orElse(null);

        // Si le lot spécifique n'existe pas, prendre le premier stock disponible
        if (stock == null) {
            stock = stocks.get(0);
            System.out.println("⚠️ Lot " + lot + " non trouvé, utilisation du lot " + stock.getLot());
        }

        int ancienneQuantite = stock.getQuantite();

        if (ancienneQuantite < quantity) {
            throw new RuntimeException("Stock insuffisant pour l'article " + articleId +
                    ". Disponible: " + ancienneQuantite + " (lot: " + stock.getLot() + ")");
        }

        int nouvelleQuantite = ancienneQuantite - quantity;
        stock.setQuantite(nouvelleQuantite);
        stock.setUpdatedAt(LocalDateTime.now());

        // 🔹 SI LA QUANTITÉ DEVIENT 0, ON SUPPRIME LA LIGNE DE STOCK
        if (nouvelleQuantite == 0) {
            System.out.println("🗑️ Suppression du stock (decrementStock) pour article " + articleId + ", lot " + stock.getLot());
            stockRepository.delete(stock);
        } else {
            stockRepository.save(stock);
        }

        User currentUser = getCurrentUser();
        if (currentUser != null) {
            MouvementStock mouvement = new MouvementStock();
            mouvement.setStockSource(stock);
            mouvement.setType("SORTIE");
            mouvement.setQuantite(quantity);
            mouvement.setMotif("VENTE");
            mouvement.setUtilisateur(currentUser);
            mouvement.setAncienneQuantiteSource(ancienneQuantite);
            mouvement.setNouvelleQuantiteSource(nouvelleQuantite);
            mouvement.setDateMouvement(LocalDateTime.now());
            mouvementRepository.save(mouvement);
            System.out.println("📦 Mouvement de stock enregistré : sortie de " + quantity +
                    " unités pour l'article " + articleId + " (lot: " + stock.getLot() + ")");
        }
    }

    // ========== MÉTHODES AVEC FILTRE PAR ENTREPÔT ==========

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
        // Filtrer les stocks à 0 et prendre le premier disponible
        stocks = stocks.stream()
                .filter(stock -> stock.getQuantite() > 0)
                .collect(Collectors.toList());
        if (stocks.isEmpty()) {
            throw new RuntimeException("Aucun stock disponible (quantité > 0) pour cet article dans l'entrepôt " + entrepotId);
        }
        Stock stock = stocks.get(0);
        return convertToDTO(stock);
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