package com.example.pfe.admin.service;

import com.example.pfe.admin.dto.UserDTO;
import com.example.pfe.auth.entity.Role;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
import com.example.pfe.notification.enums.NotificationType;
import com.example.pfe.notification.service.NotificationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WarehouseRepository warehouseRepository;
    private final NotificationService notificationService;

    public AdminService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        WarehouseRepository warehouseRepository,
                        NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.warehouseRepository = warehouseRepository;
        this.notificationService = notificationService;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO activerCompte(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setEstActif(true);
        User updatedUser = userRepository.save(user);

        // 🔔 NOTIFICATION : Compte activé (pour l'utilisateur concerné)
        try {
            notificationService.createNotification(
                    userId,
                    "✅ Compte activé",
                    String.format("Votre compte a été activé. Vous pouvez maintenant vous connecter avec le rôle: %s.",
                            user.getRole().name()),
                    NotificationType.SUCCES,
                    "/login",
                    userId,
                    "USER"
            );
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }

        return convertToDTO(updatedUser);
    }

    @Transactional
    public UserDTO desactiverCompte(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setEstActif(false);
        User updatedUser = userRepository.save(user);

        // 🔔 NOTIFICATION : Compte désactivé (pour l'utilisateur concerné)
        try {
            notificationService.createNotification(
                    userId,
                    "⚠️ Compte désactivé",
                    "Votre compte a été désactivé. Contactez l'administrateur pour plus d'informations.",
                    NotificationType.ALERTE,
                    "/login",
                    userId,
                    "USER"
            );
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }

        return convertToDTO(updatedUser);
    }

    // 🔹 MODIFIÉE : ajout du paramètre entrepotId
    @Transactional
    public UserDTO updateUserRole(Long userId, String newRole, Long entrepotId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String ancienRole = user.getRole().name();

        try {
            Role role = Role.valueOf(newRole);
            user.setRole(role);
            // Si on change le rôle et que le rôle n'est pas OPERATOR, on active automatiquement
            if (!user.isEstActif() && role != Role.OPERATOR) {
                user.setEstActif(true);
            }

            // 🔹 NOUVEAU : assigner ou retirer l'entrepôt
            if (entrepotId != null) {
                Warehouse warehouse = warehouseRepository.findById(entrepotId)
                        .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));
                user.setEntrepot(warehouse);
            } else {
                user.setEntrepot(null);
            }

            User updatedUser = userRepository.save(user);

            // 🔔 NOTIFICATION : Changement de rôle (pour l'utilisateur concerné)
            try {
                notificationService.createNotification(
                        userId,
                        "🔄 Rôle modifié",
                        String.format("Votre rôle a été changé de %s à %s. Veuillez vous reconnecter.",
                                ancienRole, newRole),
                        NotificationType.INFO,
                        "/login",
                        userId,
                        "USER"
                );
            } catch (Exception e) {
                System.out.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
            }

            return convertToDTO(updatedUser);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rôle invalide: " + newRole);
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        userRepository.deleteById(userId);
    }

    public List<String> getAllRoles() {
        return Arrays.stream(Role.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    // 🔹 MODIFIÉE : ajout de l'entrepôt dans la création
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setNom(userDTO.getNom());
        user.setPrenom(userDTO.getPrenom());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(Role.valueOf(userDTO.getRole()));
        user.setEstActif(true);

        // 🔹 NOUVEAU : assigner l'entrepôt si fourni
        if (userDTO.getEntrepotId() != null) {
            Warehouse warehouse = warehouseRepository.findById(userDTO.getEntrepotId())
                    .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));
            user.setEntrepot(warehouse);
        }

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    // 🔹 MODIFIÉE : ajout des informations entrepôt dans le DTO
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setEstActif(user.isEstActif());
        dto.setCreatedAt(user.getCreatedAt());

        // 🔹 NOUVEAU : ajouter les infos de l'entrepôt si présent
        if (user.getEntrepot() != null) {
            dto.setEntrepotId(user.getEntrepot().getId());
            dto.setEntrepotNom(user.getEntrepot().getNom());
        }

        return dto;
    }
}