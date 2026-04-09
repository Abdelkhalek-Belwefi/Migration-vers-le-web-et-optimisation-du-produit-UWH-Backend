package com.example.pfe.expedition.service;

import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.commande.entity.Commande;
import com.example.pfe.commande.enums.StatutCommande;
import com.example.pfe.commande.repository.CommandeRepository;
import com.example.pfe.expedition.dto.ExpeditionDTO;
import com.example.pfe.expedition.entity.Expedition;
import com.example.pfe.expedition.entity.ExpeditionStatut;
import com.example.pfe.expedition.repository.ExpeditionRepository;
import com.example.pfe.expedition.util.BarcodeUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.example.pfe.commande.entity.LigneCommande;

@Service
public class ExpeditionService {

    private final ExpeditionRepository expeditionRepository;
    private final CommandeRepository commandeRepository;
    private final UserRepository userRepository;

    public ExpeditionService(ExpeditionRepository expeditionRepository,
                             CommandeRepository commandeRepository,
                             UserRepository userRepository) {
        this.expeditionRepository = expeditionRepository;
        this.commandeRepository = commandeRepository;
        this.userRepository = userRepository;
    }

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

        // Mettre à jour le statut de la commande
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

            // Mettre à jour le statut de la commande associée
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
        dto.setClientNom(expedition.getCommande().getClient().getNom() + " " + expedition.getCommande().getClient().getPrenom());
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

    // ========== NOUVELLE MÉTHODE POUR GÉNÉRER LE BON DE LIVRAISON (HTML) ==========

    public String generateExpeditionPrintHtml(Long expeditionId) {
        Expedition expedition = expeditionRepository.findById(expeditionId)
                .orElseThrow(() -> new RuntimeException("Expédition non trouvée"));
        Commande commande = expedition.getCommande();

        // Récupération du transporteur
        String transporteurNom = expedition.getTransporteur() != null ? expedition.getTransporteur() : "Non spécifié";

        // Infos client
        String clientNom = commande.getClient().getNom() + " " + commande.getClient().getPrenom();
        String clientAdresse = commande.getClient().getAdresse() != null ? commande.getClient().getAdresse() : "";

        // Génération du DataMatrix
        String barcodeBase64 = BarcodeUtil.generateDataMatrixBase64(expedition.getNumeroBL(), 200, 200);

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
        html.append(".barcode { text-align: center; margin-top: 30px; }");
        html.append(".signature { margin-top: 40px; text-align: right; }");
        html.append("@media print { body { margin: 0; } .no-print { display: none; } }");
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>BON DE LIVRAISON</h1>");
        html.append("<div class='separator'></div>");

        html.append("<table class='info-table'>");
        html.append("<tr><td><strong>Transporteur :</strong></td><td>").append(transporteurNom).append("</td></tr>");
        html.append("<tr><td colspan='2'>&nbsp;</td></tr>");
        html.append(" hilab<td><strong>Client :</strong></td><td>").append(clientNom).append("</td></tr>");
        html.append("<tr><td><strong>Adresse :</strong></td><td>").append(clientAdresse).append("</td></tr>");
        html.append("<tr><td colspan='2'>&nbsp;</td></tr>");
        html.append("<tr><td><strong>N° Bon de commande :</strong></td><td>").append(commande.getNumeroCommande()).append("</td></tr>");
        html.append("<tr><td><strong>N° Bon de livraison :</strong></td><td>").append(expedition.getNumeroBL()).append("</td></tr>");
        html.append("<tr><td><strong>Date de livraison :</strong></td><td>").append(expedition.getDateExpedition() != null ? expedition.getDateExpedition().toLocalDate() : "").append("</td></tr>");
        html.append("</table>");
        html.append("<div class='separator'></div>");

        // TABLEAU DES ARTICLES
        html.append("<table class='articles-table'>");
        html.append("<thead>");
        html.append("<tr><th>Référence</th><th>Désignation</th><th>Qté</th></tr>");
        html.append("</thead><tbody>");

        for (LigneCommande ligne : commande.getLignes()) {
            String code = ligne.getArticleCode() != null ? ligne.getArticleCode() : "";
            String designation = "";
            int quantite = ligne.getQuantite() != null ? ligne.getQuantite() : 0;

            // Récupération de la désignation depuis l'objet Article
            if (ligne.getArticle() != null && ligne.getArticle().getDesignation() != null) {
                designation = ligne.getArticle().getDesignation();
            }

            html.append("<tr>");
            html.append("<td>").append(code).append("</td>");
            html.append("<td>").append(designation).append("</td>");
            html.append("<td>").append(quantite).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");
        html.append("<div class='separator'></div>");

        // DataMatrix
        html.append("<div class='barcode'>");
        html.append("<img src='data:image/png;base64,").append(barcodeBase64).append("' alt='DataMatrix' />");
        html.append("<br/><small>").append(expedition.getNumeroBL()).append("</small>");
        html.append("</div>");

        // Signature
        html.append("<div class='signature'>");
        html.append("<p>Cachet et signature du destinataire :</p>");
        html.append("<p style='margin-top: 30px;'>_________________________</p>");
        html.append("</div>");

        // Bouton impression
        html.append("<div class='no-print' style='text-align:center; margin-top:20px;'>");
        html.append("<button onclick='window.print()' style='padding:10px 20px;'>🖨️ Imprimer</button>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }
}