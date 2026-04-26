package com.example.pfe.stock.service;

import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.stock.dto.MouvementStockDTO;
import com.example.pfe.stock.entity.MouvementStock;
import com.example.pfe.stock.entity.Stock;
import com.example.pfe.stock.entity.StockStatut;
import com.example.pfe.stock.repository.MouvementStockRepository;
import com.example.pfe.stock.repository.StockRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MouvementStockService {

    private final MouvementStockRepository mouvementRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;

    public MouvementStockService(MouvementStockRepository mouvementRepository,
                                 StockRepository stockRepository,
                                 UserRepository userRepository) {
        this.mouvementRepository = mouvementRepository;
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
    }

    // ========== MÉTHODES EXISTANTES (INCHANGÉES) ==========

    @Transactional
    public MouvementStockDTO entreeStock(Long stockId, int quantite, String motif, String commentaire) {
        System.out.println("=== ENTREE STOCK ===");

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé avec l'id: " + stockId));

        int ancienneQuantite = stock.getQuantite();
        stock.setQuantite(ancienneQuantite + quantite);
        stockRepository.save(stock);

        return enregistrerMouvement(stock, null, "ENTREE", quantite,
                ancienneQuantite, stock.getQuantite(), null, null, motif, commentaire);
    }

    @Transactional
    public MouvementStockDTO sortieStock(Long stockId, int quantite, String motif, String commentaire) {
        System.out.println("=== SORTIE STOCK ===");

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé avec l'id: " + stockId));

        if (stock.getQuantite() < quantite) {
            throw new RuntimeException("Stock insuffisant. Disponible: " + stock.getQuantite());
        }

        int ancienneQuantite = stock.getQuantite();
        stock.setQuantite(ancienneQuantite - quantite);
        stockRepository.save(stock);

        return enregistrerMouvement(stock, null, "SORTIE", quantite,
                ancienneQuantite, stock.getQuantite(), null, null, motif, commentaire);
    }

    // ========== MÉTHODE TRANSFERT CORRIGÉE ==========

    @Transactional
    public MouvementStockDTO transfererStock(Long stockIdSource, String emplacementDestination,
                                             int quantite, String motif, String commentaire) {

        System.out.println("=== TRANSFERT STOCK ===");

        Stock stockSource = stockRepository.findById(stockIdSource)
                .orElseThrow(() -> new RuntimeException("Stock source non trouvé"));

        if (stockSource.getStatut() == StockStatut.BLOQUE) {
            throw new RuntimeException("Impossible de transférer un stock bloqué");
        }

        if (stockSource.getQuantite() < quantite) {
            throw new RuntimeException("Quantité insuffisante. Disponible: " + stockSource.getQuantite());
        }

        Stock stockDestination = stockRepository.findByLot(stockSource.getLot())
                .stream()
                .filter(s -> s.getArticle().getId().equals(stockSource.getArticle().getId())
                        && s.getEmplacement().equalsIgnoreCase(emplacementDestination))
                .findFirst()
                .orElse(null);

        int ancienneQteSource = stockSource.getQuantite();
        Integer ancienneQteDest = null;

        stockSource.setQuantite(ancienneQteSource - quantite);
        stockRepository.save(stockSource);

        if (stockDestination == null) {
            stockDestination = new Stock();
            stockDestination.setArticle(stockSource.getArticle());
            stockDestination.setLot(stockSource.getLot());
            stockDestination.setEmplacement(emplacementDestination.toUpperCase());
            stockDestination.setQuantite(quantite);
            stockDestination.setStatut(stockSource.getStatut());
            stockDestination.setDateExpiration(stockSource.getDateExpiration());
            stockDestination.setDateReception(stockSource.getDateReception());
            stockDestination.setEntrepot(stockSource.getEntrepot());  // ← LIGNE AJOUTÉE
            stockRepository.save(stockDestination);
        } else {
            ancienneQteDest = stockDestination.getQuantite();
            stockDestination.setQuantite(ancienneQteDest + quantite);
            stockRepository.save(stockDestination);
        }

        return enregistrerMouvement(stockSource, stockDestination, "TRANSFERT", quantite,
                ancienneQteSource, stockSource.getQuantite(),
                ancienneQteDest, stockDestination.getQuantite(),
                motif, commentaire);
    }

    private MouvementStockDTO enregistrerMouvement(Stock source, Stock destination, String type,
                                                   int quantite,
                                                   int ancienneQteSource, int nouvelleQteSource,
                                                   Integer ancienneQteDest, Integer nouvelleQteDest,
                                                   String motif, String commentaire) {

        User utilisateur = getCurrentUser();

        MouvementStock mouvement = new MouvementStock();
        mouvement.setStockSource(source);
        mouvement.setStockDestination(destination);
        mouvement.setType(type);
        mouvement.setQuantite(quantite);
        mouvement.setAncienneQuantiteSource(ancienneQteSource);
        mouvement.setNouvelleQuantiteSource(nouvelleQteSource);
        mouvement.setAncienneQuantiteDestination(ancienneQteDest);
        mouvement.setNouvelleQuantiteDestination(nouvelleQteDest);
        mouvement.setMotif(motif);
        mouvement.setUtilisateur(utilisateur);
        mouvement.setCommentaire(commentaire);

        MouvementStock saved = mouvementRepository.save(mouvement);
        return convertToDTO(saved);
    }

    public List<MouvementStockDTO> getMouvementsByStock(Long stockId) {
        List<MouvementStock> mouvements = mouvementRepository.findByStockSourceId(stockId);
        mouvements.addAll(mouvementRepository.findByStockDestinationId(stockId));
        return mouvements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========== MÉTHODE MODIFIÉE (FILTRAGE PAR ENTREPÔT) ==========

    public List<MouvementStockDTO> getAllMouvements() {
        Long entrepotId = getCurrentUserEntrepotId();
        List<MouvementStock> mouvements;

        if (entrepotId != null) {
            mouvements = mouvementRepository.findByEntrepotId(entrepotId);
        } else {
            mouvements = mouvementRepository.findAll();
        }

        return mouvements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========== NOUVELLES MÉTHODES POUR LE FILTRAGE ==========

    private User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                String email = ((UserDetails) principal).getUsername();
                return userRepository.findByEmail(email)
                        .orElse(null);
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

    public List<MouvementStockDTO> getMouvementsByStockFiltered(Long stockId) {
        Long entrepotId = getCurrentUserEntrepotId();

        List<MouvementStock> mouvements = mouvementRepository.findByStockSourceId(stockId);
        mouvements.addAll(mouvementRepository.findByStockDestinationId(stockId));

        if (entrepotId != null) {
            mouvements = mouvements.stream()
                    .filter(m -> (m.getStockSource() != null && m.getStockSource().getEntrepot() != null
                            && m.getStockSource().getEntrepot().getId().equals(entrepotId)) ||
                            (m.getStockDestination() != null && m.getStockDestination().getEntrepot() != null
                                    && m.getStockDestination().getEntrepot().getId().equals(entrepotId)))
                    .collect(Collectors.toList());
        }

        return mouvements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========== CONVERSION (INCHANGÉE) ==========

    private MouvementStockDTO convertToDTO(MouvementStock mouvement) {
        MouvementStockDTO dto = new MouvementStockDTO();
        dto.setId(mouvement.getId());

        dto.setStockSourceId(mouvement.getStockSource().getId());
        dto.setArticleDesignation(mouvement.getStockSource().getArticle().getDesignation());
        dto.setArticleCode(mouvement.getStockSource().getArticle().getCodeArticleERP());
        dto.setLotSource(mouvement.getStockSource().getLot());
        dto.setEmplacementSource(mouvement.getStockSource().getEmplacement());

        if (mouvement.getStockDestination() != null) {
            dto.setStockDestinationId(mouvement.getStockDestination().getId());
            dto.setEmplacementDestination(mouvement.getStockDestination().getEmplacement());
            dto.setLotDestination(mouvement.getStockDestination().getLot());
        }

        dto.setType(mouvement.getType());
        dto.setQuantite(mouvement.getQuantite());
        dto.setAncienneQuantiteSource(mouvement.getAncienneQuantiteSource());
        dto.setNouvelleQuantiteSource(mouvement.getNouvelleQuantiteSource());

        if (mouvement.getAncienneQuantiteDestination() != null) {
            dto.setAncienneQuantiteDestination(mouvement.getAncienneQuantiteDestination());
        }
        if (mouvement.getNouvelleQuantiteDestination() != null) {
            dto.setNouvelleQuantiteDestination(mouvement.getNouvelleQuantiteDestination());
        }

        dto.setMotif(mouvement.getMotif());

        if (mouvement.getUtilisateur() != null) {
            dto.setUtilisateurNom(mouvement.getUtilisateur().getNom() + " " +
                    mouvement.getUtilisateur().getPrenom());
        }

        dto.setDateMouvement(mouvement.getDateMouvement());
        dto.setCommentaire(mouvement.getCommentaire());
        return dto;
    }
}