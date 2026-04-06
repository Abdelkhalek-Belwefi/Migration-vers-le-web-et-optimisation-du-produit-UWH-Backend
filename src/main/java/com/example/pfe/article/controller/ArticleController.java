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
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT', 'SERVICE_COMMERCIAL')")
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT', 'SERVICE_COMMERCIAL')")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    @GetMapping("/code/{codeERP}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT', 'SERVICE_COMMERCIAL')")
    public ResponseEntity<ArticleDTO> getArticleByCodeERP(@PathVariable String codeERP) {
        return ResponseEntity.ok(articleService.getArticleByCodeERP(codeERP));
    }

    @GetMapping("/gs1/{gtin}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT', 'SERVICE_COMMERCIAL')")
    public ResponseEntity<ArticleDTO> getArticleByGTIN(@PathVariable String gtin) {
        return ResponseEntity.ok(articleService.getArticleByGTIN(gtin));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> createArticle(@RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(articleService.createArticle(articleDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> updateArticle(@PathVariable Long id, @RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(articleService.updateArticle(id, articleDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> activerArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.activerArticle(id));
    }

    @PatchMapping("/{id}/desactiver")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<ArticleDTO> desactiverArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.desactiverArticle(id));
    }
}