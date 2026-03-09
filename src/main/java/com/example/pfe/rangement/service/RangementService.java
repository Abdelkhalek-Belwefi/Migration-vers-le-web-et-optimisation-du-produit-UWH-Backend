package com.example.pfe.rangement.service;

import com.example.pfe.reception.dto.PutawayTaskDTO;
import com.example.pfe.reception.entity.PutawayTask;
import com.example.pfe.reception.repository.PutawayTaskRepository;
import com.example.pfe.stock.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RangementService {

    private final PutawayTaskRepository putawayTaskRepository;
    private final StockService stockService;

    public RangementService(PutawayTaskRepository putawayTaskRepository,
                            StockService stockService) {
        this.putawayTaskRepository = putawayTaskRepository;
        this.stockService = stockService;
    }

    /**
     * Récupère toutes les tâches de rangement à faire
     */
    public List<PutawayTaskDTO> getTasksAFaire() {
        return putawayTaskRepository.findByStatut("A_FAIRE").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les tâches de rangement (pour supervision)
     */
    public List<PutawayTaskDTO> getAllTasks() {
        return putawayTaskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les tâches par statut
     */
    public List<PutawayTaskDTO> getTasksByStatut(String statut) {
        return putawayTaskRepository.findByStatut(statut).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * L'opérateur commence une tâche de rangement
     */
    @Transactional
    public PutawayTaskDTO commencerTask(Long taskId) {
        PutawayTask task = putawayTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

        if (!"A_FAIRE".equals(task.getStatut())) {
            throw new RuntimeException("Cette tâche n'est pas à faire");
        }

        task.setStatut("EN_COURS");
        PutawayTask saved = putawayTaskRepository.save(task);
        return convertToDTO(saved);
    }

    /**
     * L'opérateur termine une tâche de rangement
     */
    @Transactional
    public PutawayTaskDTO terminerTask(Long taskId, String emplacementReel) {
        PutawayTask task = putawayTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

        if (!"EN_COURS".equals(task.getStatut())) {
            throw new RuntimeException("Cette tâche doit être en cours");
        }

        // Vérifier que l'emplacement correspond (optionnel)
        if (emplacementReel != null && !emplacementReel.equals(task.getEmplacementDestination())) {
            throw new RuntimeException("L'emplacement scanné ne correspond pas à la destination prévue");
        }

        task.setStatut("TERMINEE");
        task.setCompletedAt(LocalDateTime.now());

        // Si un emplacement réel est fourni, on le met à jour
        if (emplacementReel != null) {
            task.setEmplacementDestination(emplacementReel);
        }

        PutawayTask saved = putawayTaskRepository.save(task);
        return convertToDTO(saved);
    }

    /**
     * Convertit une entité en DTO
     */
    private PutawayTaskDTO convertToDTO(PutawayTask task) {
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
}