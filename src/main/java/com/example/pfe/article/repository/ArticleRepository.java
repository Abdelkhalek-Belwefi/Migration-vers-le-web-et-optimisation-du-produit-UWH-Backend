package com.example.pfe.article.repository;

import com.example.pfe.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByCode(String code);
    Optional<Article> findByCodeArticleERP(String codeArticleERP);
    Optional<Article> findByGtin(String gtin);

    Optional<Article> findByNumSerie(String numSerie);

    List<Article> findByCategory(String category);

    List<Article> findByDesignationContainingIgnoreCase(String designation);

    List<Article> findByActifTrue();

    boolean existsByCodeArticleERP(String codeArticleERP);

    boolean existsByGtin(String gtin);

    boolean existsByNumSerie(String numSerie);

    @Query("SELECT a FROM Article a WHERE " +
            "(:code IS NULL OR a.codeArticleERP LIKE %:code% OR a.gtin LIKE %:code% OR a.numSerie LIKE %:code%) AND " +
            "(:designation IS NULL OR a.designation LIKE %:designation%) AND " +
            "(:category IS NULL OR a.category = :category) AND " +
            "(:actif IS NULL OR a.actif = :actif)")
    List<Article> searchArticles(@Param("code") String code,
                                 @Param("designation") String designation,
                                 @Param("category") String category,
                                 @Param("actif") Boolean actif);

    // La méthode deleteById est déjà fournie par JpaRepository
    // void deleteById(Long id);


    // Ajouter cette méthode dans l’interface ArticleRepository
    boolean existsByCategory(String category);


}