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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LivraisonService {
    // tu ajuster la distance pour que valide la laivraison lorsque tu modifier max distance metres
    private static final double MAX_DISTANCE_METERS = 1000000000.0;
    private final LivraisonRepository livraisonRepository;
    private final ExpeditionRepository expeditionRepository;
    private final UserRepository userRepository;

    public LivraisonService(LivraisonRepository livraisonRepository,
                            ExpeditionRepository expeditionRepository,
                            UserRepository userRepository) {
        this.livraisonRepository = livraisonRepository;
        this.expeditionRepository = expeditionRepository;
        this.userRepository = userRepository;
    }

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
        // TODO: envoyer l'OTP au client par email/SMS (à implémenter)
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

        // Vérification géographique
        Commande commande = livraison.getExpedition().getCommande();
        String adresseClient = commande.getClient().getAdresse();
        // Ici, il faudrait géocoder l'adresse en coordonnées (appel API externe)
        // Pour l'exemple, on suppose qu'on a stocké lat/lng dans le client (à ajouter)
        // On va simuler un check qui échoue si pas de coordonnées
        if (commande.getClient().getLatitude() == null || commande.getClient().getLongitude() == null) {
            throw new RuntimeException("Coordonnées client non définies, impossible de vérifier la proximité");
        }
        double distance = distance(
                commande.getClient().getLatitude(),
                commande.getClient().getLongitude(),
                request.getLatitude(),
                request.getLongitude()
        );
        if (distance > MAX_DISTANCE_METERS) {
            throw new RuntimeException("Vous êtes trop loin du point de livraison (" + (int)distance + "m > " + MAX_DISTANCE_METERS + "m)");
        }

        livraison.setStatut(LivraisonStatut.LIVREE);
        livraison.setDateLivraison(LocalDateTime.now());
        livraison.setLatitudeValidation(request.getLatitude());
        livraison.setLongitudeValidation(request.getLongitude());
        livraison.setCommentaire(request.getCommentaire());

        // Optionnel : mettre à jour le statut de l'expédition
        Expedition expedition = livraison.getExpedition();
        expedition.setStatut(com.example.pfe.expedition.entity.ExpeditionStatut.EXPEDIEE);
        expeditionRepository.save(expedition);

        return convertToDTO(livraisonRepository.save(livraison));
    }

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
        double R = 6371000; // mètres
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

        // Ajout des coordonnées GPS du client
        if (livraison.getExpedition().getCommande().getClient() != null) {
            dto.setClientLatitude(livraison.getExpedition().getCommande().getClient().getLatitude());
            dto.setClientLongitude(livraison.getExpedition().getCommande().getClient().getLongitude());
        }

        return dto;
    }
}