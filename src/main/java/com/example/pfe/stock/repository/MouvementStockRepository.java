package com.example.pfe.stock.repository;

import com.example.pfe.stock.entity.MouvementStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    List<MouvementStock> findByStockSourceId(Long stockId);

    List<MouvementStock> findByStockDestinationId(Long stockId);

    List<MouvementStock> findByType(String type);

    List<MouvementStock> findByDateMouvementBetween(LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT m FROM MouvementStock m WHERE " +
            "(:articleId IS NULL OR m.stockSource.article.id = :articleId) AND " +
            "(:type IS NULL OR m.type = :type) AND " +
            "(:motif IS NULL OR m.motif = :motif)")
    List<MouvementStock> searchMouvements(@Param("articleId") Long articleId,
                                          @Param("type") String type,
                                          @Param("motif") String motif);

    // ========== NOUVELLE MÉTHODE POUR LE FILTRAGE PAR ENTREPÔT ==========

    /**
     * Récupère tous les mouvements liés à un entrepôt spécifique
     * (via le stock source ou le stock destination)
     */
    @Query("SELECT m FROM MouvementStock m WHERE " +
            "(:entrepotId IS NULL OR " +
            "m.stockSource.entrepot.id = :entrepotId OR " +
            "m.stockDestination.entrepot.id = :entrepotId)")
    List<MouvementStock> findByEntrepotId(@Param("entrepotId") Long entrepotId);
}