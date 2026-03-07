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

    // --- Consultation ---
    @GetMapping
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<StockDTO> getStockById(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getStockById(id));
    }

    @GetMapping("/article/{articleId}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<StockDTO>> getStocksByArticle(@PathVariable Long articleId) {
        return ResponseEntity.ok(stockService.getStocksByArticle(articleId));
    }

    @GetMapping("/lot/{lot}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<StockDTO> getStockByLot(@PathVariable String lot) {
        return ResponseEntity.ok(stockService.getStockByLot(lot));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<StockDTO>> searchStocks(
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) String lot,
            @RequestParam(required = false) String emplacement,
            @RequestParam(required = false) StockStatut statut) {
        return ResponseEntity.ok(stockService.searchStocks(articleId, lot, emplacement, statut));
    }

    // --- Mouvements ---
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

    // ✅ Changement de statut - Vérifiez que cette méthode existe bien
    @PutMapping("/{id}/statut")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<StockDTO> changerStatut(
            @PathVariable Long id,
            @RequestParam StockStatut statut) {
        return ResponseEntity.ok(stockService.changerStatut(id, statut));
    }
}