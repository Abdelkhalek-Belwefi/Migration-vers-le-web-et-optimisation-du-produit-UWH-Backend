package com.example.pfe.stock.service;

import com.example.pfe.article.entity.Article;
import com.example.pfe.article.repository.ArticleRepository;
import com.example.pfe.stock.dto.StockDTO;
import com.example.pfe.stock.entity.Stock;
import com.example.pfe.stock.entity.StockStatut;
import com.example.pfe.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final ArticleRepository articleRepository;

    public StockService(StockRepository stockRepository, ArticleRepository articleRepository) {
        this.stockRepository = stockRepository;
        this.articleRepository = articleRepository;
    }

    // --- Consultation ---
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

    // Méthode adaptée pour gérer plusieurs stocks avec le même lot
    public StockDTO getStockByLot(String lot) {
        List<Stock> stocks = stockRepository.findByLot(lot);
        if (stocks.isEmpty()) {
            throw new RuntimeException("Aucun stock trouvé pour le lot: " + lot);
        }
        if (stocks.size() > 1) {
            // En cas de doublon, on retourne le premier mais on avertit
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

    // --- Mouvements ---
    @Transactional
    public StockDTO augmenterQuantite(Long articleId, String lot, String emplacement, int quantite,
                                      LocalDateTime dateExpiration, StockStatut statut) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        // Recherche d'un stock existant avec le même article, lot et emplacement
        Stock stock = stockRepository.findByLot(lot)
                .stream()
                .filter(s -> s.getArticle().getId().equals(articleId)
                        && s.getEmplacement().equalsIgnoreCase(emplacement))
                .findFirst()
                .orElse(null);

        if (stock != null) {
            stock.setQuantite(stock.getQuantite() + quantite);
            if (dateExpiration != null) stock.setDateExpiration(dateExpiration);
            if (statut != null) stock.setStatut(statut);
        } else {
            stock = new Stock();
            stock.setArticle(article);
            stock.setLot(lot);
            stock.setEmplacement(emplacement);
            stock.setQuantite(quantite);
            stock.setStatut(statut != null ? statut : StockStatut.DISPONIBLE);
            stock.setDateReception(LocalDateTime.now());
            stock.setDateExpiration(dateExpiration);
        }

        return convertToDTO(stockRepository.save(stock));
    }

    @Transactional
    public StockDTO diminuerQuantite(Long stockId, int quantite) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

        if (stock.getQuantite() < quantite) {
            throw new RuntimeException("Quantité insuffisante. Disponible: " + stock.getQuantite());
        }

        stock.setQuantite(stock.getQuantite() - quantite);
        return convertToDTO(stockRepository.save(stock));
    }

    // --- Changement de statut ---
    @Transactional
    public StockDTO changerStatut(Long stockId, StockStatut nouveauStatut) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));
        stock.setStatut(nouveauStatut);
        return convertToDTO(stockRepository.save(stock));
    }

    // --- Conversion ---
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