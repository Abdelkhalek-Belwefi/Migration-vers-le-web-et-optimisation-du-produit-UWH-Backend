package com.example.pfe.article.controller;

import com.example.pfe.article.dto.ArticleDTO;
import com.example.pfe.article.service.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "http://localhost:5173")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * GET /api/articles
     * Récupère tous les articles
     * Accessible à : ADMINISTRATEUR, RESPONSABLE_ENTREPOT, OPERATEUR_ENTREPOT
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    /**
     * GET /api/articles/{id}
     * Récupère un article par son ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    /**
     * GET /api/articles/code/{codeERP}
     * Récupère un article par son code ERP
     */
    @GetMapping("/code/{codeERP}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> getArticleByCodeERP(@PathVariable String codeERP) {
        return ResponseEntity.ok(articleService.getArticleByCodeERP(codeERP));
    }

    /**
     * GET /api/articles/gs1/{gs1Code}
     * Récupère un article par code GS1 (code-barres)
     * @param gs1Code Le code GS1 scanné
     */
    @GetMapping("/gs1/{gs1Code}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> findArticleByGS1Code(@PathVariable String gs1Code) {
        return ResponseEntity.ok(articleService.findArticleByGS1Code(gs1Code));
    }

    /**
     * POST /api/articles
     * Crée un nouvel article
     * Accessible à : ADMINISTRATEUR uniquement
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> createArticle(@RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(articleService.createArticle(articleDTO));
    }

    /**
     * PUT /api/articles/{id}
     * Met à jour un article existant
     * Accessible à : ADMINISTRATEUR uniquement
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> updateArticle(@PathVariable Long id, @RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(articleService.updateArticle(id, articleDTO));
    }

    /**
     * PUT /api/articles/{id}/activer
     * Active un article
     * Accessible à : ADMINISTRATEUR et RESPONSABLE_ENTREPOT
     * Correction : Changé de hasAuthority à hasAnyAuthority
     */
    @PutMapping("/{id}/activer")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT')")
    public ResponseEntity<ArticleDTO> activerArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.activerArticle(id));
    }

    /**
     * PUT /api/articles/{id}/desactiver
     * Désactive un article
     * Accessible à : ADMINISTRATEUR et RESPONSABLE_ENTREPOT
     * Correction : Changé de hasAuthority à hasAnyAuthority
     */
    @PutMapping("/{id}/desactiver")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT')")
    public ResponseEntity<ArticleDTO> desactiverArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.desactiverArticle(id));
    }

    /**
     * GET /api/articles/search
     * Recherche avancée d'articles avec filtres
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<ArticleDTO>> searchArticles(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean actif) {
        return ResponseEntity.ok(articleService.searchArticles(code, designation, category, actif));
    }

    /**
     * POST /api/articles/sync
     * Synchronisation depuis l'ERP externe
     * Accessible publiquement pour l'ERP (à sécuriser avec clé API)
     */
    @PostMapping("/sync")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ArticleDTO> synchroniserDepuisERP(@RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(articleService.synchroniserDepuisERP(articleDTO));
    }
}