package com.example.pfe.ocr.controller;

import com.example.pfe.ocr.service.OcrService;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
@CrossOrigin(origins = "http://localhost:5173")
public class OcrController {

    private final OcrService ocrService;

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/extract")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<?> extractDocumentInfo(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, String> extractedInfo = ocrService.extractDocumentInfo(file);
            return ResponseEntity.ok(extractedInfo);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IOException e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur de lecture du fichier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (TesseractException e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur OCR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur inattendue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/decode/{barcode}")
    @PreAuthorize("hasAnyAuthority('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<Map<String, String>> decodeDocumentBarcode(@PathVariable String barcode) {
        Map<String, String> decoded = ocrService.decodeDocumentBarcode(barcode);
        return ResponseEntity.ok(decoded);
    }

    // ========== NOUVEAU : Endpoint WebSocket pour recevoir image en base64 depuis mobile ==========
    @MessageMapping("/ocr/scan")
    @SendTo("/topic/ocr/result")
    public Map<String, String> processOcrFromMobile(String base64Image) {
        System.out.println("📱 Réception d'une image OCR depuis mobile");
        System.out.println("📏 Taille du base64: " + (base64Image != null ? base64Image.length() : 0));

        Map<String, String> response = new HashMap<>();

        try {
            // Nettoyer le message (enlever le préfixe "OCR:" si présent)
            String cleanBase64 = base64Image;
            if (base64Image != null && base64Image.startsWith("OCR:")) {
                cleanBase64 = base64Image.substring(4);
                System.out.println("🧹 Préfixe OCR: supprimé");
            }

            // Analyser l'image
            Map<String, String> extractedInfo = ocrService.extractDocumentInfoFromBase64(cleanBase64);

            // S'assurer que la date est présente (sinon mettre date du jour)
            if (extractedInfo.get("dateReception") == null || extractedInfo.get("dateReception").isEmpty()) {
                extractedInfo.put("dateReception", LocalDate.now().toString());
                System.out.println("📅 Date par défaut ajoutée: " + LocalDate.now());
            }

            response.putAll(extractedInfo);
            response.put("status", "SUCCESS");

            System.out.println("✅ OCR terminé avec succès");
            System.out.println("📊 Résultat final: " + response);

        } catch (Exception e) {
            System.err.println("❌ Erreur OCR: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        }

        return response;
    }
}