package com.example.pfe.expedition.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.DataMatrixWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class BarcodeUtil {

    public static String generateDataMatrixBase64(String content, int width, int height) {
        try {
            DataMatrixWriter writer = new DataMatrixWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.DATA_MATRIX, width, height);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(pngData);
        } catch (Exception e) {  // Capture toutes les exceptions (WriterException n'est plus lancée seule)
            throw new RuntimeException("Erreur génération DataMatrix", e);
        }
    }
}