package com.example.pfe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${geocoding.nominatim.url:https://nominatim.openstreetmap.org/search}")
    private String nominatimUrl;

    @Value("${geocoding.user-agent:WarehouseApp/1.0}")
    private String userAgent;

    @Autowired
    public GeocodingService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Convertit une adresse en coordonnées GPS (latitude, longitude).
     *
     * @param address l'adresse à géocoder.
     * @return un tableau de deux doubles [latitude, longitude] ou null si échec.
     */
    public Double[] geocodeAddress(String address) {
        // Construire l'URL avec les paramètres requis
        String url = UriComponentsBuilder.fromHttpUrl(nominatimUrl)
                .queryParam("q", address)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .build()
                .toUriString();

        // Définir le header User-Agent (obligatoire pour Nominatim)
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, userAgent);
        org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);

        try {
            // Envoyer la requête GET
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, entity);

            if (response.getBody() == null) {
                return null;
            }

            // Parser la réponse JSON
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.isArray() && root.size() > 0) {
                JsonNode firstResult = root.get(0);
                double lat = firstResult.get("lat").asDouble();
                double lon = firstResult.get("lon").asDouble();
                return new Double[]{lat, lon};
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du géocodage de l'adresse: " + address);
            e.printStackTrace();
        }
        return null;
    }
}