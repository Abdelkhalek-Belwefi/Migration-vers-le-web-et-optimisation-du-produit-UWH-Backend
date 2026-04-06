package com.example.pfe.ocr.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrService {

    private final Tesseract tesseract;
    private final String tessdataPath;

    public OcrService(@Value("${tesseract.datapath}") String tessdataPath) {
        this.tessdataPath = tessdataPath;
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(tessdataPath);
        this.tesseract.setLanguage("fra+eng");
        this.tesseract.setPageSegMode(6);
        this.tesseract.setVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-:/");
        System.out.println("✅ Tesseract initialisé avec datapath: " + tessdataPath);
    }

    public Map<String, String> extractDocumentInfo(MultipartFile file) throws IOException, TesseractException {
        System.out.println("=== DÉBUT OCR ===");
        System.out.println("Nom fichier: " + file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }

        Path tempFile = Files.createTempFile("ocr_", "_" + fileName);
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        String extractedText;
        String fileType = fileName.toLowerCase();

        try {
            if (fileType.endsWith(".pdf")) {
                extractedText = extractTextFromPdf(tempFile.toFile());
            } else {
                extractedText = extractTextFromImage(tempFile.toFile());
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }

        System.out.println("=== TEXTE EXTRAIT PAR OCR ===");
        System.out.println(extractedText);

        Map<String, String> result = new HashMap<>();

        // ===== RECONSTRUCTION DES NUMÉROS COMPLETS (CORRIGÉ) =====

        // 1. Détection du Numéro PO (cherche PO- suivi de chiffres ou tirets)
        Pattern poPattern = Pattern.compile("PO-?([0-9\\-]+)");
        Matcher poMatcher = poPattern.matcher(extractedText);
        if (poMatcher.find()) {
            result.put("numeroPO", poMatcher.group(0)); // group(0) récupère "PO-XXXX"
        }

        // 2. Détection du Bon de Livraison (cherche BL- suivi de chiffres ou tirets)
        Pattern blPattern = Pattern.compile("BL-?([0-9\\-]+)");
        Matcher blMatcher = blPattern.matcher(extractedText);
        if (blMatcher.find()) {
            result.put("bonLivraison", blMatcher.group(0)); // group(0) récupère "BL-XXXX"
        }

        // 3. Fournisseur - corriger "LOGISTIQUEEXPRESS"
        String[] lignes = extractedText.split("\n");
        for (String ligne : lignes) {
            ligne = ligne.trim();
            if (ligne.contains("LOGISTIQUE") || ligne.contains("EXPRESS") || ligne.contains("DUPONT")) {
                String fournisseur = ligne.replaceAll("FOURNISSEUR\\s*:?\\s*", "").trim();
                fournisseur = fournisseur.replaceAll("([A-Z])([A-Z][a-z])", "$1 $2");
                fournisseur = fournisseur.replaceAll("([a-z])([A-Z])", "$1 $2");
                result.put("fournisseur", fournisseur);
                break;
            }
        }

        // 4. Date
        Pattern datePattern = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");
        Matcher dateMatcher = datePattern.matcher(extractedText);
        if (dateMatcher.find()) {
            result.put("dateReception", dateMatcher.group(1));
        }

        System.out.println("=== RÉSULTAT OCR ===");
        System.out.println(result);
        return result;
    }

    private String extractTextFromImage(File file) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Impossible de lire l'image");
        }
        return tesseract.doOCR(image);
    }

    private String extractTextFromPdf(File file) throws IOException, TesseractException {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            StringBuilder fullText = new StringBuilder();

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                String pageText = tesseract.doOCR(image);
                fullText.append(pageText).append("\n");
            }

            return fullText.toString();
        }
    }

    public Map<String, String> decodeDocumentBarcode(String barcode) {
        Map<String, String> result = new HashMap<>();
        if (barcode.startsWith("(01)")) {
            Pattern gtinPattern = Pattern.compile("\\(01\\)(\\d{14})");
            Matcher gtinMatcher = gtinPattern.matcher(barcode);
            if (gtinMatcher.find()) {
                result.put("gtin", gtinMatcher.group(1));
            }
        } else if (barcode.startsWith("PO-")) {
            result.put("numeroPO", barcode);
        } else if (barcode.startsWith("BL-")) {
            result.put("bonLivraison", barcode);
        } else {
            result.put("bonLivraison", barcode);
        }
        return result;
    }
}