package com.example.pfe.notification.controller;

import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.notification.dto.NotificationDTO;
import com.example.pfe.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            User user = userRepository.findByEmail(email).orElse(null);
            return user != null ? user.getId() : null;
        }
        return null;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getMesNotifications() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }

    @GetMapping("/non-lues")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getNonLues() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notificationService.getNonLuesByUser(userId));
        response.put("count", notificationService.getNonLuCount(userId));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/lu")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationDTO> marquerCommeLu(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.marquerCommeLu(id));
    }

    @PutMapping("/marquer-tout-lu")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marquerToutCommeLu() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        notificationService.marquerToutCommeLu(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tout-supprimer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAllNotifications() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        notificationService.deleteAllNotificationsByUser(userId);
        return ResponseEntity.ok().build();
    }
}