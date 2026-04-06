package com.example.pfe.expedition.service;

import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.commande.entity.Commande;
import com.example.pfe.commande.enums.StatutCommande;
import com.example.pfe.commande.repository.CommandeRepository;
import com.example.pfe.expedition.dto.ExpeditionDTO;
import com.example.pfe.expedition.entity.Expedition;
import com.example.pfe.expedition.entity.ExpeditionStatut;
import com.example.pfe.expedition.repository.ExpeditionRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpeditionService {

    private final ExpeditionRepository expeditionRepository;
    private final CommandeRepository commandeRepository;
    private final UserRepository userRepository;

    public ExpeditionService(ExpeditionRepository expeditionRepository,
                             CommandeRepository commandeRepository,
                             UserRepository userRepository) {
        this.expeditionRepository = expeditionRepository;
        this.commandeRepository = commandeRepository;
        this.userRepository = userRepository;
    }

    public List<ExpeditionDTO> getAllExpeditions() {
        return expeditionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ExpeditionDTO getExpeditionById(Long id) {
        Expedition expedition = expeditionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expédition non trouvée"));
        return convertToDTO(expedition);
    }

    public List<ExpeditionDTO> getExpeditionsByStatut(ExpeditionStatut statut) {
        return expeditionRepository.findByStatut(statut).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpeditionDTO expedierCommande(Long commandeId, String transporteur) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        if (commande.getStatut() != StatutCommande.VALIDEE) {
            throw new RuntimeException("Seules les commandes validées peuvent être expédiées");
        }
        if (expeditionRepository.findByCommandeId(commandeId).isPresent()) {
            throw new RuntimeException("Une expédition existe déjà pour cette commande");
        }

        Expedition expedition = new Expedition();
        expedition.setCommande(commande);
        expedition.setNumeroBL(generateNumeroBL());
        expedition.setTransporteur(transporteur);
        expedition.setStatut(ExpeditionStatut.EXPEDIEE);
        expedition.setDateExpedition(LocalDateTime.now());
        expedition.setPreparePar(getCurrentUser());

        Expedition saved = expeditionRepository.save(expedition);

        // Mettre à jour le statut de la commande
        commande.setStatut(StatutCommande.EXPEDIEE);
        commande.setUpdatedAt(LocalDateTime.now());
        commandeRepository.save(commande);

        return convertToDTO(saved);
    }

    @Transactional
    public ExpeditionDTO updateStatut(Long id, ExpeditionStatut statut) {
        Expedition expedition = expeditionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expédition non trouvée"));
        expedition.setStatut(statut);
        if (statut == ExpeditionStatut.EXPEDIEE) {
            expedition.setDateExpedition(LocalDateTime.now());
            expedition.setValidePar(getCurrentUser());

            // Mettre à jour le statut de la commande associée
            Commande commande = expedition.getCommande();
            commande.setStatut(StatutCommande.EXPEDIEE);
            commande.setUpdatedAt(LocalDateTime.now());
            commandeRepository.save(commande);
        }
        expedition.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(expeditionRepository.save(expedition));
    }

    @Transactional
    public void deleteExpedition(Long id) {
        expeditionRepository.deleteById(id);
    }

    private String generateNumeroBL() {
        return "BL-" + System.currentTimeMillis();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        }
        return null;
    }

    private ExpeditionDTO convertToDTO(Expedition expedition) {
        ExpeditionDTO dto = new ExpeditionDTO();
        dto.setId(expedition.getId());
        dto.setCommandeId(expedition.getCommande().getId());
        dto.setCommandeNumero(expedition.getCommande().getNumeroCommande());
        dto.setClientNom(expedition.getCommande().getClient().getNom() + " " + expedition.getCommande().getClient().getPrenom());
        dto.setNumeroBL(expedition.getNumeroBL());
        dto.setStatut(expedition.getStatut());
        dto.setDateExpedition(expedition.getDateExpedition());
        dto.setPrepareParNom(expedition.getPreparePar() != null ? expedition.getPreparePar().getNom() + " " + expedition.getPreparePar().getPrenom() : null);
        dto.setTransporteur(expedition.getTransporteur());
        dto.setNumeroSuivi(expedition.getNumeroSuivi());
        dto.setCreatedAt(expedition.getCreatedAt());
        dto.setUpdatedAt(expedition.getUpdatedAt());
        return dto;
    }
}