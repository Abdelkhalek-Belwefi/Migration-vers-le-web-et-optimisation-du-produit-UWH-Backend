package com.example.pfe.notification.dto;

import com.example.pfe.notification.enums.NotificationStatut;
import com.example.pfe.notification.enums.NotificationType;
import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id;
    private String titre;
    private String message;
    private NotificationType type;
    private NotificationStatut statut;
    private Long destinataireId;
    private String lienAction;
    private Long entiteId;
    private String entiteType;
    private LocalDateTime createdAt;

    // Constructeurs
    public NotificationDTO() {}

    public NotificationDTO(Long id, String titre, String message, NotificationType type,
                           NotificationStatut statut, Long destinataireId, String lienAction,
                           Long entiteId, String entiteType, LocalDateTime createdAt) {
        this.id = id;
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.statut = statut;
        this.destinataireId = destinataireId;
        this.lienAction = lienAction;
        this.entiteId = entiteId;
        this.entiteType = entiteType;
        this.createdAt = createdAt;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public NotificationStatut getStatut() { return statut; }
    public void setStatut(NotificationStatut statut) { this.statut = statut; }

    public Long getDestinataireId() { return destinataireId; }
    public void setDestinataireId(Long destinataireId) { this.destinataireId = destinataireId; }

    public String getLienAction() { return lienAction; }
    public void setLienAction(String lienAction) { this.lienAction = lienAction; }

    public Long getEntiteId() { return entiteId; }
    public void setEntiteId(Long entiteId) { this.entiteId = entiteId; }

    public String getEntiteType() { return entiteType; }
    public void setEntiteType(String entiteType) { this.entiteType = entiteType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}