package com.example.pfe.chatbot.controller;

import com.example.pfe.chatbot.dto.ChatMessageDTO;
import com.example.pfe.chatbot.dto.ChatResponseDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ChatbotController {

    @MessageMapping("/chat.send")
    @SendTo("/topic/chat")
    public ChatResponseDTO sendMessage(ChatMessageDTO message) {
        System.out.println("📨 Message reçu: " + message.getMessage());

        String lowerMsg = message.getMessage().toLowerCase();
        ChatResponseDTO response;

        // Parsing simple pour tester
        if (lowerMsg.contains("bonjour") || lowerMsg.contains("salut")) {
            response = new ChatResponseDTO("👋 Bonjour ! Comment puis-je vous aider aujourd'hui ?", "TEXT");
        }
        else if (lowerMsg.contains("aide")) {
            response = new ChatResponseDTO(
                    "🤖 Commandes disponibles :\n" +
                            "• 'Stock faible' - Voir les articles avec stock < 20\n" +
                            "• 'Bonjour' - Me saluer\n" +
                            "• 'Aide' - Voir cette aide",
                    "TEXT"
            );
        }
        else if (lowerMsg.contains("stock faible")) {
            response = new ChatResponseDTO(
                    "📊 Voici les articles avec stock faible :\n" +
                            "• Clavier mécanique - 5 unités\n" +
                            "• Souris gaming - 8 unités\n" +
                            "• Carte graphique - 12 unités",
                    "TEXT"
            );
        }
        else {
            response = new ChatResponseDTO(
                    "🤔 Désolé, je n'ai pas compris votre demande. Tapez 'Aide' pour voir les commandes disponibles.",
                    "TEXT"
            );
        }

        return response;
    }
}