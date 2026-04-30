package com.example.pfe.expedition.service;

import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.auth.entity.Role;
import com.example.pfe.commande.entity.Commande;
import com.example.pfe.commande.entity.LigneCommande;
import com.example.pfe.commande.enums.StatutCommande;
import com.example.pfe.commande.enums.TypeCommande;
import com.example.pfe.commande.repository.CommandeRepository;
import com.example.pfe.expedition.dto.ExpeditionDTO;
import com.example.pfe.expedition.entity.Expedition;
import com.example.pfe.expedition.entity.ExpeditionStatut;
import com.example.pfe.expedition.repository.ExpeditionRepository;
import com.example.pfe.expedition.util.BarcodeUtil;
import com.example.pfe.livraison.entity.Livraison;
import com.example.pfe.livraison.entity.LivraisonStatut;
import com.example.pfe.livraison.repository.LivraisonRepository;
import com.example.pfe.service.EmailService;
import com.example.pfe.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpeditionService {

    private final ExpeditionRepository expeditionRepository;
    private final CommandeRepository commandeRepository;
    private final UserRepository userRepository;
    private final StockService stockService;

    @Autowired
    private LivraisonRepository livraisonRepository;

    @Autowired
    private EmailService emailService;

    public ExpeditionService(ExpeditionRepository expeditionRepository,
                             CommandeRepository commandeRepository,
                             UserRepository userRepository,
                             StockService stockService) {
        this.expeditionRepository = expeditionRepository;
        this.commandeRepository = commandeRepository;
        this.userRepository = userRepository;
        this.stockService = stockService;
    }

    // ========== MÉTHODES EXISTANTES (INCHANGÉES) ==========

    public List<ExpeditionDTO> getAllExpeditions() {
        return expeditionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ExpeditionDTO getExpeditionById(Long id) {
        Expedition expedition = expeditionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expédition non trouvée"));
        return convertToDTO(expedition);
    }

    public List<ExpeditionDTO> getExpeditionsByStatut(ExpeditionStatut statut) {
        return expeditionRepository.findByStatut(statut).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpeditionDTO expedierCommande(Long commandeId, String transporteur) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        if (commande.getStatut() != StatutCommande.VALIDEE) {
            throw new RuntimeException("Seules les commandes validées peuvent être expédiées");
        }
        if (expeditionRepository.findByCommandeId(commandeId).isPresent()) {
            throw new RuntimeException("Une expédition existe déjà pour cette commande");
        }

        Expedition expedition = new Expedition();
        expedition.setCommande(commande);
        expedition.setNumeroBL(generateNumeroBL());
        expedition.setTransporteur(transporteur);
        expedition.setStatut(ExpeditionStatut.EXPEDIEE);
        expedition.setDateExpedition(LocalDateTime.now());
        expedition.setPreparePar(getCurrentUser());

        Expedition saved = expeditionRepository.save(expedition);

        commande.setStatut(StatutCommande.EXPEDIEE);
        commande.setUpdatedAt(LocalDateTime.now());
        commandeRepository.save(commande);

        return convertToDTO(saved);
    }

    @Transactional
    public ExpeditionDTO updateStatut(Long id, ExpeditionStatut statut) {
        Expedition expedition = expeditionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expédition non trouvée"));
        expedition.setStatut(statut);
        if (statut == ExpeditionStatut.EXPEDIEE) {
            expedition.setDateExpedition(LocalDateTime.now());
            expedition.setValidePar(getCurrentUser());

            Commande commande = expedition.getCommande();
            commande.setStatut(StatutCommande.EXPEDIEE);
            commande.setUpdatedAt(LocalDateTime.now());
            commandeRepository.save(commande);
        }
        expedition.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(expeditionRepository.save(expedition));
    }

    @Transactional
    public void deleteExpedition(Long id) {
        expeditionRepository.deleteById(id);
    }

    private String generateNumeroBL() {
        return "BL-" + System.currentTimeMillis();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        }
        return null;
    }

    private ExpeditionDTO convertToDTO(Expedition expedition) {
        ExpeditionDTO dto = new ExpeditionDTO();
        dto.setId(expedition.getId());
        dto.setCommandeId(expedition.getCommande().getId());
        dto.setCommandeNumero(expedition.getCommande().getNumeroCommande());

        // Gestion du client (peut être null pour les commandes de transfert)
        if (expedition.getCommande().getClient() != null) {
            dto.setClientNom(expedition.getCommande().getClient().getNom() + " " + expedition.getCommande().getClient().getPrenom());
        } else {
            dto.setClientNom("Transfert entre entrepôts");
        }

        dto.setNumeroBL(expedition.getNumeroBL());
        dto.setStatut(expedition.getStatut());
        dto.setDateExpedition(expedition.getDateExpedition());
        dto.setPrepareParNom(expedition.getPreparePar() != null ? expedition.getPreparePar().getNom() + " " + expedition.getPreparePar().getPrenom() : null);
        dto.setTransporteur(expedition.getTransporteur());
        dto.setNumeroSuivi(expedition.getNumeroSuivi());
        dto.setCreatedAt(expedition.getCreatedAt());
        dto.setUpdatedAt(expedition.getUpdatedAt());
        return dto;
    }

    public List<ExpeditionDTO> getExpeditionsByCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        return expeditionRepository.findAll().stream()
                .filter(exp -> exp.getPreparePar() != null && exp.getPreparePar().getId().equals(currentUser.getId()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public String generateExpeditionPrintHtml(Long expeditionId) {
        Expedition expedition = expeditionRepository.findById(expeditionId)
                .orElseThrow(() -> new RuntimeException("Expédition non trouvée"));
        Commande commande = expedition.getCommande();

        String transporteurNom = expedition.getTransporteur() != null ? expedition.getTransporteur() : "Non spécifié";

        // Gestion du client (peut être null pour les commandes de transfert)
        String clientNom;
        String clientAdresse;

        if (commande.getClient() != null) {
            clientNom = commande.getClient().getNom() + " " + commande.getClient().getPrenom();
            clientAdresse = commande.getClient().getAdresse() != null ? commande.getClient().getAdresse() : "";
        } else {
            clientNom = "Transfert entre entrepôts - " + commande.getEntrepotDestination().getNom();
            clientAdresse = commande.getEntrepotDestination().getAdresse() != null ? commande.getEntrepotDestination().getAdresse() : "";
        }

        String barcodeBL = BarcodeUtil.generateDataMatrixBase64(expedition.getNumeroBL(), 200, 200);
        String barcodeCommande = BarcodeUtil.generateDataMatrixBase64(commande.getNumeroCommande(), 200, 200);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Bon de livraison - ").append(expedition.getNumeroBL()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: 'Courier New', monospace; margin: 2cm; }");
        html.append("h1 { text-align: center; }");
        html.append(".separator { border-top: 1px solid #000; margin: 10px 0; }");
        html.append(".info-table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append(".info-table td { padding: 5px; vertical-align: top; }");
        html.append(".info-table td:first-child { font-weight: bold; width: 35%; }");
        html.append(".articles-table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append(".articles-table th, .articles-table td { border: 1px solid #000; padding: 8px; text-align: left; }");
        html.append(".articles-table th { background-color: #f2f2f2; }");
        html.append(".articles-table td:last-child { text-align: center; }");
        html.append(".barcode { display: flex; justify-content: center; gap: 40px; margin-top: 30px; }");
        html.append(".barcode-item { text-align: center; }");
        html.append(".signature { margin-top: 40px; text-align: right; }");
        html.append("@media print { body { margin: 0; } .no-print { display: none; } }");
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>BON DE LIVRAISON</h1>");
        html.append("<div class='separator'></div>");

        html.append("<table class='info-table'>");
        html.append("<tr><th>Transporteur :</th><td>").append(transporteurNom).append("</tr>");
        html.append("<tr><td colspan='2'>&nbsp;</td></tr>");
        html.append("<tr><th>Destinataire :</th><td>").append(clientNom).append("</td></tr>");
        html.append("<tr><th>Adresse :</th><td>").append(clientAdresse).append("</td></tr>");
        html.append("<tr><td colspan='2'>&nbsp;</td></tr>");
        html.append("<tr><th>N° Bon de commande :</th><td>").append(commande.getNumeroCommande()).append("</td></tr>");
        html.append("<tr><th>N° Bon de livraison :</th><td>").append(expedition.getNumeroBL()).append("</td></tr>");
        html.append("<tr><th>Date de livraison :</th><td>").append(expedition.getDateExpedition() != null ? expedition.getDateExpedition().toLocalDate() : "").append("</td></tr>");
        html.append("</table>");
        html.append("<div class='separator'></div>");

        html.append("<table class='articles-table'>");
        html.append("<thead><tr><th>Référence</th><th>Désignation</th><th>Qté</th><th>Code-barre</th></tr></thead><tbody>");

        for (LigneCommande ligne : commande.getLignes()) {
            String code = ligne.getArticleCode() != null ? ligne.getArticleCode() : "";
            String designation = "";
            int quantite = ligne.getQuantite() != null ? ligne.getQuantite() : 0;

            if (ligne.getArticle() != null && ligne.getArticle().getDesignation() != null) {
                designation = ligne.getArticle().getDesignation();
            }

            String gtin = "";
            if (ligne.getArticle() != null && ligne.getArticle().getGtin() != null && !ligne.getArticle().getGtin().isBlank()) {
                gtin = ligne.getArticle().getGtin();
            } else if (!code.isEmpty()) {
                gtin = code;
            }

            String barcodeArticle = "";
            if (!gtin.isEmpty()) {
                barcodeArticle = BarcodeUtil.generateDataMatrixBase64(gtin, 100, 100);
            }

            html.append("<tr>");
            html.append("<td>").append(code).append("</td>");
            html.append("<td>").append(designation).append("</td>");
            html.append("<td>").append(quantite).append("</td>");
            html.append("<td>");
            if (!barcodeArticle.isEmpty()) {
                html.append("<img src='data:image/png;base64,").append(barcodeArticle).append("' width='70' alt='Code-barres article'/>");
            } else {
                html.append("-");
            }
            html.append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table>");
        html.append("<div class='separator'></div>");

        html.append("<div class='barcode'>");
        html.append("<div class='barcode-item'>");
        html.append("<img src='data:image/png;base64,").append(barcodeBL).append("' alt='Code-barres BL' />");
        html.append("<br/><small>BL : ").append(expedition.getNumeroBL()).append("</small>");
        html.append("</div>");
        html.append("<div class='barcode-item'>");
        html.append("<img src='data:image/png;base64,").append(barcodeCommande).append("' alt='Code-barres Commande' />");
        html.append("<br/><small>Commande : ").append(commande.getNumeroCommande()).append("</small>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='signature'>");
        html.append("<p>Cachet et signature du destinataire :</p>");
        html.append("<p style='margin-top: 30px;'>_________________________</p>");
        html.append("</div>");

        html.append("<div class='no-print' style='text-align:center; margin-top:20px;'>");
        html.append("<button onclick='window.print()' style='padding:10px 20px;'>🖨️ Imprimer</button>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    public List<ExpeditionDTO> getAllExpeditionsForList() {
        return expeditionRepository.findAll().stream()
                .sorted((e1, e2) -> e2.getDateExpedition().compareTo(e1.getDateExpedition()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpeditionDTO assignerTransporteur(Long expeditionId, Long transporteurId) {
        Expedition expedition = expeditionRepository.findById(expeditionId)
                .orElseThrow(() -> new RuntimeException("Expédition non trouvée"));
        expedition.setTransporteur(transporteurId.toString());
        expedition.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(expeditionRepository.save(expedition));
    }

    // ========== MÉTHODE CORRIGÉE POUR TRANSFERT AVEC DIMINUTION DU STOCK ==========

    @Transactional
    public ExpeditionDTO expedierCommandeWithTransporteurId(Long commandeId, Long transporteurId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        if (commande.getStatut() != StatutCommande.VALIDEE) {
            throw new RuntimeException("Seules les commandes validées peuvent être expédiées");
        }
        if (expeditionRepository.findByCommandeId(commandeId).isPresent()) {
            throw new RuntimeException("Une expédition existe déjà pour cette commande");
        }

        User transporteur = userRepository.findById(transporteurId)
                .orElseThrow(() -> new RuntimeException("Transporteur non trouvé"));
        if (transporteur.getRole() != Role.TRANSPORTEUR) {
            throw new RuntimeException("L'utilisateur n'a pas le rôle TRANSPORTEUR");
        }

        // ========== DIMINUER LE STOCK POUR LES COMMANDES DE TRANSFERT ==========
        if (commande.getTypeCommande() == TypeCommande.TRANSFERT) {
            Long entrepotSourceId = commande.getEntrepotSource().getId();
            System.out.println("📦 Diminution du stock pour transfert - Entrepôt source ID: " + entrepotSourceId);

            for (LigneCommande ligne : commande.getLignes()) {
                String lot = ligne.getArticle().getLotDefaut();
                if (lot == null || lot.isEmpty()) {
                    lot = "DEFAULT";
                }
                try {
                    stockService.decrementStock(ligne.getArticle().getId(), ligne.getQuantite(), entrepotSourceId, lot);
                    System.out.println("✅ Stock diminué: Article " + ligne.getArticle().getId() + ", Qté: " + ligne.getQuantite() + ", Lot: " + lot);
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors de la diminution du stock: " + e.getMessage());
                    throw new RuntimeException("Erreur lors de la diminution du stock: " + e.getMessage());
                }
            }
        }

        Expedition expedition = new Expedition();
        expedition.setCommande(commande);
        expedition.setNumeroBL(generateNumeroBL());
        expedition.setTransporteur(transporteur.getNom() + " " + transporteur.getPrenom());
        expedition.setStatut(ExpeditionStatut.EXPEDIEE);
        expedition.setDateExpedition(LocalDateTime.now());
        expedition.setPreparePar(getCurrentUser());
        expedition.setEntrepot(commande.getEntrepot());

        Expedition saved = expeditionRepository.save(expedition);

        commande.setStatut(StatutCommande.EXPEDIEE);
        commande.setUpdatedAt(LocalDateTime.now());
        commandeRepository.save(commande);

        Livraison livraison = new Livraison();
        livraison.setExpedition(saved);
        livraison.setTransporteur(transporteur);
        livraison.setCodeOtp(generateOtp());
        livraison.setStatut(LivraisonStatut.ASSIGNEE);
        livraison.setDateAssignation(LocalDateTime.now());
        livraisonRepository.save(livraison);

        // Envoi de l'email uniquement pour les commandes client (pas pour les transferts)
        if (commande.getTypeCommande() != TypeCommande.TRANSFERT) {
            try {
                String clientEmail = commande.getClient().getEmail();
                if (clientEmail != null && !clientEmail.isEmpty()) {
                    emailService.sendOtpEmail(clientEmail, livraison.getCodeOtp(), saved.getNumeroBL());
                    System.out.println("OTP envoyé à " + clientEmail);
                } else {
                    System.out.println("Le client n'a pas d'adresse email : " + commande.getClient().getNom());
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            }
        } else {
            System.out.println("📦 Commande de transfert expédiée depuis l'entrepôt: " + commande.getEntrepotSource().getNom());
            System.out.println("📦 Vers l'entrepôt: " + commande.getEntrepotDestination().getNom());
            System.out.println("🔑 OTP généré: " + livraison.getCodeOtp());
        }

        return convertToDTO(saved);
    }

    private String generateOtp() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // ========== MÉTHODES DE FILTRAGE ==========

    private Long getCurrentUserEntrepotId() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null && currentUser.getEntrepot() != null) {
                return currentUser.getEntrepot().getId();
            }
        } catch (Exception e) {
            System.out.println("Erreur récupération entrepôt utilisateur: " + e.getMessage());
        }
        return null;
    }

    public List<ExpeditionDTO> getAllExpeditionsFiltered() {
        Long entrepotId = getCurrentUserEntrepotId();
        List<Expedition> expeditions;
        if (entrepotId != null) {
            expeditions = expeditionRepository.findByEntrepotId(entrepotId);
        } else {
            expeditions = expeditionRepository.findAll();
        }
        return expeditions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ExpeditionDTO> getExpeditionsByStatutFiltered(ExpeditionStatut statut) {
        Long entrepotId = getCurrentUserEntrepotId();
        List<Expedition> expeditions;
        if (entrepotId != null) {
            expeditions = expeditionRepository.findByStatutAndEntrepotId(statut, entrepotId);
        } else {
            expeditions = expeditionRepository.findByStatut(statut);
        }
        return expeditions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ExpeditionDTO> getExpeditionsByCurrentUserFiltered() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        Long entrepotId = getCurrentUserEntrepotId();
        List<Expedition> expeditions;
        if (entrepotId != null) {
            expeditions = expeditionRepository.findByPrepareParIdAndEntrepotId(currentUser.getId(), entrepotId);
        } else {
            expeditions = expeditionRepository.findAll().stream()
                    .filter(exp -> exp.getPreparePar() != null && exp.getPreparePar().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }
        return expeditions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ExpeditionDTO> getAllExpeditionsForListFiltered() {
        Long entrepotId = getCurrentUserEntrepotId();
        List<Expedition> expeditions;
        if (entrepotId != null) {
            expeditions = expeditionRepository.findByEntrepotId(entrepotId);
        } else {
            expeditions = expeditionRepository.findAll();
        }
        return expeditions.stream()
                .sorted((e1, e2) -> e2.getDateExpedition().compareTo(e1.getDateExpedition()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}