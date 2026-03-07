package com.example.pfe.stock.repository;

import com.example.pfe.stock.entity.Stock;
import com.example.pfe.stock.entity.StockStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByArticleId(Long articleId);
    List<Stock> findByEmplacementContainingIgnoreCase(String emplacement);
    Optional<Stock> findByLot(String lot);
    List<Stock> findByStatut(StockStatut statut);
    boolean existsByLot(String lot);

    @Query("SELECT s FROM Stock s WHERE " +
            "(:articleId IS NULL OR s.article.id = :articleId) AND " +
            "(:lot IS NULL OR s.lot LIKE %:lot%) AND " +
            "(:emplacement IS NULL OR s.emplacement LIKE %:emplacement%) AND " +
            "(:statut IS NULL OR s.statut = :statut)")
    List<Stock> searchStocks(@Param("articleId") Long articleId,
                             @Param("lot") String lot,
                             @Param("emplacement") String emplacement,
                             @Param("statut") StockStatut statut);

    @Query("SELECT SUM(s.quantite) FROM Stock s WHERE s.article.id = :articleId")
    Integer getTotalQuantiteByArticle(@Param("articleId") Long articleId);
}