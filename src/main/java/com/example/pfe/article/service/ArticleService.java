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
    private final GS1ParserService gs1ParserService;

    public ArticleService(ArticleRepository articleRepository,
                          GS1ParserService gs1ParserService) {
        this.articleRepository = articleRepository;
        this.gs1ParserService = gs1ParserService;
    }

    // ========== MÉTHODES DE BASE ==========

    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'id: " + id));
        return convertToDTO(article);
    }

    public ArticleDTO getArticleByCodeERP(String codeERP) {
        Article article = articleRepository.findByCodeArticleERP(codeERP)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec le code: " + codeERP));
        return convertToDTO(article);
    }

    // ========== MÉTHODE RECHERCHE PAR GTIN ==========

    public ArticleDTO findArticleByGTIN(String gtin) {
        Article article = articleRepository.findByGtin(gtin)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec le GTIN: " + gtin));
        return convertToDTO(article);
    }

    // ========== MÉTHODE GS1 ==========

    /**
     * Recherche un article par code GS1
     * @param gs1Code Le code GS1 scanné
     * @return L'article correspondant
     */
    public ArticleDTO findArticleByGS1Code(String gs1Code) {
        // Parser le code GS1 pour extraire le GTIN
        GS1ParserService.GS1Data gs1Data = gs1ParserService.parseGS1Code(gs1Code);

        Article article = null;

        // Chercher d'abord par GTIN
        if (gs1Data.getGtin() != null && !gs1Data.getGtin().isEmpty()) {
            article = articleRepository.findByGtin(gs1Data.getGtin()).orElse(null);
        }

        // Si pas trouvé par GTIN, chercher par code ERP
        if (article == null) {
            article = articleRepository.findByCodeArticleERP(gs1Code).orElse(null);
        }

        if (article == null) {
            throw new RuntimeException("Article non trouvé pour le code GS1: " + gs1Code);
        }

        return convertToDTO(article);
    }

    // ========== MÉTHODES DE CRÉATION ==========

    @Transactional
    public ArticleDTO createArticle(ArticleDTO articleDTO) {
        // Vérifier si le code ERP existe déjà
        if (articleRepository.existsByCodeArticleERP(articleDTO.getCodeArticleERP())) {
            throw new RuntimeException("Un article avec ce code ERP existe déjà");
        }

        // Vérifier si le GTIN existe déjà (s'il est fourni)
        if (articleDTO.getGtin() != null && !articleDTO.getGtin().isEmpty()) {
            if (articleRepository.existsByGtin(articleDTO.getGtin())) {
                throw new RuntimeException("Un article avec ce GTIN existe déjà");
            }
        }

        Article article = convertToEntity(articleDTO);
        Article savedArticle = articleRepository.save(article);
        return convertToDTO(savedArticle);
    }

    // ========== MÉTHODES DE MISE À JOUR ==========

    @Transactional
    public ArticleDTO updateArticle(Long id, ArticleDTO articleDTO) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        // Vérifier si le nouveau code ERP est déjà utilisé par un autre article
        if (!article.getCodeArticleERP().equals(articleDTO.getCodeArticleERP()) &&
                articleRepository.existsByCodeArticleERP(articleDTO.getCodeArticleERP())) {
            throw new RuntimeException("Un autre article avec ce code ERP existe déjà");
        }

        // Vérifier si le nouveau GTIN est déjà utilisé par un autre article
        if (articleDTO.getGtin() != null && !articleDTO.getGtin().isEmpty() &&
                !articleDTO.getGtin().equals(article.getGtin()) &&
                articleRepository.existsByGtin(articleDTO.getGtin())) {
            throw new RuntimeException("Un autre article avec ce GTIN existe déjà");
        }

        updateArticleFromDTO(article, articleDTO);
        Article updatedArticle = articleRepository.save(article);
        return convertToDTO(updatedArticle);
    }

    // ========== MÉTHODES D'ACTIVATION/DÉSACTIVATION ==========

    @Transactional
    public ArticleDTO activerArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        article.setActif(true);
        Article updatedArticle = articleRepository.save(article);
        return convertToDTO(updatedArticle);
    }

    @Transactional
    public ArticleDTO desactiverArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        article.setActif(false);
        Article updatedArticle = articleRepository.save(article);
        return convertToDTO(updatedArticle);
    }

    // ========== MÉTHODES DE SUPPRESSION ==========

    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new RuntimeException("Article non trouvé");
        }
        articleRepository.deleteById(id);
    }

    // ========== RECHERCHE AVANCÉE ==========

    public List<ArticleDTO> searchArticles(String code, String designation, String category, Boolean actif) {
        return articleRepository.searchArticles(code, designation, category, actif).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========== SYNCHRONISATION ERP ==========

    @Transactional
    public ArticleDTO synchroniserDepuisERP(ArticleDTO articleDTO) {
        // Si un GTIN est fourni, chercher par GTIN d'abord
        if (articleDTO.getGtin() != null && !articleDTO.getGtin().isEmpty()) {
            return articleRepository.findByGtin(articleDTO.getGtin())
                    .map(article -> {
                        updateArticleFromDTO(article, articleDTO);
                        return convertToDTO(articleRepository.save(article));
                    })
                    .orElseGet(() -> {
                        // Créer un nouvel article
                        Article newArticle = convertToEntity(articleDTO);
                        return convertToDTO(articleRepository.save(newArticle));
                    });
        }

        // Sinon chercher par code ERP
        return articleRepository.findByCodeArticleERP(articleDTO.getCodeArticleERP())
                .map(article -> {
                    updateArticleFromDTO(article, articleDTO);
                    return convertToDTO(articleRepository.save(article));
                })
                .orElseGet(() -> {
                    Article newArticle = convertToEntity(articleDTO);
                    return convertToDTO(articleRepository.save(newArticle));
                });
    }

    // ========== MÉTHODES DE CONVERSION ==========

    /**
     * Convertit une entité Article en DTO
     */
    private ArticleDTO convertToDTO(Article article) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(article.getId());
        dto.setCodeArticleERP(article.getCodeArticleERP());
        dto.setGtin(article.getGtin());
        dto.setDesignation(article.getDesignation());
        dto.setDescription(article.getDescription());
        dto.setCategory(article.getCategory());
        dto.setUniteMesure(article.getUniteMesure());
        dto.setPoids(article.getPoids());
        dto.setVolume(article.getVolume());
        dto.setLotDefaut(article.getLotDefaut());
        dto.setDureeExpirationJours(article.getDureeExpirationJours());
        dto.setActif(article.isActif());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        return dto;
    }

    /**
     * Convertit un DTO en entité Article
     */
    private Article convertToEntity(ArticleDTO dto) {
        Article article = new Article();
        article.setCodeArticleERP(dto.getCodeArticleERP());
        article.setGtin(dto.getGtin());
        article.setDesignation(dto.getDesignation());
        article.setDescription(dto.getDescription());
        article.setCategory(dto.getCategory());
        article.setUniteMesure(dto.getUniteMesure());
        article.setPoids(dto.getPoids());
        article.setVolume(dto.getVolume());
        article.setLotDefaut(dto.getLotDefaut());
        article.setDureeExpirationJours(dto.getDureeExpirationJours());
        article.setActif(dto.isActif());
        return article;
    }

    /**
     * Met à jour une entité Article à partir d'un DTO
     */
    private void updateArticleFromDTO(Article article, ArticleDTO dto) {
        article.setCodeArticleERP(dto.getCodeArticleERP());
        article.setGtin(dto.getGtin());
        article.setDesignation(dto.getDesignation());
        article.setDescription(dto.getDescription());
        article.setCategory(dto.getCategory());
        article.setUniteMesure(dto.getUniteMesure());
        article.setPoids(dto.getPoids());
        article.setVolume(dto.getVolume());
        article.setLotDefaut(dto.getLotDefaut());
        article.setDureeExpirationJours(dto.getDureeExpirationJours());
        article.setActif(dto.isActif());
    }
}