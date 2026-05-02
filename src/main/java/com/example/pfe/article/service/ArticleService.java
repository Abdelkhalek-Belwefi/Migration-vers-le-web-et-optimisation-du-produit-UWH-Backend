package com.example.pfe.article.service;

import com.example.pfe.article.dto.ArticleDTO;
import com.example.pfe.article.entity.Article;
import com.example.pfe.article.repository.ArticleRepository;
import com.example.pfe.auth.entity.Role;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;

    public ArticleService(ArticleRepository articleRepository,
                          UserRepository userRepository,
                          WarehouseRepository warehouseRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
    }

    // ========== MÉTHODES EXISTANTES (INCHANGÉES) ==========

    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(ArticleDTO::new)
                .collect(Collectors.toList());
    }

    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        return new ArticleDTO(article);
    }

    public ArticleDTO getArticleByCodeERP(String codeERP) {
        Article article = articleRepository.findByCodeArticleERP(codeERP)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec le code ERP: " + codeERP));
        return new ArticleDTO(article);
    }

    public ArticleDTO getArticleByGTIN(String gtin) {
        Article article = articleRepository.findByGtin(gtin)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec le GTIN: " + gtin));
        return new ArticleDTO(article);
    }

    // ========== MÉTHODE CREATE ARTICLE CORRIGÉE (SANS ENTREPÔT OBLIGATOIRE) ==========

    @Transactional
    public ArticleDTO createArticle(ArticleDTO dto) {
        // Récupérer l'utilisateur connecté
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        Article article = new Article();
        article.setCodeArticleERP(dto.getCodeArticleERP());
        article.setGtin(dto.getGtin());
        article.setNumSerie(dto.getNumSerie());
        article.setDesignation(dto.getDesignation());
        article.setDescription(dto.getDescription());
        article.setCategory(dto.getCategory());
        article.setUniteMesure(dto.getUniteMesure());
        article.setPoids(dto.getPoids());
        article.setVolume(dto.getVolume());
        article.setLotDefaut(dto.getLotDefaut());
        article.setDureeExpirationJours(dto.getDureeExpirationJours());
        article.setActif(dto.isActif());
        article.setPrixUnitaire(dto.getPrixUnitaire());
        // L'entrepôt n'est plus assigné à l'article (nullable)

        Article saved = articleRepository.save(article);
        System.out.println("✅ Article créé avec succès (sans association d'entrepôt)");
        return new ArticleDTO(saved);
    }

    @Transactional
    public ArticleDTO updateArticle(Long id, ArticleDTO dto) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        article.setCodeArticleERP(dto.getCodeArticleERP());
        article.setGtin(dto.getGtin());
        article.setNumSerie(dto.getNumSerie());
        article.setDesignation(dto.getDesignation());
        article.setDescription(dto.getDescription());
        article.setCategory(dto.getCategory());
        article.setUniteMesure(dto.getUniteMesure());
        article.setPoids(dto.getPoids());
        article.setVolume(dto.getVolume());
        article.setLotDefaut(dto.getLotDefaut());
        article.setDureeExpirationJours(dto.getDureeExpirationJours());
        article.setActif(dto.isActif());
        article.setPrixUnitaire(dto.getPrixUnitaire());
        article.setUpdatedAt(java.time.LocalDateTime.now());
        Article updated = articleRepository.save(article);
        return new ArticleDTO(updated);
    }

    @Transactional
    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    @Transactional
    public ArticleDTO activerArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        article.setActif(true);
        article.setUpdatedAt(java.time.LocalDateTime.now());
        return new ArticleDTO(articleRepository.save(article));
    }

    @Transactional
    public ArticleDTO desactiverArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        article.setActif(false);
        article.setUpdatedAt(java.time.LocalDateTime.now());
        return new ArticleDTO(articleRepository.save(article));
    }

    // ========== MÉTHODE UTILITAIRE ==========

    private User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                String email = ((UserDetails) principal).getUsername();
                return userRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            System.out.println("Erreur récupération utilisateur: " + e.getMessage());
        }
        return null;
    }
}