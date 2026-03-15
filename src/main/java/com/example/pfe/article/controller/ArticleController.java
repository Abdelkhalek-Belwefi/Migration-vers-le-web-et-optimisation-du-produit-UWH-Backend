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

    @GetMapping
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    @GetMapping("/code/{codeERP}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> getArticleByCodeERP(@PathVariable String codeERP) {
        return ResponseEntity.ok(articleService.getArticleByCodeERP(codeERP));
    }

    @GetMapping("/gs1/{gs1Code}")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> findArticleByGS1Code(@PathVariable String gs1Code) {
        return ResponseEntity.ok(articleService.findArticleByGS1Code(gs1Code));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> createArticle(@RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(articleService.createArticle(articleDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> updateArticle(@PathVariable Long id, @RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(articleService.updateArticle(id, articleDTO));
    }

    @PutMapping("/{id}/activer")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT')")
    public ResponseEntity<ArticleDTO> activerArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.activerArticle(id));
    }

    @PutMapping("/{id}/desactiver")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT')")
    public ResponseEntity<ArticleDTO> desactiverArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.desactiverArticle(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_ENTREPOT', 'OPERATEUR_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<ArticleDTO>> searchArticles(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean actif) {
        return ResponseEntity.ok(articleService.searchArticles(code, designation, category, actif));
    }

    @PostMapping("/sync")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ArticleDTO> synchroniserDepuisERP(@RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(articleService.synchroniserDepuisERP(articleDTO));
    }
}