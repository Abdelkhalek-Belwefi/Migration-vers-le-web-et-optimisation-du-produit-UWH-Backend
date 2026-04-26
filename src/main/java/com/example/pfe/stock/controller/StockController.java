package com.example.pfe.stock.controller;

import com.example.pfe.stock.dto.StockDTO;
import com.example.pfe.stock.entity.StockStatut;
import com.example.pfe.stock.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "http://localhost:5173")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // ========== MÉTHODES EXISTANTES (INCHANGÉES) ==========

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<StockDTO> getStockById(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getStockById(id));
    }

    @GetMapping("/lot/{lot}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<StockDTO> getStockByLot(@PathVariable String lot) {
        return ResponseEntity.ok(stockService.getStockByLot(lot));
    }

    // ========== MÉTHODES MODIFIÉES (FILTRAGE PAR ENTREPÔT) ==========

    /**
     * Récupère tous les stocks (filtrés par entrepôt de l'utilisateur connecté)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocksFiltered());
    }

    /**
     * Récupère les stocks d'un article (filtrés par entrepôt de l'utilisateur connecté)
     */
    @GetMapping("/article/{articleId}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<StockDTO>> getStocksByArticle(@PathVariable Long articleId) {
        return ResponseEntity.ok(stockService.getStocksByArticleFiltered(articleId));
    }

    /**
     * Recherche avancée des stocks (filtrée par entrepôt de l'utilisateur connecté)
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<StockDTO>> searchStocks(
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) String lot,
            @RequestParam(required = false) String emplacement,
            @RequestParam(required = false) StockStatut statut) {
        return ResponseEntity.ok(stockService.searchStocksFiltered(articleId, lot, emplacement, statut));
    }

    // ========== MÉTHODES DE MOUVEMENT (INCHANGÉES) ==========

    @PostMapping("/augmenter")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<StockDTO> augmenterQuantite(
            @RequestParam Long articleId,
            @RequestParam String lot,
            @RequestParam String emplacement,
            @RequestParam int quantite,
            @RequestParam(required = false) LocalDateTime dateExpiration,
            @RequestParam(required = false) StockStatut statut) {
        return ResponseEntity.ok(stockService.augmenterQuantite(articleId, lot, emplacement, quantite,
                dateExpiration, statut));
    }

    @PostMapping("/diminuer")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<StockDTO> diminuerQuantite(
            @RequestParam Long stockId,
            @RequestParam int quantite) {
        return ResponseEntity.ok(stockService.diminuerQuantite(stockId, quantite));
    }

    @PutMapping("/{id}/statut")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<StockDTO> changerStatut(
            @PathVariable Long id,
            @RequestParam StockStatut statut) {
        return ResponseEntity.ok(stockService.changerStatut(id, statut));
    }
}