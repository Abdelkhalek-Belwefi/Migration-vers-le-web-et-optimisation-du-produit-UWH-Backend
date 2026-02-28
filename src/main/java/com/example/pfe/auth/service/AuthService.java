package com.example.pfe.auth.service;

import com.example.pfe.auth.dto.AuthResponse;
import com.example.pfe.auth.dto.LoginRequest;
import com.example.pfe.auth.dto.RegisterRequest;
import com.example.pfe.auth.entity.Role;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.auth.config.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        System.out.println("=== NOUVELLE INSCRIPTION ===");
        System.out.println("Email reçu: '" + request.getEmail() + "'");
        System.out.println("Nom: " + request.getNom());
        System.out.println("Prénom: " + request.getPrenom());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            System.out.println("ERREUR: Email déjà utilisé: " + request.getEmail());
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNumTelephone(request.getNumTelephone());
        user.setRole(Role.EN_ATTENTE);
        user.setEstActif(false);

        System.out.println("Hash du mot de passe généré: " + user.getPassword());

        User savedUser = userRepository.save(user);
        System.out.println("Utilisateur sauvegardé avec ID: " + savedUser.getId());

        String jwtToken = jwtService.generateToken(user.getEmail());

        AuthResponse response = new AuthResponse();
        response.setToken(jwtToken);
        response.setRole(user.getRole().name());
        response.setNom(user.getNom());
        response.setPrenom(user.getPrenom());
        response.setEmail(user.getEmail());
        response.setEstActif(user.isEstActif());

        System.out.println("Inscription réussie pour: " + user.getEmail());
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        System.out.println("=================================");
        System.out.println("=== TENTATIVE DE CONNEXION ===");
        System.out.println("Email reçu: '" + request.getEmail() + "'");
        System.out.println("Mot de passe reçu: '" + request.getPassword() + "'");
        System.out.println("=================================");

        try {
            // Vérifier si l'utilisateur existe
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        System.out.println("ERREUR: Utilisateur non trouvé: " + request.getEmail());
                        return new UsernameNotFoundException("Utilisateur non trouvé");
                    });

            System.out.println("Utilisateur trouvé:");
            System.out.println("  - ID: " + user.getId());
            System.out.println("  - Email: " + user.getEmail());
            System.out.println("  - Rôle: " + user.getRole());
            System.out.println("  - Actif: " + user.isEstActif());
            System.out.println("  - Hash en base: " + user.getPassword());

            // Vérifier le mot de passe
            boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
            System.out.println("Vérification mot de passe: " + (passwordMatches ? "OK" : "ECHEC"));

            if (!passwordMatches) {
                System.out.println("ERREUR: Mot de passe incorrect");
                throw new BadCredentialsException("Email ou mot de passe incorrect");
            }

            // Vérifier si le compte est actif
            if (!user.isEstActif()) {
                System.out.println("ERREUR: Compte non activé");
                throw new DisabledException("Votre compte est en attente de validation");
            }

            // Authentification Spring
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );
                System.out.println("Authentification Spring OK");
            } catch (Exception e) {
                System.out.println("ERREUR Spring Auth: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }

            String jwtToken = jwtService.generateToken(user.getEmail());

            AuthResponse response = new AuthResponse();
            response.setToken(jwtToken);
            response.setRole(user.getRole().name());
            response.setNom(user.getNom());
            response.setPrenom(user.getPrenom());
            response.setEmail(user.getEmail());
            response.setEstActif(user.isEstActif());

            System.out.println("Connexion réussie pour: " + user.getEmail());
            System.out.println("Token généré: " + jwtToken.substring(0, 20) + "...");

            return response;

        } catch (UsernameNotFoundException e) {
            System.out.println("ERREUR FINALE: " + e.getMessage());
            throw new RuntimeException("Email ou mot de passe incorrect");
        } catch (BadCredentialsException e) {
            System.out.println("ERREUR FINALE: " + e.getMessage());
            throw new RuntimeException("Email ou mot de passe incorrect");
        } catch (DisabledException e) {
            System.out.println("ERREUR FINALE: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            System.out.println("ERREUR FINALE inattendue: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Email ou mot de passe incorrect");
        }
    }
}