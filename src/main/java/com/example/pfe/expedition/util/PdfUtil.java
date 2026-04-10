package com.example.pfe.expedition.util;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;

public class PdfUtil {

    public static byte[] convertHtmlToPdf(String htmlContent) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            HtmlConverter.convertToPdf(htmlContent, writer);
            writer.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la conversion HTML → PDF", e);
        }
    }
}