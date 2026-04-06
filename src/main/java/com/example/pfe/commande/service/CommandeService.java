package com.example.pfe.commande.service;

import com.example.pfe.article.entity.Article;
import com.example.pfe.article.repository.ArticleRepository;
import com.example.pfe.client.entity.Client;
import com.example.pfe.client.repository.ClientRepository;
import com.example.pfe.commande.dto.CommandeDTO;
import com.example.pfe.commande.dto.LigneCommandeDTO;
import com.example.pfe.commande.entity.Commande;
import com.example.pfe.commande.entity.LigneCommande;
import com.example.pfe.commande.enums.StatutCommande;
import com.example.pfe.commande.repository.CommandeRepository;
import com.example.pfe.commande.repository.LigneCommandeRepository;
import com.example.pfe.picking.service.PickingService;
import com.example.pfe.stock.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final ClientRepository clientRepository;
    private final ArticleRepository articleRepository;
    private final PickingService pickingService;
    private final StockService stockService;   // ← ajout

    public CommandeService(CommandeRepository commandeRepository,
                           LigneCommandeRepository ligneCommandeRepository,
                           ClientRepository clientRepository,
                           ArticleRepository articleRepository,
                           PickingService pickingService,
                           StockService stockService) {   // ← ajout
        this.commandeRepository = commandeRepository;
        this.ligneCommandeRepository = ligneCommandeRepository;
        this.clientRepository = clientRepository;
        this.articleRepository = articleRepository;
        this.pickingService = pickingService;
        this.stockService = stockService;
    }

    public List<CommandeDTO> getAllCommandes() {
        return commandeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CommandeDTO getCommandeById(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        return convertToDTO(commande);
    }

    public List<CommandeDTO> getCommandesByStatut(StatutCommande statut) {
        return commandeRepository.findByStatut(statut).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CommandeDTO> getCommandesAExpedier() {
        return commandeRepository.findByStatutAndNoExpedition(StatutCommande.VALIDEE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommandeDTO createCommande(CommandeDTO dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        Commande commande = new Commande();
        commande.setClient(client);
        commande.setNumeroCommande(generateNumeroCommande());
        commande.setDateLivraisonSouhaitee(dto.getDateLivraisonSouhaitee());
        commande.setNotes(dto.getNotes());
        commande.setStatut(StatutCommande.EN_ATTENTE);

        Commande savedCommande = commandeRepository.save(commande);

        if (dto.getLignes() != null) {
            for (LigneCommandeDTO ligneDTO : dto.getLignes()) {
                Article article = articleRepository.findById(ligneDTO.getArticleId())
                        .orElseThrow(() -> new RuntimeException("Article non trouvé"));
                LigneCommande ligne = new LigneCommande();
                ligne.setCommande(savedCommande);
                ligne.setArticle(article);
                ligne.setQuantite(ligneDTO.getQuantite());
                ligne.setPrixUnitaire(ligneDTO.getPrixUnitaire() != null
                        ? ligneDTO.getPrixUnitaire()
                        : article.getPrixUnitaire());
                savedCommande.getLignes().add(ligne);
            }
            commandeRepository.save(savedCommande);
        }
        return convertToDTO(savedCommande);
    }

    @Transactional
    public CommandeDTO updateCommande(Long id, CommandeDTO dto) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        commande.setDateLivraisonSouhaitee(dto.getDateLivraisonSouhaitee());
        commande.setNotes(dto.getNotes());

        // Mise à jour des lignes
        commande.getLignes().clear();
        if (dto.getLignes() != null) {
            for (LigneCommandeDTO ligneDTO : dto.getLignes()) {
                Article article = articleRepository.findById(ligneDTO.getArticleId())
                        .orElseThrow(() -> new RuntimeException("Article non trouvé"));
                LigneCommande ligne = new LigneCommande();
                ligne.setCommande(commande);
                ligne.setArticle(article);
                ligne.setQuantite(ligneDTO.getQuantite());
                ligne.setPrixUnitaire(ligneDTO.getPrixUnitaire() != null
                        ? ligneDTO.getPrixUnitaire()
                        : article.getPrixUnitaire());
                commande.getLignes().add(ligne);
            }
        }

        commande.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(commandeRepository.save(commande));
    }

    @Transactional
    public void deleteCommande(Long id) {
        commandeRepository.deleteById(id);
    }

    @Transactional
    public CommandeDTO updateStatut(Long id, StatutCommande statut) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        // Génération des tâches de picking si on passe à VALIDEE
        if (statut == StatutCommande.VALIDEE && commande.getStatut() != StatutCommande.VALIDEE) {
            pickingService.generatePickingTasks(id);
        }

        // ✅ Décrémentation du stock si on passe à EXPEDIEE
        if (statut == StatutCommande.EXPEDIEE && commande.getStatut() != StatutCommande.EXPEDIEE) {
            for (LigneCommande ligne : commande.getLignes()) {
                stockService.decrementStock(ligne.getArticle().getId(), ligne.getQuantite());
            }
        }

        commande.setStatut(statut);
        commande.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(commandeRepository.save(commande));
    }

    private String generateNumeroCommande() {
        return "CMD-" + System.currentTimeMillis();
    }

    private CommandeDTO convertToDTO(Commande commande) {
        CommandeDTO dto = new CommandeDTO();
        dto.setId(commande.getId());
        dto.setNumeroCommande(commande.getNumeroCommande());
        dto.setClientId(commande.getClient().getId());
        dto.setClientNom(commande.getClient().getNom() + " " + commande.getClient().getPrenom());
        dto.setDateCommande(commande.getDateCommande());
        dto.setDateLivraisonSouhaitee(commande.getDateLivraisonSouhaitee());
        dto.setStatut(commande.getStatut());
        dto.setNotes(commande.getNotes());
        dto.setCreatedAt(commande.getCreatedAt());
        dto.setUpdatedAt(commande.getUpdatedAt());

        if (commande.getLignes() != null) {
            dto.setLignes(commande.getLignes().stream()
                    .map(this::convertLigneToDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private LigneCommandeDTO convertLigneToDTO(LigneCommande ligne) {
        LigneCommandeDTO dto = new LigneCommandeDTO();
        dto.setId(ligne.getId());
        dto.setCommandeId(ligne.getCommande().getId());
        dto.setArticleId(ligne.getArticle().getId());
        dto.setArticleCode(ligne.getArticle().getCode());
        dto.setArticleDesignation(ligne.getArticle().getDesignation());
        dto.setQuantite(ligne.getQuantite());
        dto.setPrixUnitaire(ligne.getPrixUnitaire());
        dto.setStatutPreparation(ligne.getStatutPreparation());
        return dto;
    }
}