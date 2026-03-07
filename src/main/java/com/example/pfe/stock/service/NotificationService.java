package com.example.pfe.stock.service;

import com.example.pfe.stock.entity.Stock;
import com.example.pfe.stock.repository.StockRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final StockRepository stockRepository;
    private final Map<Long, Boolean> notificationsEnvoyees = new HashMap<>();

    // Seuils configurables (pourraient venir d'une table de paramètres)
    private static final int SEUIL_CRITIQUE = 10;
    private static final int SEUIL_ALERTE = 20;

    public NotificationService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Scheduled(fixedDelay = 3600000) // Toutes les heures
    public void verifierStocksBas() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            int quantite = stock.getQuantite();
            Long stockId = stock.getId();

            if (quantite <= SEUIL_CRITIQUE) {
                if (!notificationsEnvoyees.getOrDefault(stockId, false)) {
                    envoyerNotificationCritique(stock);
                    notificationsEnvoyees.put(stockId, true);
                }
            } else if (quantite <= SEUIL_ALERTE) {
                // On peut réinitialiser si le stock remonte
                notificationsEnvoyees.remove(stockId);
                envoyerNotificationAlerte(stock);
            } else {
                notificationsEnvoyees.remove(stockId);
            }
        }
    }

    private void envoyerNotificationCritique(Stock stock) {
        String message = String.format(
                "🔴 STOCK CRITIQUE : %s (lot %s) - Quantité: %d (seuil: %d)",
                stock.getArticle().getDesignation(),
                stock.getLot(),
                stock.getQuantite(),
                SEUIL_CRITIQUE
        );
        // Ici, vous pouvez implémenter l'envoi réel :
        // - Email
        // - Notification push
        // - Message dans une table "notifications"
        // - WebSocket
        System.out.println("⚠️ " + message);

        // TODO: Sauvegarder dans une table Notification
    }

    private void envoyerNotificationAlerte(Stock stock) {
        String message = String.format(
                "🟠 ALERTE STOCK : %s (lot %s) - Quantité: %d (seuil: %d)",
                stock.getArticle().getDesignation(),
                stock.getLot(),
                stock.getQuantite(),
                SEUIL_ALERTE
        );
        System.out.println("ℹ️ " + message);
    }

    public List<Stock> getStocksCritiques() {
        return stockRepository.findAll().stream()
                .filter(s -> s.getQuantite() <= SEUIL_CRITIQUE)
                .collect(Collectors.toList());
    }

    public List<Stock> getStocksAlerte() {
        return stockRepository.findAll().stream()
                .filter(s -> s.getQuantite() > SEUIL_CRITIQUE && s.getQuantite() <= SEUIL_ALERTE)
                .collect(Collectors.toList());
    }
}