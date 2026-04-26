package com.example.pfe.rangement.service;

import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.reception.dto.PutawayTaskDTO;
import com.example.pfe.reception.entity.PutawayTask;
import com.example.pfe.reception.repository.PutawayTaskRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RangementService {

    private final PutawayTaskRepository taskRepository;
    private final UserRepository userRepository;  // ← NOUVEAU

    public RangementService(PutawayTaskRepository taskRepository,
                            UserRepository userRepository) {  // ← NOUVEAU
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // ========== MÉTHODES EXISTANTES (INCHANGÉES) ==========

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

    // ========== NOUVELLES MÉTHODES POUR LE FILTRAGE PAR ENTREPÔT ==========

    /**
     * Récupère l'utilisateur connecté
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email: " + email));
        }
        return null;
    }

    /**
     * Récupère l'ID de l'entrepôt de l'utilisateur connecté
     */
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

    /**
     * Récupère toutes les tâches (filtrées par entrepôt)
     */
    public List<PutawayTaskDTO> getAllTasksFiltered() {
        Long entrepotId = getCurrentUserEntrepotId();
        List<PutawayTask> tasks;
        if (entrepotId != null) {
            tasks = taskRepository.findByEntrepotId(entrepotId);
        } else {
            tasks = taskRepository.findAll();
        }
        return tasks.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Récupère les tâches par statut (filtrées par entrepôt)
     */
    public List<PutawayTaskDTO> getTasksByStatutFiltered(String statut) {
        Long entrepotId = getCurrentUserEntrepotId();
        List<PutawayTask> tasks;
        if (entrepotId != null) {
            tasks = taskRepository.findByStatutAndEntrepotId(statut, entrepotId);
        } else {
            tasks = taskRepository.findByStatut(statut);
        }
        return tasks.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Récupère les tâches à faire (filtrées par entrepôt)
     */
    public List<PutawayTaskDTO> getTasksAFaireFiltered() {
        return getTasksByStatutFiltered("A_FAIRE");
    }

    /**
     * Récupère les tâches d'une réception (filtrées par entrepôt)
     */
    public List<PutawayTaskDTO> getTasksByReceptionFiltered(Long receptionId) {
        Long entrepotId = getCurrentUserEntrepotId();
        List<PutawayTask> tasks;
        if (entrepotId != null) {
            tasks = taskRepository.findByReceptionIdAndEntrepotId(receptionId, entrepotId);
        } else {
            tasks = taskRepository.findByReceptionId(receptionId);
        }
        return tasks.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}