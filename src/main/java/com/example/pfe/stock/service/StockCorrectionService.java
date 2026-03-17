package com.example.pfe.stock.service;

import com.example.pfe.stock.entity.Stock;
import com.example.pfe.stock.entity.StockStatut;
import com.example.pfe.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class StockCorrectionService {

    private final StockRepository stockRepository;
    private static final int SEUIL_BLOCAGE = 1000;

    public StockCorrectionService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * Corrige les stocks existants au démarrage de l'application :
     * - Si quantité >= 1000 → BLOQUE
     * - Si quantité < 1000 et statut = BLOQUE ou DISPONIBLE → DISPONIBLE
     * - Les autres statuts (RESERVE, QUALITE) sont conservés.
     */
    @PostConstruct
    @Transactional
    public void corrigerStocksExistants() {
        System.out.println("=== DÉBUT CORRECTION DES STOCKS EXISTANTS ===");
        List<Stock> stocks = stockRepository.findAll();
        int modifie = 0;

        for (Stock stock : stocks) {
            StockStatut ancienStatut = stock.getStatut();
            StockStatut nouveauStatut = ancienStatut;

            // Règle de blocage par quantité
            if (stock.getQuantite() >= SEUIL_BLOCAGE) {
                nouveauStatut = StockStatut.BLOQUE;
            } else {
                // Si la quantité est inférieure au seuil, on repasse en DISPONIBLE
                // uniquement si le statut actuel est BLOQUE ou DISPONIBLE
                if (ancienStatut == StockStatut.BLOQUE || ancienStatut == StockStatut.DISPONIBLE) {
                    nouveauStatut = StockStatut.DISPONIBLE;
                }
                // Sinon on garde RESERVE ou QUALITE
            }

            if (nouveauStatut != ancienStatut) {
                stock.setStatut(nouveauStatut);
                stockRepository.save(stock);
                modifie++;
                System.out.println("Stock ID " + stock.getId() + " : " + ancienStatut + " → " + nouveauStatut +
                        " (quantité=" + stock.getQuantite() + ")");
            }
        }

        System.out.println("=== CORRECTION TERMINÉE : " + modifie + " stocks modifiés ===");
    }
}