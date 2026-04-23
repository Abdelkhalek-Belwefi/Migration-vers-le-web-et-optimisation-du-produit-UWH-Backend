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
}