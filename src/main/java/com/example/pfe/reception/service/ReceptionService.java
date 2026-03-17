package com.example.pfe.reception.service;

import com.example.pfe.article.entity.Article;
import com.example.pfe.article.repository.ArticleRepository;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.reception.dto.PutawayTaskDTO;
import com.example.pfe.reception.dto.ReceptionDTO;
import com.example.pfe.reception.dto.ReceptionLineDTO;
import com.example.pfe.reception.entity.PutawayTask;
import com.example.pfe.reception.entity.Reception;
import com.example.pfe.reception.entity.ReceptionLine;
import com.example.pfe.reception.entity.ReceptionStatut;
import com.example.pfe.reception.repository.PutawayTaskRepository;
import com.example.pfe.reception.repository.ReceptionLineRepository;
import com.example.pfe.reception.repository.ReceptionRepository;
import com.example.pfe.stock.service.StockService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReceptionService {

    private final ReceptionRepository receptionRepository;
    private final ReceptionLineRepository receptionLineRepository;
    private final PutawayTaskRepository putawayTaskRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final StockService stockService;

    public ReceptionService(ReceptionRepository receptionRepository,
                            ReceptionLineRepository receptionLineRepository,
                            PutawayTaskRepository putawayTaskRepository,
                            ArticleRepository articleRepository,
                            UserRepository userRepository,
                            StockService stockService) {
        this.receptionRepository = receptionRepository;
        this.receptionLineRepository = receptionLineRepository;
        this.putawayTaskRepository = putawayTaskRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.stockService = stockService;
    }

    // ==================== CRÉATION ====================

    @Transactional
    public ReceptionDTO createReception(ReceptionDTO receptionDTO) {
        System.out.println("=== CRÉATION RÉCEPTION ===");
        System.out.println("Numéro PO: " + receptionDTO.getNumeroPO());

        // 🔴 MODIFICATION : on vérifie s'il existe déjà, mais on autorise la création
        List<Reception> existingList = receptionRepository.findByNumeroPO(receptionDTO.getNumeroPO());
        if (!existingList.isEmpty()) {
            System.out.println("⚠️ Le PO " + receptionDTO.getNumeroPO() + " existe déjà (IDs: "
                    + existingList.stream().map(r -> r.getId().toString()).collect(Collectors.joining(", "))
                    + "). Création d'une nouvelle réception.");
        }

        User currentUser = getCurrentUser();

        Reception reception = new Reception();
        reception.setNumeroPO(receptionDTO.getNumeroPO());
        reception.setDateReception(receptionDTO.getDateReception() != null ?
                receptionDTO.getDateReception() : LocalDateTime.now());
        reception.setStatut(ReceptionStatut.EN_ATTENTE);
        reception.setFournisseur(receptionDTO.getFournisseur());
        reception.setBonLivraison(receptionDTO.getBonLivraison());
        reception.setCreateur(currentUser);

        Reception savedReception = receptionRepository.save(reception);
        System.out.println("✅ Réception créée avec ID: " + savedReception.getId());

        if (receptionDTO.getLignes() != null && !receptionDTO.getLignes().isEmpty()) {
            for (ReceptionLineDTO lineDTO : receptionDTO.getLignes()) {
                addLineToReception(savedReception.getId(), lineDTO);
            }
        }

        return getReceptionById(savedReception.getId());
    }

    // ==================== CONSULTATION ====================

    public List<ReceptionDTO> getAllReceptions() {
        return receptionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ReceptionDTO getReceptionById(Long id) {
        Reception reception = receptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réception non trouvée avec l'id: " + id));
        return convertToDTO(reception);
    }

    // 🔴 MODIFICATION : gestion de plusieurs réceptions pour un même PO
    public ReceptionDTO getReceptionByPO(String numeroPO) {
        List<Reception> receptions = receptionRepository.findByNumeroPO(numeroPO);

        if (receptions.isEmpty()) {
            throw new RuntimeException("Aucune réception trouvée pour le PO: " + numeroPO);
        }

        if (receptions.size() > 1) {
            System.out.println("⚠️ Plusieurs réceptions trouvées pour le PO " + numeroPO
                    + ". Retour de la plus récente (ID: " + receptions.get(receptions.size()-1).getId() + ")");
            return convertToDTO(receptions.get(receptions.size()-1));
        }

        return convertToDTO(receptions.get(0));
    }

    public List<ReceptionDTO> searchReceptions(String numeroPO, String fournisseur, ReceptionStatut statut) {
        return receptionRepository.searchReceptions(numeroPO, fournisseur, statut).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== GESTION DES LIGNES ====================

    @Transactional
    public ReceptionLineDTO addLineToReception(Long receptionId, ReceptionLineDTO lineDTO) {
        System.out.println("➕ Ajout d'une ligne à la réception " + receptionId);

        Reception reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new RuntimeException("Réception non trouvée"));

        if (reception.getStatut() != ReceptionStatut.EN_ATTENTE) {
            throw new RuntimeException("Impossible d'ajouter une ligne à une réception déjà validée");
        }

        if (lineDTO.getArticleId() == null) {
            throw new RuntimeException("L'ID de l'article est obligatoire");
        }

        Article article = articleRepository.findById(lineDTO.getArticleId())
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'id: " + lineDTO.getArticleId()));

        ReceptionLine line = new ReceptionLine();
        line.setReception(reception);
        line.setArticle(article);
        line.setQuantiteAttendue(lineDTO.getQuantiteAttendue());

        // ✅ CORRIGÉ : Utilisation de > 0 au lieu de != null
        line.setQuantiteRecue(lineDTO.getQuantiteRecue() > 0 ? lineDTO.getQuantiteRecue() : 0);

        line.setLot(lineDTO.getLot());
        line.setDateExpiration(lineDTO.getDateExpiration());
        line.setEmplacementDestination(lineDTO.getEmplacementDestination());
        line.setStatut("EN_ATTENTE");

        ReceptionLine savedLine = receptionLineRepository.save(line);
        System.out.println("✅ Ligne ajoutée avec ID: " + savedLine.getId());

        return convertLineToDTO(savedLine);
    }

    @Transactional
    public ReceptionLineDTO updateReceptionLine(Long lineId, int quantiteRecue, String lot,
                                                LocalDateTime dateExpiration, String emplacement) {
        System.out.println("✏️ Mise à jour de la ligne " + lineId);

        ReceptionLine line = receptionLineRepository.findById(lineId)
                .orElseThrow(() -> new RuntimeException("Ligne de réception non trouvée"));

        Reception reception = line.getReception();
        if (reception.getStatut() != ReceptionStatut.EN_ATTENTE) {
            throw new RuntimeException("Impossible de modifier une ligne d'une réception déjà validée");
        }

        line.setQuantiteRecue(quantiteRecue);
        if (lot != null) line.setLot(lot);
        if (dateExpiration != null) line.setDateExpiration(dateExpiration);
        if (emplacement != null) line.setEmplacementDestination(emplacement);

        if (quantiteRecue >= line.getQuantiteAttendue()) {
            line.setStatut("RECU");
        } else if (quantiteRecue > 0) {
            line.setStatut("PARTIEL");
        } else {
            line.setStatut("EN_ATTENTE");
        }

        ReceptionLine savedLine = receptionLineRepository.save(line);
        return convertLineToDTO(savedLine);
    }

    // ==================== VALIDATION (RESPONSABLE) ====================

    @Transactional
    public ReceptionDTO validerReception(Long receptionId) {
        System.out.println("=== DÉBUT VALIDATION RÉCEPTION " + receptionId + " ===");

        Reception reception = receptionRepository.findById(receptionId)
                .orElseThrow(() -> new RuntimeException("Réception non trouvée avec l'id: " + receptionId));

        System.out.println("Statut actuel: " + reception.getStatut());

        if (reception.getStatut() == ReceptionStatut.VALIDEE) {
            throw new RuntimeException("Cette réception est déjà validée");
        }

        if (reception.getLignes() == null || reception.getLignes().isEmpty()) {
            throw new RuntimeException("Impossible de valider une réception sans articles");
        }

        System.out.println("Nombre de lignes: " + reception.getLignes().size());

        for (ReceptionLine line : reception.getLignes()) {
            if (line.getArticle() == null) {
                throw new RuntimeException("Ligne " + line.getId() + " : article non défini");
            }
            if (line.getStatut() == null || line.getStatut().isEmpty()) {
                System.out.println("⚠️ Ligne " + line.getId() + " sans statut - mise à jour automatique");
                line.setStatut("EN_ATTENTE");
                receptionLineRepository.save(line);
            }
            System.out.println("Ligne " + line.getId() +
                    " - Article: " + line.getArticle().getId() +
                    " - Qté reçue: " + line.getQuantiteRecue() +
                    " - Lot: " + line.getLot() +
                    " - Emplacement: " + line.getEmplacementDestination() +
                    " - Statut: " + line.getStatut());
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        reception.setStatut(ReceptionStatut.VALIDEE);
        reception.setValideur(currentUser);
        reception.setValidatedAt(LocalDateTime.now());

        Reception savedReception = receptionRepository.save(reception);
        System.out.println("✅ Réception marquée comme VALIDEE");

        int tasksCrees = 0;

        for (ReceptionLine line : reception.getLignes()) {
            if (line.getQuantiteRecue() > 0) {
                try {
                    String lot = line.getLot() != null ? line.getLot() : "DEFAULT";
                    String emplacement = line.getEmplacementDestination() != null ?
                            line.getEmplacementDestination() : "ZONE-REC";

                    System.out.println("📦 Mise à jour stock - Article: " + line.getArticle().getId() +
                            ", Lot: " + lot + ", Qté: " + line.getQuantiteRecue() +
                            ", Emplacement: " + emplacement);

                    stockService.augmenterQuantite(
                            line.getArticle().getId(),
                            lot,
                            emplacement,
                            line.getQuantiteRecue(),
                            line.getDateExpiration(),
                            null
                    );

                    if (line.getEmplacementDestination() != null && !line.getEmplacementDestination().isEmpty()) {
                        PutawayTask task = new PutawayTask();
                        task.setArticle(line.getArticle());
                        task.setLot(line.getLot());
                        task.setQuantite(line.getQuantiteRecue());
                        task.setEmplacementSource("ZONE-REC");
                        task.setEmplacementDestination(line.getEmplacementDestination());
                        task.setStatut("A_FAIRE");
                        task.setReception(reception);
                        putawayTaskRepository.save(task);
                        tasksCrees++;
                        System.out.println("📋 Tâche de rangement créée pour l'article " + line.getArticle().getId());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors du traitement de la ligne " + line.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Erreur lors de la validation: " + e.getMessage(), e);
                }
            }
        }

        System.out.println("✅ Validation terminée - " + tasksCrees + " tâches de rangement créées");
        return convertToDTO(savedReception);
    }

    // ==================== TÂCHES DE RANGEMENT ====================

    public List<PutawayTaskDTO> getPutawayTasksByReception(Long receptionId) {
        return putawayTaskRepository.findByReceptionId(receptionId).stream()
                .map(this::convertTaskToDTO)
                .collect(Collectors.toList());
    }

    public List<PutawayTaskDTO> getAllPutawayTasks() {
        return putawayTaskRepository.findAll().stream()
                .map(this::convertTaskToDTO)
                .collect(Collectors.toList());
    }

    // ==================== CONVERSIONS ====================

    private ReceptionDTO convertToDTO(Reception reception) {
        ReceptionDTO dto = new ReceptionDTO();
        dto.setId(reception.getId());
        dto.setNumeroPO(reception.getNumeroPO());
        dto.setDateReception(reception.getDateReception());
        dto.setStatut(reception.getStatut());
        dto.setFournisseur(reception.getFournisseur());
        dto.setBonLivraison(reception.getBonLivraison());

        if (reception.getCreateur() != null) {
            dto.setCreateurNom(reception.getCreateur().getNom() + " " + reception.getCreateur().getPrenom());
        }
        if (reception.getValideur() != null) {
            dto.setValideurNom(reception.getValideur().getNom() + " " + reception.getValideur().getPrenom());
        }

        dto.setCreatedAt(reception.getCreatedAt());
        dto.setValidatedAt(reception.getValidatedAt());

        dto.setLignes(reception.getLignes().stream()
                .map(this::convertLineToDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    private ReceptionLineDTO convertLineToDTO(ReceptionLine line) {
        ReceptionLineDTO dto = new ReceptionLineDTO();
        dto.setId(line.getId());
        dto.setArticleId(line.getArticle().getId());
        dto.setArticleCode(line.getArticle().getCodeArticleERP());
        dto.setArticleDesignation(line.getArticle().getDesignation());
        dto.setQuantiteAttendue(line.getQuantiteAttendue());
        dto.setQuantiteRecue(line.getQuantiteRecue());
        dto.setLot(line.getLot());
        dto.setDateExpiration(line.getDateExpiration());
        dto.setEmplacementDestination(line.getEmplacementDestination());
        dto.setStatut(line.getStatut());
        return dto;
    }

    private PutawayTaskDTO convertTaskToDTO(PutawayTask task) {
        PutawayTaskDTO dto = new PutawayTaskDTO();
        dto.setId(task.getId());
        dto.setArticleId(task.getArticle().getId());
        dto.setArticleDesignation(task.getArticle().getDesignation());
        dto.setLot(task.getLot());
        dto.setQuantite(task.getQuantite());
        dto.setEmplacementSource(task.getEmplacementSource());
        dto.setEmplacementDestination(task.getEmplacementDestination());
        dto.setStatut(task.getStatut());
        dto.setReceptionId(task.getReception() != null ? task.getReception().getId() : null);
        dto.setCreatedAt(task.getCreatedAt());
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

    // 🔹 NOUVELLE MÉTHODE : Traiter un code de document scanné
    public ReceptionDTO getDocumentInfo(String code) {
        System.out.println("🔍 Traitement du document scanné: " + code);

        // Pour l'instant, on fait une recherche simple à partir du code
        // On peut imaginer que le code contient directement le numéro de BL ou de PO
        // Si le code commence par "PO-", on cherche une réception existante
        if (code.toUpperCase().startsWith("PO-")) {
            try {
                return getReceptionByPO(code);
            } catch (RuntimeException e) {
                // Si aucune réception trouvée, on crée un DTO avec le PO
                ReceptionDTO dto = new ReceptionDTO();
                dto.setNumeroPO(code);
                return dto;
            }
        } else if (code.toUpperCase().startsWith("BL-")) {
            // Pour un BL, on crée un DTO avec le numéro de BL
            ReceptionDTO dto = new ReceptionDTO();
            dto.setBonLivraison(code);
            return dto;
        } else {
            // Sinon, on retourne un DTO avec le code comme bon de livraison
            ReceptionDTO dto = new ReceptionDTO();
            dto.setBonLivraison(code);
            return dto;
        }
    }
}