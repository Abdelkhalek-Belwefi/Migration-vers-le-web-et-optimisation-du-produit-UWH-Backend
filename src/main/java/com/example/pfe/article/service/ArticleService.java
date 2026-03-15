package com.example.pfe.article.service;

import com.example.pfe.article.dto.ArticleDTO;
import com.example.pfe.article.entity.Article;
import com.example.pfe.article.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public List<ArticleDTO> getAllArticles() {
        List<Article> articles = articleRepository.findAll();
        return articles.stream()
                .map(ArticleDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ArticleDTO createArticle(ArticleDTO articleDTO) {
        // Vérifier si le code ERP existe déjà
        if (articleRepository.existsByCodeArticleERP(articleDTO.getCodeArticleERP())) {
            throw new RuntimeException("Un article avec ce code ERP existe déjà");
        }

        // Vérifier si le GTIN existe déjà (si fourni)
        if (articleDTO.getGtin() != null && articleRepository.existsByGtin(articleDTO.getGtin())) {
            throw new RuntimeException("Un article avec ce GTIN existe déjà");
        }

        // Vérifier si le numéro de série existe déjà (si fourni)
        if (articleDTO.getNumSerie() != null && !articleDTO.getNumSerie().isEmpty()
                && articleRepository.existsByNumSerie(articleDTO.getNumSerie())) {
            throw new RuntimeException("Un article avec ce numéro de série existe déjà");
        }

        Article article = new Article();
        article.setCodeArticleERP(articleDTO.getCodeArticleERP());
        article.setGtin(articleDTO.getGtin());
        article.setNumSerie(articleDTO.getNumSerie());
        article.setDesignation(articleDTO.getDesignation());
        article.setDescription(articleDTO.getDescription());
        article.setCategory(articleDTO.getCategory());
        article.setUniteMesure(articleDTO.getUniteMesure());
        article.setPoids(articleDTO.getPoids());
        article.setVolume(articleDTO.getVolume());
        article.setLotDefaut(articleDTO.getLotDefaut());
        article.setDureeExpirationJours(articleDTO.getDureeExpirationJours());
        article.setActif(articleDTO.isActif());

        Article savedArticle = articleRepository.save(article);
        return new ArticleDTO(savedArticle);
    }

    @Transactional(readOnly = true)
    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'id: " + id));
        return new ArticleDTO(article);
    }

    // 🔴 AJOUT DE LA MÉTHODE MANQUANTE
    @Transactional(readOnly = true)
    public ArticleDTO getArticleByCodeERP(String codeERP) {
        Article article = articleRepository.findByCodeArticleERP(codeERP)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec le code ERP: " + codeERP));
        return new ArticleDTO(article);
    }

    @Transactional
    public ArticleDTO updateArticle(Long id, ArticleDTO articleDTO) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'id: " + id));

        article.setCodeArticleERP(articleDTO.getCodeArticleERP());
        article.setGtin(articleDTO.getGtin());
        article.setNumSerie(articleDTO.getNumSerie());
        article.setDesignation(articleDTO.getDesignation());
        article.setDescription(articleDTO.getDescription());
        article.setCategory(articleDTO.getCategory());
        article.setUniteMesure(articleDTO.getUniteMesure());
        article.setPoids(articleDTO.getPoids());
        article.setVolume(articleDTO.getVolume());
        article.setLotDefaut(articleDTO.getLotDefaut());
        article.setDureeExpirationJours(articleDTO.getDureeExpirationJours());
        article.setActif(articleDTO.isActif());

        Article updatedArticle = articleRepository.save(article);
        return new ArticleDTO(updatedArticle);
    }

    @Transactional
    public ArticleDTO activerArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'id: " + id));
        article.setActif(true);
        return new ArticleDTO(articleRepository.save(article));
    }

    @Transactional
    public ArticleDTO desactiverArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'id: " + id));
        article.setActif(false);
        return new ArticleDTO(articleRepository.save(article));
    }

    @Transactional(readOnly = true)
    public List<ArticleDTO> searchArticles(String code, String designation, String category, Boolean actif) {
        List<Article> articles = articleRepository.searchArticles(code, designation, category, actif);
        return articles.stream()
                .map(ArticleDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArticleDTO findArticleByGS1Code(String gs1Code) {
        // Extraction du GTIN (si le code contient AI 01)
        String gtin = gs1Code;
        if (gs1Code.contains("01")) {
            int startIndex = gs1Code.indexOf("01") + 2;
            if (startIndex + 14 <= gs1Code.length()) {
                gtin = gs1Code.substring(startIndex, startIndex + 14);
            }
        }

        Article article = articleRepository.findByGtin(gtin)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec le code GS1: " + gs1Code));
        return new ArticleDTO(article);
    }

    @Transactional
    public ArticleDTO synchroniserDepuisERP(ArticleDTO articleDTO) {
        Article article = articleRepository.findByCodeArticleERP(articleDTO.getCodeArticleERP())
                .orElse(new Article());

        article.setCodeArticleERP(articleDTO.getCodeArticleERP());
        article.setGtin(articleDTO.getGtin());
        article.setNumSerie(articleDTO.getNumSerie());
        article.setDesignation(articleDTO.getDesignation());
        article.setDescription(articleDTO.getDescription());
        article.setCategory(articleDTO.getCategory());
        article.setUniteMesure(articleDTO.getUniteMesure());
        article.setPoids(articleDTO.getPoids());
        article.setVolume(articleDTO.getVolume());
        article.setLotDefaut(articleDTO.getLotDefaut());
        article.setDureeExpirationJours(articleDTO.getDureeExpirationJours());
        article.setActif(articleDTO.isActif());

        return new ArticleDTO(articleRepository.save(article));
    }

    // 🔴 MÉTHODE DELETE CORRIGÉE
    @Transactional
    public void deleteArticle(Long id) {
        try {
            System.out.println("=== SUPPRESSION ARTICLE ===");
            System.out.println("Tentative de suppression de l'article avec l'ID: " + id);

            // Vérifier si l'article existe
            if (!articleRepository.existsById(id)) {
                System.out.println("❌ Article non trouvé avec l'ID: " + id);
                throw new RuntimeException("Article non trouvé avec l'id: " + id);
            }

            // Supprimer l'article
            articleRepository.deleteById(id);

            System.out.println("✅ Article supprimé avec succès, ID: " + id);
            System.out.println("=== FIN SUPPRESSION ===");

        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la suppression: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression de l'article: " + e.getMessage());
        }
    }
}