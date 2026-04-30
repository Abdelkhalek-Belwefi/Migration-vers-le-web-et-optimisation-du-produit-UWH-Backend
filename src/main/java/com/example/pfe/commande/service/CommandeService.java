package com.example.pfe.commande.service;

import com.example.pfe.article.entity.Article;
import com.example.pfe.article.repository.ArticleRepository;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.client.entity.Client;
import com.example.pfe.client.repository.ClientRepository;
import com.example.pfe.commande.dto.CommandeDTO;
import com.example.pfe.commande.dto.LigneCommandeDTO;
import com.example.pfe.commande.entity.Commande;
import com.example.pfe.commande.entity.LigneCommande;
import com.example.pfe.commande.enums.StatutCommande;
import com.example.pfe.commande.enums.TypeCommande;
import com.example.pfe.commande.repository.CommandeRepository;
import com.example.pfe.commande.repository.LigneCommandeRepository;
import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
import com.example.pfe.picking.service.PickingService;
import com.example.pfe.stock.service.StockService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final StockService stockService;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;

    public CommandeService(CommandeRepository commandeRepository,
                           LigneCommandeRepository ligneCommandeRepository,
                           ClientRepository clientRepository,
                           ArticleRepository articleRepository,
                           PickingService pickingService,
                           StockService stockService,
                           UserRepository userRepository,
                           WarehouseRepository warehouseRepository) {
        this.commandeRepository = commandeRepository;
        this.ligneCommandeRepository = ligneCommandeRepository;
        this.clientRepository = clientRepository;
        this.articleRepository = articleRepository;
        this.pickingService = pickingService;
        this.stockService = stockService;
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
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

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        Warehouse userEntrepot = currentUser.getEntrepot();
        if (userEntrepot == null) {
            throw new RuntimeException("Impossible de créer une commande : utilisateur non lié à un entrepôt");
        }

        Commande commande = new Commande();
        commande.setClient(client);
        commande.setNumeroCommande(generateNumeroCommande());
        commande.setDateLivraisonSouhaitee(dto.getDateLivraisonSouhaitee());
        commande.setNotes(dto.getNotes());
        commande.setStatut(StatutCommande.EN_ATTENTE);
        commande.setEntrepot(userEntrepot);
        commande.setTypeCommande(TypeCommande.CLIENT);

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

    // ========== MÉTHODE UPDATE STATUT CORRIGÉE AVEC GESTION DES TRANSFERTS ==========

    @Transactional
    public CommandeDTO updateStatut(Long id, StatutCommande statut) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        System.out.println("📝 Mise à jour statut: " + commande.getStatut() + " -> " + statut + " | Type: " + commande.getTypeCommande());

        // Gestion spécifique pour les commandes de transfert
        if (commande.getTypeCommande() == TypeCommande.TRANSFERT) {
            // EN_PREPARATION -> VALIDEE (préparation terminée)
            if (statut == StatutCommande.VALIDEE && commande.getStatut() == StatutCommande.EN_PREPARATION) {
                commande.setStatut(StatutCommande.VALIDEE);
                System.out.println("📦 Commande de transfert: préparation terminée, passage à VALIDEE");
            } else {
                commande.setStatut(statut);
            }
        } else {
            // Gestion pour les commandes client
            if (statut == StatutCommande.VALIDEE && commande.getStatut() != StatutCommande.VALIDEE) {
                pickingService.generatePickingTasks(id);

                Long commandeEntrepotId = commande.getEntrepot().getId();
                for (LigneCommande ligne : commande.getLignes()) {
                    String lot = ligne.getArticle().getLotDefaut();
                    if (lot == null || lot.isEmpty()) {
                        lot = "DEFAULT";
                    }
                    stockService.decrementStock(ligne.getArticle().getId(), ligne.getQuantite(), commandeEntrepotId, lot);
                }
                System.out.println("📦 Stock diminué pour la commande client " + commande.getNumeroCommande());
                commande.setStatut(statut);
            } else {
                commande.setStatut(statut);
            }
        }

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

        if (commande.getClient() != null) {
            dto.setClientId(commande.getClient().getId());
            dto.setClientNom(commande.getClient().getNom() + " " + commande.getClient().getPrenom());
        }

        dto.setDateCommande(commande.getDateCommande());
        dto.setDateLivraisonSouhaitee(commande.getDateLivraisonSouhaitee());
        dto.setStatut(commande.getStatut());
        dto.setNotes(commande.getNotes());
        dto.setCreatedAt(commande.getCreatedAt());
        dto.setUpdatedAt(commande.getUpdatedAt());
        dto.setTypeCommande(commande.getTypeCommande());

        if (commande.getEntrepotSource() != null) {
            dto.setEntrepotSourceId(commande.getEntrepotSource().getId());
        }
        if (commande.getEntrepotDestination() != null) {
            dto.setEntrepotDestinationId(commande.getEntrepotDestination().getId());
        }

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

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email: " + email));
        }
        return null;
    }

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

    public List<CommandeDTO> getAllCommandesFiltered() {
        Long entrepotId = getCurrentUserEntrepotId();
        List<Commande> commandes;
        if (entrepotId != null) {
            commandes = commandeRepository.findByEntrepotId(entrepotId);
        } else {
            commandes = commandeRepository.findAll();
        }
        return commandes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<CommandeDTO> getCommandesByStatutFiltered(StatutCommande statut) {
        Long entrepotId = getCurrentUserEntrepotId();
        List<Commande> commandes;
        if (entrepotId != null) {
            commandes = commandeRepository.findByStatutAndEntrepotId(statut, entrepotId);
        } else {
            commandes = commandeRepository.findByStatut(statut);
        }
        return commandes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<CommandeDTO> getCommandesAExpedierFiltered() {
        Long entrepotId = getCurrentUserEntrepotId();
        List<Commande> commandes;
        if (entrepotId != null) {
            commandes = commandeRepository.findByStatutAndEntrepotIdAndNoExpedition(StatutCommande.VALIDEE, entrepotId);
        } else {
            commandes = commandeRepository.findByStatutAndNoExpedition(StatutCommande.VALIDEE);
        }
        return commandes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public CommandeDTO createCommandeWithEntrepot(CommandeDTO dto) {
        Long entrepotId = getCurrentUserEntrepotId();
        if (entrepotId == null) {
            throw new RuntimeException("Impossible de créer une commande : utilisateur non lié à un entrepôt");
        }

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        Commande commande = new Commande();
        commande.setClient(client);
        commande.setNumeroCommande(generateNumeroCommande());
        commande.setDateLivraisonSouhaitee(dto.getDateLivraisonSouhaitee());
        commande.setNotes(dto.getNotes());
        commande.setStatut(StatutCommande.EN_ATTENTE);
        commande.setTypeCommande(TypeCommande.CLIENT);

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

    // ========== NOUVELLES MÉTHODES POUR TRANSFERT ENTRE ENTREPÔTS ==========

    @Transactional
    public CommandeDTO createCommandeTransfert(CommandeDTO dto) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        Warehouse entrepotDestination = currentUser.getEntrepot();
        if (entrepotDestination == null) {
            throw new RuntimeException("Impossible de créer une demande : utilisateur non lié à un entrepôt");
        }

        Warehouse entrepotSource = warehouseRepository.findById(dto.getEntrepotSourceId())
                .orElseThrow(() -> new RuntimeException("Entrepôt source non trouvé"));

        Commande commande = new Commande();
        commande.setNumeroCommande(generateNumeroCommande());
        commande.setDateLivraisonSouhaitee(dto.getDateLivraisonSouhaitee());
        commande.setNotes(dto.getNotes());
        commande.setStatut(StatutCommande.EN_ATTENTE);
        commande.setTypeCommande(TypeCommande.TRANSFERT);
        commande.setEntrepotSource(entrepotSource);
        commande.setEntrepotDestination(entrepotDestination);
        commande.setEntrepot(entrepotSource);

        Commande savedCommande = commandeRepository.save(commande);

        if (dto.getLignes() != null) {
            for (LigneCommandeDTO ligneDTO : dto.getLignes()) {
                Article article = articleRepository.findById(ligneDTO.getArticleId())
                        .orElseThrow(() -> new RuntimeException("Article non trouvé"));
                LigneCommande ligne = new LigneCommande();
                ligne.setCommande(savedCommande);
                ligne.setArticle(article);
                ligne.setQuantite(ligneDTO.getQuantite());
                ligne.setPrixUnitaire(0.0);
                savedCommande.getLignes().add(ligne);
            }
            commandeRepository.save(savedCommande);
        }
        return convertToDTO(savedCommande);
    }

    public List<CommandeDTO> getCommandesTransfertRecues() {
        Long entrepotId = getCurrentUserEntrepotId();
        if (entrepotId == null) {
            throw new RuntimeException("Utilisateur non lié à un entrepôt");
        }

        List<Commande> commandes = commandeRepository.findByTypeCommandeAndEntrepotDestinationId(
                TypeCommande.TRANSFERT, entrepotId);

        return commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.EN_ATTENTE)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommandeDTO accepterDemandeTransfert(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (commande.getTypeCommande() != TypeCommande.TRANSFERT) {
            throw new RuntimeException("Cette commande n'est pas un transfert");
        }

        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new RuntimeException("Cette demande a déjà été traitée");
        }

        // Passage en EN_PREPARATION (prêt à être préparé par l'opérateur)
        commande.setStatut(StatutCommande.EN_PREPARATION);
        commande.setUpdatedAt(LocalDateTime.now());

        System.out.println("✅ Demande de transfert acceptée, passage en EN_PREPARATION");

        return convertToDTO(commandeRepository.save(commande));
    }

    @Transactional
    public CommandeDTO refuserDemandeTransfert(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (commande.getTypeCommande() != TypeCommande.TRANSFERT) {
            throw new RuntimeException("Cette commande n'est pas un transfert");
        }

        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new RuntimeException("Cette demande a déjà été traitée");
        }

        commande.setStatut(StatutCommande.ANNULEE);
        commande.setUpdatedAt(LocalDateTime.now());

        return convertToDTO(commandeRepository.save(commande));
    }

    public List<CommandeDTO> getCommandesTransfertSource() {
        Long entrepotId = getCurrentUserEntrepotId();
        if (entrepotId == null) {
            throw new RuntimeException("Utilisateur non lié à un entrepôt");
        }

        List<Commande> commandes = commandeRepository.findByTypeCommandeAndEntrepotSourceId(
                TypeCommande.TRANSFERT, entrepotId);

        return commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.EN_ATTENTE)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CommandeDTO> getCommandesTransfertAPreparer() {
        Long entrepotId = getCurrentUserEntrepotId();
        if (entrepotId == null) {
            throw new RuntimeException("Utilisateur non lié à un entrepôt");
        }

        // Récupère les commandes où l'entrepôt SOURCE = entrepôt de l'utilisateur
        // ET le statut = EN_PREPARATION (prêt à être préparé)
        List<Commande> commandes = commandeRepository.findByTypeCommandeAndEntrepotSourceIdAndStatut(
                TypeCommande.TRANSFERT, entrepotId, StatutCommande.EN_PREPARATION);

        System.out.println("📋 Commandes de transfert à préparer trouvées: " + commandes.size());

        return commandes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}