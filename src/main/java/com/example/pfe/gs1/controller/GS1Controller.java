package com.example.pfe.gs1.controller;

import com.example.pfe.gs1.dto.GS1DataDTO;
import com.example.pfe.gs1.service.GS1DecoderService;  // ← Nom modifié
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gs1")
@CrossOrigin(origins = "http://localhost:5173")
public class GS1Controller {

    private final GS1DecoderService gs1DecoderService;  // ← Nom modifié

    public GS1Controller(GS1DecoderService gs1DecoderService) {
        this.gs1DecoderService = gs1DecoderService;
    }

    @GetMapping("/decode/{code}")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<GS1DataDTO> decodeGS1(@PathVariable String code) {
        System.out.println("🔍 Décodage GS1 reçu: " + code);
        GS1DataDTO result = gs1DecoderService.decodeGS1(code);
        return ResponseEntity.ok(result);
    }
}