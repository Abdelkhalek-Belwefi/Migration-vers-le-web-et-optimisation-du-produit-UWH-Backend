package com.example.pfe.article.repository;

import com.example.pfe.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByCode(String code);
    Optional<Article> findByCodeArticleERP(String codeArticleERP);
    Optional<Article> findByGtin(String gtin);
}