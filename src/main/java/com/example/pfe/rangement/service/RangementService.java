package com.example.pfe.rangement.service;

import com.example.pfe.reception.dto.PutawayTaskDTO;
import com.example.pfe.reception.entity.PutawayTask;
import com.example.pfe.reception.repository.PutawayTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RangementService {

    private final PutawayTaskRepository taskRepository;

    public RangementService(PutawayTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<PutawayTaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PutawayTaskDTO> getTasksByStatut(String statut) {
        return taskRepository.findByStatut(statut).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PutawayTaskDTO> getTasksAFaire() {
        return getTasksByStatut("A_FAIRE");
    }

    @Transactional
    public PutawayTaskDTO commencerTask(Long id) {
        PutawayTask task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        if (!"A_FAIRE".equals(task.getStatut())) {
            throw new RuntimeException("Seules les tâches à faire peuvent être commencées");
        }
        task.setStatut("EN_COURS");
        task.setCompletedAt(null);
        return convertToDTO(taskRepository.save(task));
    }

    @Transactional
    public PutawayTaskDTO terminerTask(Long id, String emplacementReel) {
        PutawayTask task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        if (!"EN_COURS".equals(task.getStatut())) {
            throw new RuntimeException("Seules les tâches en cours peuvent être terminées");
        }
        task.setStatut("TERMINEE");
        task.setCompletedAt(LocalDateTime.now());
        if (emplacementReel != null && !emplacementReel.isEmpty()) {
            task.setEmplacementDestination(emplacementReel);
        }
        return convertToDTO(taskRepository.save(task));
    }

    private PutawayTaskDTO convertToDTO(PutawayTask task) {
        PutawayTaskDTO dto = new PutawayTaskDTO();
        dto.setId(task.getId());
        dto.setArticleId(task.getArticle().getId());
        // dto.setArticleCode(task.getArticle().getCode()); // supprimé car champ absent
        dto.setArticleDesignation(task.getArticle().getDesignation());
        dto.setLot(task.getLot());
        dto.setQuantite(task.getQuantite());
        dto.setEmplacementSource(task.getEmplacementSource());
        dto.setEmplacementDestination(task.getEmplacementDestination());
        dto.setStatut(task.getStatut());
        dto.setReceptionId(task.getReception() != null ? task.getReception().getId() : null);
        dto.setCreatedAt(task.getCreatedAt());
        // dto.setCompletedAt(task.getCompletedAt()); // champ absent du DTO
        return dto;
    }
}