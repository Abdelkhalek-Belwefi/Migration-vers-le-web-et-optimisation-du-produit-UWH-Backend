package com.example.pfe.notification.service;

import com.example.pfe.notification.dto.NotificationDTO;
import com.example.pfe.notification.entity.Notification;
import com.example.pfe.notification.enums.NotificationStatut;
import com.example.pfe.notification.enums.NotificationType;
import com.example.pfe.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationDTO createNotification(Long destinataireId, String titre, String message,
                                              NotificationType type, String lienAction,
                                              Long entiteId, String entiteType) {
        Notification notification = new Notification();
        notification.setDestinataireId(destinataireId);
        notification.setTitre(titre);
        notification.setMessage(message);
        notification.setType(type);
        notification.setStatut(NotificationStatut.NON_LU);
        notification.setLienAction(lienAction);
        notification.setEntiteId(entiteId);
        notification.setEntiteType(entiteType);

        Notification saved = notificationRepository.save(notification);

        // Envoyer via WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + destinataireId, convertToDTO(saved));
        } catch (Exception e) {
            System.out.println("WebSocket non disponible: " + e.getMessage());
        }

        return convertToDTO(saved);
    }

    public List<NotificationDTO> getNotificationsByUser(Long userId) {
        return notificationRepository.findByDestinataireIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getNonLuesByUser(Long userId) {
        return notificationRepository.findByDestinataireIdAndStatutOrderByCreatedAtDesc(userId, NotificationStatut.NON_LU)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long getNonLuCount(Long userId) {
        return notificationRepository.countByDestinataireIdAndStatut(userId, NotificationStatut.NON_LU);
    }

    @Transactional
    public NotificationDTO marquerCommeLu(Long notificationId) {
        notificationRepository.updateStatut(notificationId, NotificationStatut.LU);
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        return notification != null ? convertToDTO(notification) : null;
    }

    @Transactional
    public void marquerToutCommeLu(Long userId) {
        notificationRepository.marquerToutCommeLu(userId, NotificationStatut.LU);
    }

    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Transactional
    public void deleteAllNotificationsByUser(Long userId) {
        List<Notification> notifications = notificationRepository.findByDestinataireIdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(notifications);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getTitre(),
                notification.getMessage(),
                notification.getType(),
                notification.getStatut(),
                notification.getDestinataireId(),
                notification.getLienAction(),
                notification.getEntiteId(),
                notification.getEntiteType(),
                notification.getCreatedAt()
        );
    }
}