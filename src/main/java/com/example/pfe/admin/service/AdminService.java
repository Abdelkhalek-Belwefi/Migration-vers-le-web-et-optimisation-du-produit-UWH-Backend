package com.example.pfe.admin.service;

import com.example.pfe.admin.dto.UserDTO;
import com.example.pfe.auth.entity.Role;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO activerCompte(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setEstActif(true);
        // Si le rôle est encore EN_ATTENTE, on le garde, l'admin devra le changer
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Transactional
    public UserDTO desactiverCompte(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setEstActif(false);
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Transactional
    public UserDTO updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        try {
            Role role = Role.valueOf(newRole);
            user.setRole(role);
            // Si on change le rôle et que le compte n'est pas actif, on l'active automatiquement
            if (!user.isEstActif() && role != Role.EN_ATTENTE) {
                user.setEstActif(true);
            }
            User updatedUser = userRepository.save(user);
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
        return List.of(
                Role.ADMINISTRATEUR.name(),
                Role.RESPONSABLE_ENTREPOT.name(),
                Role.RECEIVER.name(),
                Role.EFFECTOR_TRANSFERT.name(),
                Role.EN_ATTENTE.name()
        );
    }

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
        user.setEstActif(true); // Si l'admin crée directement, le compte est actif

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setEstActif(user.isEstActif());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}