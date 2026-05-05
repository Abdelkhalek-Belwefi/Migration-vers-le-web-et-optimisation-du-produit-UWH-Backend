package com.example.pfe.auth.service;

import com.example.pfe.auth.dto.AuthResponse;
import com.example.pfe.auth.dto.LoginRequest;
import com.example.pfe.auth.dto.RegisterRequest;
import com.example.pfe.auth.entity.PasswordResetToken;
import com.example.pfe.auth.entity.Role;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.PasswordResetTokenRepository;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.config.JwtService;
import com.example.pfe.notification.enums.NotificationType;
import com.example.pfe.notification.service.NotificationService;
import com.example.pfe.service.EmailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       NotificationService notificationService,
                       PasswordResetTokenRepository tokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.notificationService = notificationService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNumTelephone(request.getNumTelephone());
        user.setRole(Role.OPERATOR);  // Rôle par défaut : en attente
        user.setEstActif(false);       // Compte inactif en attente de validation

        userRepository.save(user);

        // 🔔 NOTIFICATION : Nouvel utilisateur inscrit (pour tous les administrateurs)
        try {
            userRepository.findByRole(Role.ADMINISTRATEUR).forEach(admin -> {
                notificationService.createNotification(
                        admin.getId(),
                        "👤 Nouvel utilisateur inscrit",
                        String.format("%s %s (%s) s'est inscrit et attend la validation.",
                                user.getPrenom(), user.getNom(), user.getEmail()),
                        NotificationType.INFO,
                        "/admin/users",
                        user.getId(),
                        "USER"
                );
            });
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }

        // Générer un token quand même pour permettre la redirection vers /en-attente
        String jwtToken = jwtService.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setToken(jwtToken);
        response.setRole(user.getRole().name());
        response.setNom(user.getNom());
        response.setPrenom(user.getPrenom());
        response.setEmail(user.getEmail());
        response.setEstActif(false);    // Important pour le frontend
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier si le compte est actif
        if (!user.isEstActif()) {
            throw new DisabledException("Votre compte est en attente de validation par l'administrateur");
        }

        String jwtToken = jwtService.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setToken(jwtToken);
        response.setRole(user.getRole().name());
        response.setNom(user.getNom());
        response.setPrenom(user.getPrenom());
        response.setEmail(user.getEmail());
        response.setEstActif(user.isEstActif());
        return response;
    }

    // ========== MOT DE PASSE OUBLIÉ ==========

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        // Pour des raisons de sécurité, on ne révèle pas si l'email existe ou non
        if (user == null) {
            return;
        }

        // Supprimer les anciens tokens pour cet utilisateur
        tokenRepository.deleteByUserId(user.getId());

        // Créer un nouveau token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);

        // Envoyer l'email
        try {
            emailService.sendResetPasswordEmail(user.getEmail(), token, user.getPrenom(), user.getNom());
            System.out.println("✅ Email de réinitialisation envoyé à " + user.getEmail());
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("Token expiré. Veuillez refaire une demande.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Supprimer le token après utilisation
        tokenRepository.delete(resetToken);
    }
}