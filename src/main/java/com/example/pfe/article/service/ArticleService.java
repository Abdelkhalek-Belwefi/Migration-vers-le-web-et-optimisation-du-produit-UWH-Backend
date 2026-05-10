package com.example.pfe.article.service;

import com.example.pfe.article.dto.ArticleDTO;
import com.example.pfe.article.entity.Article;
import com.example.pfe.article.repository.ArticleRepository;
import com.example.pfe.auth.entity.Role;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
import com.example.pfe.notification.enums.NotificationType;
import com.example.pfe.notification.service.NotificationService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final NotificationService notificationService;

    public ArticleService(ArticleRepository articleRepository,
                          UserRepository userRepository,
                          WarehouseRepository warehouseRepository,
                          NotificationService notificationService) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
        this.notificationService = notificationService;
    }

    // ========== MÉTHODES EXISTANTES (INCHANGÉES) ==========

    // @Cacheable(value = "articles")  ← SUPPRIMÉ (causait l'erreur 400)
    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(ArticleDTO::new)
                .collect(Collectors.toList());
    }

    //@Cacheable(value = "articles", key = "#id")
    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        return new ArticleDTO(article);
    }

    //@Cacheable(value = "articles", key = "#codeERP")
    public ArticleDTO getArticleByCodeERP(String codeERP) {
        Article article = articleRepository.findByCodeArticleERP(codeERP)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec le code ERP: " + codeERP));
        return new ArticleDTO(article);
    }

    //@Cacheable(value = "articles", key = "#gtin")
    public ArticleDTO getArticleByGTIN(String gtin) {
        Article article = articleRepository.findByGtin(gtin)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec le GTIN: " + gtin));
        return new ArticleDTO(article);
    }

    // ========== MÉTHODE CREATE ARTICLE CORRIGÉE (SANS ENTREPÔT OBLIGATOIRE) ==========

    @Transactional
    @CacheEvict(value = "articles", allEntries = true)
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

        // 🔔 NOTIFICATION : Nouvel article créé (pour tous les responsables entrepôt)
        try {
            List<User> responsables = userRepository.findByRole(Role.RESPONSABLE_ENTREPOT);
            for (User responsable : responsables) {
                notificationService.createNotification(
                        responsable.getId(),
                        "📝 Nouvel article créé",
                        String.format("Un nouvel article a été créé : %s (Code: %s)",
                                saved.getDesignation(), saved.getCodeArticleERP()),
                        NotificationType.INFO,
                        "/dashboard?tab=articles",
                        saved.getId(),
                        "ARTICLE"
                );
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }

        return new ArticleDTO(saved);
    }

    @Transactional
    @CacheEvict(value = "articles", allEntries = true)
    public ArticleDTO updateArticle(Long id, ArticleDTO dto) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        String ancienneDesignation = article.getDesignation();

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

        // 🔔 NOTIFICATION : Article modifié (pour tous les responsables entrepôt)
        try {
            List<User> responsables = userRepository.findByRole(Role.RESPONSABLE_ENTREPOT);
            for (User responsable : responsables) {
                notificationService.createNotification(
                        responsable.getId(),
                        "✏️ Article modifié",
                        String.format("L'article '%s' a été modifié. Nouvelle désignation: %s",
                                ancienneDesignation, updated.getDesignation()),
                        NotificationType.INFO,
                        "/dashboard?tab=articles",
                        updated.getId(),
                        "ARTICLE"
                );
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }

        return new ArticleDTO(updated);
    }

    @Transactional
    @CacheEvict(value = "articles", allEntries = true)
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id).orElse(null);
        articleRepository.deleteById(id);

        // 🔔 NOTIFICATION : Article supprimé (pour tous les responsables entrepôt)
        if (article != null) {
            try {
                List<User> responsables = userRepository.findByRole(Role.RESPONSABLE_ENTREPOT);
                for (User responsable : responsables) {
                    notificationService.createNotification(
                            responsable.getId(),
                            "🗑️ Article supprimé",
                            String.format("L'article '%s' (Code: %s) a été supprimé du catalogue.",
                                    article.getDesignation(), article.getCodeArticleERP()),
                            NotificationType.ERREUR,
                            "/dashboard?tab=articles",
                            id,
                            "ARTICLE"
                    );
                }
            } catch (Exception e) {
                System.out.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
            }
        }
    }

    @Transactional
    @CacheEvict(value = "articles", allEntries = true)
    public ArticleDTO activerArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        article.setActif(true);
        article.setUpdatedAt(java.time.LocalDateTime.now());
        Article updated = articleRepository.save(article);

        // 🔔 NOTIFICATION : Article activé
        try {
            List<User> responsables = userRepository.findByRole(Role.RESPONSABLE_ENTREPOT);
            for (User responsable : responsables) {
                notificationService.createNotification(
                        responsable.getId(),
                        "🟢 Article activé",
                        String.format("L'article '%s' a été réactivé et est de nouveau disponible.",
                                updated.getDesignation()),
                        NotificationType.SUCCES,
                        "/dashboard?tab=articles",
                        updated.getId(),
                        "ARTICLE"
                );
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }

        return new ArticleDTO(updated);
    }

    @Transactional
    @CacheEvict(value = "articles", allEntries = true)
    public ArticleDTO desactiverArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        article.setActif(false);
        article.setUpdatedAt(java.time.LocalDateTime.now());
        Article updated = articleRepository.save(article);

        // 🔔 NOTIFICATION : Article désactivé
        try {
            List<User> responsables = userRepository.findByRole(Role.RESPONSABLE_ENTREPOT);
            for (User responsable : responsables) {
                notificationService.createNotification(
                        responsable.getId(),
                        "🔴 Article désactivé",
                        String.format("L'article '%s' a été désactivé et n'est plus disponible à la vente.",
                                updated.getDesignation()),
                        NotificationType.ALERTE,
                        "/dashboard?tab=articles",
                        updated.getId(),
                        "ARTICLE"
                );
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }

        return new ArticleDTO(updated);
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