package com.example.pfe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@warehouse.com}")
    private String fromEmail;

    /**
     * Envoie un email contenant le code OTP au client.
     *
     * @param to  adresse email du client
     * @param otp code OTP à 6 chiffres
     * @param numeroBl numéro du bon de livraison (optionnel)
     */
    public void sendOtpEmail(String to, String otp, String numeroBl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("🔐 Code de validation de livraison - " + numeroBl);
        message.setText(String.format(
                "Bonjour,\n\n" +
                        "Votre commande a été expédiée et sera livrée prochainement.\n" +
                        "Pour valider la réception de votre colis, veuillez communiquer le code suivant au livreur :\n\n" +
                        "🔢 CODE OTP : %s\n\n" +
                        "Ce code est strictement personnel et à usage unique.\n\n" +
                        "Merci de votre confiance.\n" +
                        "L'équipe Warehouse",
                otp
        ));
        mailSender.send(message);
    }

    /**
     * Envoie un email de bienvenue à un nouvel utilisateur avec ses identifiants.
     *
     * @param to adresse email de l'utilisateur
     * @param prenom prénom de l'utilisateur
     * @param nom nom de l'utilisateur
     * @param email email de connexion
     * @param motDePasse mot de passe en clair
     * @param role rôle attribué
     */
    public void sendWelcomeEmail(String to, String prenom, String nom, String email, String motDePasse, String role) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("🎉 Bienvenue sur la plateforme Warehouse - Vos identifiants de connexion");

        String roleLibelle = getRoleLibelle(role);

        message.setText(String.format(
                "Bonjour %s %s,\n\n" +
                        "Votre compte a été créé avec succès sur la plateforme Warehouse.\n\n" +
                        "📋 Vos identifiants de connexion :\n" +
                        "   • Email : %s\n" +
                        "   • Mot de passe : %s\n\n" +
                        "👤 Votre rôle : %s\n\n" +
                        "🔗 Lien de connexion : http://localhost:5173/login\n\n" +
                        "⚠️ Pour des raisons de sécurité, nous vous recommandons de modifier votre mot de passe lors de votre première connexion.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Warehouse",
                prenom, nom,
                email,
                motDePasse,
                roleLibelle
        ));

        mailSender.send(message);
    }

    /**
     * Envoie un email de réinitialisation de mot de passe.
     *
     * @param to adresse email de l'utilisateur
     * @param token code de réinitialisation
     * @param prenom prénom de l'utilisateur
     * @param nom nom de l'utilisateur
     */
    public void sendResetPasswordEmail(String to, String token, String prenom, String nom) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("🔐 Réinitialisation de votre mot de passe - Warehouse");

        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        message.setText(String.format(
                "Bonjour %s %s,\n\n" +
                        "Nous avons reçu une demande de réinitialisation de votre mot de passe.\n\n" +
                        "🔗 Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe :\n" +
                        "%s\n\n" +
                        "⚠️ Ce lien est valable pendant 15 minutes.\n\n" +
                        "Si vous n'êtes pas à l'origine de cette demande, veuillez ignorer cet email.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Warehouse",
                prenom, nom,
                resetLink
        ));

        mailSender.send(message);
    }

    private String getRoleLibelle(String role) {
        switch (role) {
            case "ADMINISTRATEUR":
                return "Administrateur";
            case "RESPONSABLE_ENTREPOT":
                return "Responsable d'entrepôt";
            case "OPERATEUR_ENTREPOT":
                return "Opérateur d'entrepôt";
            case "SERVICE_COMMERCIAL":
                return "Service Commercial";
            case "TRANSPORTEUR":
                return "Transporteur";
            default:
                return role;
        }
    }
}