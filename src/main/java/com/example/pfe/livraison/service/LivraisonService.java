package com.example.pfe.livraison.service;

import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.commande.entity.Commande;
import com.example.pfe.expedition.entity.Expedition;
import com.example.pfe.expedition.repository.ExpeditionRepository;
import com.example.pfe.livraison.dto.LivraisonDTO;
import com.example.pfe.livraison.dto.ValidationLivraisonRequest;
import com.example.pfe.livraison.entity.Livraison;
import com.example.pfe.livraison.entity.LivraisonStatut;
import com.example.pfe.livraison.repository.LivraisonRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LivraisonService {

    private static final double MAX_DISTANCE_METERS = 1000000000.0;
    private final LivraisonRepository livraisonRepository;
    private final ExpeditionRepository expeditionRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Map de fallback pour les villes tunisiennes
    private static final Map<String, Double[]> CITY_COORDINATES = new HashMap<>();

    static {
        CITY_COORDINATES.put("kélibia", new Double[]{36.8481, 11.0939});
        CITY_COORDINATES.put("kelibia", new Double[]{36.8481, 11.0939});
        CITY_COORDINATES.put("menzel temime", new Double[]{36.7882, 10.9817});
        CITY_COORDINATES.put("menzel tmim", new Double[]{36.7882, 10.9817});
        CITY_COORDINATES.put("nabeul", new Double[]{36.4539, 10.7363});
        CITY_COORDINATES.put("tunis", new Double[]{36.8065, 10.1815});
        CITY_COORDINATES.put("hammamet", new Double[]{36.4000, 10.6167});
        CITY_COORDINATES.put("sousse", new Double[]{35.8256, 10.6370});
        CITY_COORDINATES.put("sfax", new Double[]{34.7406, 10.7603});
        CITY_COORDINATES.put("monastir", new Double[]{35.7779, 10.8262});
        CITY_COORDINATES.put("bizerte", new Double[]{37.2744, 9.8739});
        CITY_COORDINATES.put("beja", new Double[]{36.7256, 9.1817});
        CITY_COORDINATES.put("jendouba", new Double[]{36.5015, 8.7802});
        CITY_COORDINATES.put("le kef", new Double[]{36.1822, 8.7144});
        CITY_COORDINATES.put("siliana", new Double[]{36.0849, 9.3708});
        CITY_COORDINATES.put("zaghouan", new Double[]{36.4029, 10.1423});
        CITY_COORDINATES.put("ben arous", new Double[]{36.7431, 10.2184});
        CITY_COORDINATES.put("l' ariana", new Double[]{36.8625, 10.1956});
        CITY_COORDINATES.put("manouba", new Double[]{36.8078, 10.0972});
        CITY_COORDINATES.put("mahdia", new Double[]{35.5028, 11.0623});
        CITY_COORDINATES.put("kairouan", new Double[]{35.6781, 10.0964});
        CITY_COORDINATES.put("kasserine", new Double[]{35.1676, 8.8365});
        CITY_COORDINATES.put("sidi bouzid", new Double[]{35.0382, 9.4858});
        CITY_COORDINATES.put("gafsa", new Double[]{34.4250, 8.7842});
        CITY_COORDINATES.put("tozeur", new Double[]{33.9197, 8.1336});
        CITY_COORDINATES.put("kebili", new Double[]{33.7043, 8.9690});
        CITY_COORDINATES.put("gabes", new Double[]{33.8815, 10.0982});
        CITY_COORDINATES.put("medenine", new Double[]{33.3549, 10.5055});
        CITY_COORDINATES.put("tataouine", new Double[]{32.9308, 10.4518});
    }

    public LivraisonService(LivraisonRepository livraisonRepository,
                            ExpeditionRepository expeditionRepository,
                            UserRepository userRepository) {
        this.livraisonRepository = livraisonRepository;
        this.expeditionRepository = expeditionRepository;
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ========== MÉTHODES EXISTANTES (INCHANGÉES) ==========

    @Transactional
    public LivraisonDTO assignerTransporteur(Long expeditionId, Long transporteurId) {
        Expedition expedition = expeditionRepository.findById(expeditionId)
                .orElseThrow(() -> new RuntimeException("Expédition non trouvée"));
        User transporteur = userRepository.findById(transporteurId)
                .orElseThrow(() -> new RuntimeException("Transporteur non trouvé"));
        if (transporteur.getRole() != com.example.pfe.auth.entity.Role.TRANSPORTEUR) {
            throw new RuntimeException("L'utilisateur n'a pas le rôle TRANSPORTEUR");
        }
        if (livraisonRepository.findByExpeditionId(expeditionId).isPresent()) {
            throw new RuntimeException("Une livraison est déjà assignée à cette expédition");
        }

        Livraison livraison = new Livraison();
        livraison.setExpedition(expedition);
        livraison.setTransporteur(transporteur);
        livraison.setCodeOtp(generateOtp());
        livraison.setStatut(LivraisonStatut.ASSIGNEE);

        Livraison saved = livraisonRepository.save(livraison);
        return convertToDTO(saved);
    }

    public List<LivraisonDTO> getLivraisonsPourTransporteur() {
        User transporteur = getCurrentTransporteur();
        List<LivraisonStatut> statuts = List.of(LivraisonStatut.ASSIGNEE, LivraisonStatut.EN_COURS);
        return livraisonRepository.findByTransporteurAndStatutIn(transporteur, statuts).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LivraisonDTO> getHistoriqueLivraisons() {
        User transporteur = getCurrentTransporteur();
        List<LivraisonStatut> statuts = List.of(LivraisonStatut.LIVREE, LivraisonStatut.ECHOUEE);
        return livraisonRepository.findByTransporteurAndStatutIn(transporteur, statuts).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========== GÉOCODAGE NOMINATIM AMÉLIORÉ ==========
    private Double[] geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) return null;

        String cleanAddress = address
                .replace("Tunisie", "Tunisia")
                .replace("kélibia", "Kelibia")
                .replace("Kélibia", "Kelibia")
                .trim();

        // Ajouter Tunisia si pas présent
        if (!cleanAddress.toLowerCase().contains("tunisia")) {
            cleanAddress = cleanAddress + ", Tunisia";
        }

        System.out.println("🔍 Tentative géocodage: " + cleanAddress);

        try {
            String encodedAddress = URLEncoder.encode(cleanAddress, "UTF-8");
            String url = "https://nominatim.openstreetmap.org/search?q=" + encodedAddress + "&format=json&limit=1";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "WarehouseApp/1.0 (contact@warehouse.com)");
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            if (root.isArray() && root.size() > 0) {
                double lat = root.get(0).get("lat").asDouble();
                double lon = root.get(0).get("lon").asDouble();
                System.out.println("📍 Géocodage réussi: " + cleanAddress + " -> " + lat + ", " + lon);
                return new Double[]{lat, lon};
            } else {
                System.out.println("⚠️ Aucun résultat Nominatim pour: " + cleanAddress);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur Nominatim: " + e.getMessage());
        }

        // Fallback: coordonnées par ville
        return getFallbackCoordinates(address);
    }

    private Double[] getFallbackCoordinates(String address) {
        if (address == null) return null;

        String lowerAddress = address.toLowerCase();

        for (Map.Entry<String, Double[]> entry : CITY_COORDINATES.entrySet()) {
            if (lowerAddress.contains(entry.getKey())) {
                System.out.println("📍 Fallback trouvé pour: " + entry.getKey() + " -> " + entry.getValue()[0] + ", " + entry.getValue()[1]);
                return entry.getValue();
            }
        }

        System.out.println("⚠️ Aucun fallback trouvé pour: " + address);
        return null;
    }

    // ========== MÉTHODE VALIDATION (CORRIGÉE) ==========
    @Transactional
    public LivraisonDTO validerLivraison(Long livraisonId, ValidationLivraisonRequest request) {
        Livraison livraison = livraisonRepository.findById(livraisonId)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée"));
        User transporteur = getCurrentTransporteur();
        if (!livraison.getTransporteur().getId().equals(transporteur.getId())) {
            throw new RuntimeException("Vous n'êtes pas le transporteur assigné");
        }
        if (livraison.getStatut() == LivraisonStatut.LIVREE) {
            throw new RuntimeException("Livraison déjà validée");
        }

        // Vérification OTP
        if (!livraison.getCodeOtp().equals(request.getCodeOtp())) {
            throw new RuntimeException("Code OTP invalide");
        }

        Commande commande = livraison.getExpedition().getCommande();

        // Récupérer les coordonnées du client (base ou géocodage)
        Double clientLat = commande.getClient().getLatitude();
        Double clientLng = commande.getClient().getLongitude();

        if (clientLat == null || clientLng == null) {
            System.out.println("🔍 Coordonnées client manquantes, géocodage...");
            String adresseClient = commande.getClient().getAdresse();
            if (adresseClient != null && !adresseClient.isEmpty()) {
                Double[] coords = geocodeAddress(adresseClient);
                if (coords != null) {
                    clientLat = coords[0];
                    clientLng = coords[1];
                    // Sauvegarder pour la prochaine fois
                    commande.getClient().setLatitude(clientLat);
                    commande.getClient().setLongitude(clientLng);
                }
            }
        }

        if (clientLat == null || clientLng == null) {
            throw new RuntimeException("Impossible de localiser l'adresse du client: " + commande.getClient().getAdresse());
        }

        double distance = distance(clientLat, clientLng, request.getLatitude(), request.getLongitude());
        if (distance > MAX_DISTANCE_METERS) {
            throw new RuntimeException("Vous êtes trop loin du point de livraison (" + (int)distance + "m)");
        }

        livraison.setStatut(LivraisonStatut.LIVREE);
        livraison.setDateLivraison(LocalDateTime.now());
        livraison.setLatitudeValidation(request.getLatitude());
        livraison.setLongitudeValidation(request.getLongitude());
        livraison.setCommentaire(request.getCommentaire());

        Expedition expedition = livraison.getExpedition();
        expedition.setStatut(com.example.pfe.expedition.entity.ExpeditionStatut.EXPEDIEE);
        expeditionRepository.save(expedition);

        return convertToDTO(livraisonRepository.save(livraison));
    }

    // ========== MÉTHODES UTILITAIRES (INCHANGÉES) ==========

    private User getCurrentTransporteur() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        }
        throw new RuntimeException("Non authentifié");
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000;
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);
        double a = Math.sin(deltaPhi/2) * Math.sin(deltaPhi/2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLambda/2) * Math.sin(deltaLambda/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // ========== CONVERSION (INCHANGÉE) ==========

    private LivraisonDTO convertToDTO(Livraison livraison) {
        LivraisonDTO dto = new LivraisonDTO();
        dto.setId(livraison.getId());
        dto.setExpeditionId(livraison.getExpedition().getId());
        dto.setNumeroBL(livraison.getExpedition().getNumeroBL());
        dto.setClientNom(livraison.getExpedition().getCommande().getClient().getNom() + " " +
                livraison.getExpedition().getCommande().getClient().getPrenom());
        dto.setAdresseLivraison(livraison.getExpedition().getCommande().getClient().getAdresse());
        dto.setTransporteurNom(livraison.getTransporteur().getNom() + " " + livraison.getTransporteur().getPrenom());
        dto.setCodeOtp(livraison.getCodeOtp());
        dto.setStatut(livraison.getStatut());
        dto.setDateAssignation(livraison.getDateAssignation());
        dto.setDateLivraison(livraison.getDateLivraison());

        if (livraison.getExpedition().getCommande().getClient() != null) {
            dto.setClientLatitude(livraison.getExpedition().getCommande().getClient().getLatitude());
            dto.setClientLongitude(livraison.getExpedition().getCommande().getClient().getLongitude());
        }

        return dto;
    }
}