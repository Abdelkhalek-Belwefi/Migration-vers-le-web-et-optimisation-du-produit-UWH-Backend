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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Base64;
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
        this.tesseract.setVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-/:,.");
        System.out.println("✅ Tesseract initialisé avec datapath: " + tessdataPath);
    }

    // ========== MÉTHODE EXISTANTE (POUR FICHIER UPLOAD) - INCHANGÉE ==========
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

        // 1. Détection du Bon de commande (CMD-XXXXXX)
        String commande = extractCommandeNumber(extractedText);
        if (commande != null && !commande.isEmpty()) {
            result.put("numeroPO", commande);
            System.out.println("✅ Bon de commande trouvé: " + commande);
        }

        // 2. Détection du Bon de livraison (BL-XXXXXX)
        String bl = extractBLNumber(extractedText);
        if (bl != null && !bl.isEmpty()) {
            result.put("bonLivraison", bl);
            System.out.println("✅ Bon de livraison trouvé: " + bl);
        }

        // 3. Détection de la Date
        String date = extractDate(extractedText);
        if (date != null && !date.isEmpty()) {
            result.put("dateReception", date);
            System.out.println("✅ Date trouvée: " + date);
        }

        // 4. Détection du Fournisseur (spécial transfert entre entrepôts)
        String fournisseur = extractFournisseur(extractedText);
        if (fournisseur != null && !fournisseur.isEmpty()) {
            result.put("fournisseur", fournisseur);
            System.out.println("✅ Fournisseur trouvé: " + fournisseur);
        }

        System.out.println("=== RÉSULTAT OCR FINAL ===");
        System.out.println(result);
        return result;
    }

    // ========== NOUVELLE MÉTHODE : Analyser image en base64 reçue du mobile ==========
    public Map<String, String> extractDocumentInfoFromBase64(String base64Image) throws IOException, TesseractException {
        System.out.println("=== DÉBUT OCR DEPUIS BASE64 (MOBILE) ===");
        System.out.println("📏 Taille base64: " + (base64Image != null ? base64Image.length() : 0));

        // Nettoyer le base64 (enlever les préfixes éventuels)
        String cleanBase64 = base64Image;
        if (base64Image != null && base64Image.contains(",")) {
            cleanBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
        }

        // Décoder le base64 en bytes
        byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);

        // Créer un fichier temporaire
        Path tempFile = Files.createTempFile("ocr_mobile_", ".jpg");
        Files.write(tempFile, imageBytes);

        String extractedText;

        try {
            extractedText = extractTextFromImage(tempFile.toFile());
        } finally {
            Files.deleteIfExists(tempFile);
        }

        System.out.println("=== TEXTE EXTRAIT PAR OCR (MOBILE) ===");
        System.out.println(extractedText);

        Map<String, String> result = new HashMap<>();

        // 1. Détection du Bon de commande (CMD-XXXXXX ou PO-XXXXXX)
        String commande = extractCommandeNumber(extractedText);
        if (commande != null && !commande.isEmpty()) {
            result.put("numeroPO", commande);
            System.out.println("✅ Bon de commande trouvé: " + commande);
        } else {
            // Fallback: chercher un numéro à 10-13 chiffres
            String fallbackCommande = extractCommandeNumberFallback(extractedText);
            if (fallbackCommande != null && !fallbackCommande.isEmpty()) {
                result.put("numeroPO", fallbackCommande);
                System.out.println("✅ Bon de commande (fallback) trouvé: " + fallbackCommande);
            }
        }

        // 2. Détection du Bon de livraison (BL-XXXXXX)
        String bl = extractBLNumber(extractedText);
        if (bl != null && !bl.isEmpty()) {
            result.put("bonLivraison", bl);
            System.out.println("✅ Bon de livraison trouvé: " + bl);
        } else {
            // Fallback: chercher BL suivi de chiffres
            String fallbackBL = extractBLNumberFallback(extractedText);
            if (fallbackBL != null && !fallbackBL.isEmpty()) {
                result.put("bonLivraison", fallbackBL);
                System.out.println("✅ Bon de livraison (fallback) trouvé: " + fallbackBL);
            }
        }

        // 3. Détection de la Date
        String date = extractDate(extractedText);
        if (date != null && !date.isEmpty()) {
            date = formatDateToISO(date);
            result.put("dateReception", date);
            System.out.println("✅ Date trouvée: " + date);
        } else {
            // Date par défaut = aujourd'hui
            result.put("dateReception", LocalDate.now().toString());
            System.out.println("✅ Date par défaut utilisée: " + LocalDate.now());
        }

        // 4. Détection du Fournisseur
        String fournisseur = extractFournisseur(extractedText);
        if (fournisseur != null && !fournisseur.isEmpty()) {
            result.put("fournisseur", fournisseur);
            System.out.println("✅ Fournisseur trouvé: " + fournisseur);
        }

        System.out.println("=== RÉSULTAT OCR MOBILE FINAL ===");
        System.out.println(result);
        return result;
    }

    // ========== NOUVELLE MÉTHODE : Fallback pour extraire le Bon de commande ==========
    private String extractCommandeNumberFallback(String text) {
        // Chercher un numéro à 10-13 chiffres isolé
        Pattern pattern = Pattern.compile("\\b([0-9]{10,13})\\b");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return "CMD-" + matcher.group(1);
        }
        return null;
    }

    // ========== NOUVELLE MÉTHODE : Fallback pour extraire le Bon de livraison ==========
    private String extractBLNumberFallback(String text) {
        // Chercher BL suivi de chiffres
        Pattern pattern = Pattern.compile("BL[-\\s]*([0-9]{10,13})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return "BL-" + matcher.group(1);
        }
        return null;
    }

    // ========== NOUVELLE MÉTHODE : Formater la date en ISO ==========
    private String formatDateToISO(String date) {
        if (date == null) return null;

        // Format DD/MM/YYYY -> YYYY-MM-DD
        Pattern pattern = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})");
        Matcher matcher = pattern.matcher(date);
        if (matcher.find()) {
            return matcher.group(3) + "-" + matcher.group(2) + "-" + matcher.group(1);
        }

        return date;
    }

    // ========== MÉTHODES EXISTANTES (INCHANGÉES) ==========
    private String extractCommandeNumber(String text) {
        Pattern pattern1 = Pattern.compile("N[°°]\\s*Bon\\s*de\\s*commande\\s*[:\\s]*([A-Z0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher1 = pattern1.matcher(text);
        if (matcher1.find()) {
            return matcher1.group(1).trim();
        }

        Pattern pattern2 = Pattern.compile("(CMD-[0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher2 = pattern2.matcher(text);
        if (matcher2.find()) {
            return matcher2.group(1).trim();
        }

        Pattern pattern3 = Pattern.compile("(PO-[0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher3 = pattern3.matcher(text);
        if (matcher3.find()) {
            return matcher3.group(1).trim();
        }

        return null;
    }

    private String extractBLNumber(String text) {
        Pattern pattern1 = Pattern.compile("N[°°]\\s*Bon\\s*de\\s*livraison\\s*[:\\s]*([A-Z0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher1 = pattern1.matcher(text);
        if (matcher1.find()) {
            return matcher1.group(1).trim();
        }

        Pattern pattern2 = Pattern.compile("(BL-[0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher2 = pattern2.matcher(text);
        if (matcher2.find()) {
            return matcher2.group(1).trim();
        }

        return null;
    }

    private String extractDate(String text) {
        Pattern pattern1 = Pattern.compile("Date\\s*de\\s*livraison\\s*[:\\s]*(\\d{4}-\\d{2}-\\d{2})", Pattern.CASE_INSENSITIVE);
        Matcher matcher1 = pattern1.matcher(text);
        if (matcher1.find()) {
            return matcher1.group(1);
        }

        Pattern pattern2 = Pattern.compile("Date\\s*réception\\s*[:\\s]*(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE);
        Matcher matcher2 = pattern2.matcher(text);
        if (matcher2.find()) {
            return matcher2.group(1);
        }

        Pattern pattern3 = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
        Matcher matcher3 = pattern3.matcher(text);
        if (matcher3.find()) {
            return matcher3.group(1);
        }

        Pattern pattern4 = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");
        Matcher matcher4 = pattern4.matcher(text);
        if (matcher4.find()) {
            return matcher4.group(1);
        }

        return null;
    }

    private String extractFournisseur(String text) {
        Pattern destPattern = Pattern.compile("Destinataire\\s*[:\\s]*Transfert\\s*entre\\s*entrepôts\\s*-\\s*Entrepot\\s*([A-Za-z\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher destMatcher = destPattern.matcher(text);
        if (destMatcher.find()) {
            String entrepotNom = destMatcher.group(1).trim();
            entrepotNom = entrepotNom.replaceAll("\\s+", " ");
            return "Transfert entre entrepôts - Entrepot " + entrepotNom;
        }

        Pattern transfertPattern = Pattern.compile("Transfert\\s*entre\\s*entrepôts\\s*-\\s*Entrepot\\s*([A-Za-z\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher transfertMatcher = transfertPattern.matcher(text);
        if (transfertMatcher.find()) {
            String entrepotNom = transfertMatcher.group(1).trim();
            entrepotNom = entrepotNom.replaceAll("\\s+", " ");
            return "Transfert entre entrepôts - Entrepot " + entrepotNom;
        }

        return null;
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
        } else if (barcode.startsWith("CMD-")) {
            result.put("numeroPO", barcode);
        } else {
            result.put("bonLivraison", barcode);
        }
        return result;
    }
}