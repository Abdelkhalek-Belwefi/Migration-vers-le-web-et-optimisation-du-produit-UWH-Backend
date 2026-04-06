package com.example.pfe.picking.service;

import com.example.pfe.article.repository.ArticleRepository;
import com.example.pfe.commande.entity.Commande;
import com.example.pfe.commande.entity.LigneCommande;
import com.example.pfe.commande.repository.CommandeRepository;
import com.example.pfe.commande.repository.LigneCommandeRepository;
import com.example.pfe.picking.dto.PickingTaskDTO;
import com.example.pfe.picking.entity.PickingTask;
import com.example.pfe.picking.enums.StatutPicking;
import com.example.pfe.picking.repository.PickingTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PickingService {

    private final PickingTaskRepository pickingTaskRepository;
    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final ArticleRepository articleRepository;

    public PickingService(PickingTaskRepository pickingTaskRepository,
                          CommandeRepository commandeRepository,
                          LigneCommandeRepository ligneCommandeRepository,
                          ArticleRepository articleRepository) {
        this.pickingTaskRepository = pickingTaskRepository;
        this.commandeRepository = commandeRepository;
        this.ligneCommandeRepository = ligneCommandeRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional
    public void generatePickingTasks(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        if (commande.getLignes() != null && !commande.getLignes().isEmpty()) {
            for (LigneCommande ligne : commande.getLignes()) {
                PickingTask task = new PickingTask();
                task.setCommande(commande);
                task.setLigneCommande(ligne);
                task.setArticle(ligne.getArticle());
                task.setQuantiteCommandee(ligne.getQuantite());
                task.setQuantitePicked(0);
                task.setStatut(StatutPicking.A_PREPARER);
                pickingTaskRepository.save(task);
            }
        }
    }

    public List<PickingTaskDTO> getTasksForOperator(String operatorId) {
        return pickingTaskRepository.findByAssignedToAndStatut(operatorId, StatutPicking.A_PREPARER).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PickingTaskDTO> getUnassignedTasks() {
        return pickingTaskRepository.findByStatut(StatutPicking.A_PREPARER).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PickingTaskDTO assignTask(Long taskId, String operatorId) {
        PickingTask task = pickingTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        task.setAssignedTo(operatorId);
        task.setStatut(StatutPicking.EN_COURS);
        task.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(pickingTaskRepository.save(task));
    }

    @Transactional
    public PickingTaskDTO pickQuantity(Long taskId, int pickedQty) {
        PickingTask task = pickingTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        if (task.getStatut() == StatutPicking.TERMINE) {
            throw new RuntimeException("Cette tâche est déjà terminée");
        }
        int newPicked = task.getQuantitePicked() + pickedQty;
        if (newPicked > task.getQuantiteCommandee()) {
            throw new RuntimeException("Quantité prélevée supérieure à la commande");
        }
        task.setQuantitePicked(newPicked);
        task.setStatut(newPicked >= task.getQuantiteCommandee() ? StatutPicking.TERMINE : StatutPicking.EN_COURS);
        task.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(pickingTaskRepository.save(task));
    }

    private PickingTaskDTO convertToDTO(PickingTask task) {
        PickingTaskDTO dto = new PickingTaskDTO();
        dto.setId(task.getId());
        dto.setCommandeId(task.getCommande().getId());
        dto.setNumeroCommande(task.getCommande().getNumeroCommande());
        dto.setLigneCommandeId(task.getLigneCommande().getId());
        dto.setArticleId(task.getArticle().getId());
        dto.setArticleCode(task.getArticle().getCode());
        dto.setArticleDesignation(task.getArticle().getDesignation());
        dto.setQuantiteCommandee(task.getQuantiteCommandee());
        dto.setQuantitePicked(task.getQuantitePicked());
        dto.setStatut(task.getStatut());
        dto.setAssignedTo(task.getAssignedTo());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }
}