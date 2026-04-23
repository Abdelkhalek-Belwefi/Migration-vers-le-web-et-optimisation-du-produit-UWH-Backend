package com.example.pfe.client.service;

import com.example.pfe.client.dto.ClientDTO;
import com.example.pfe.client.entity.Client;
import com.example.pfe.client.repository.ClientRepository;
import com.example.pfe.service.GeocodingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final GeocodingService geocodingService;   // ← AJOUT

    public ClientService(ClientRepository clientRepository, GeocodingService geocodingService) {
        this.clientRepository = clientRepository;
        this.geocodingService = geocodingService;     // ← AJOUT
    }

    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ClientDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        return convertToDTO(client);
    }

    @Transactional
    public ClientDTO createClient(ClientDTO dto) {
        if (clientRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }
        Client client = new Client();
        client.setNom(dto.getNom());
        client.setPrenom(dto.getPrenom());
        client.setEmail(dto.getEmail());
        client.setTelephone(dto.getTelephone());
        client.setAdresse(dto.getAdresse());
        client.setVille(dto.getVille());
        client.setCodePostal(dto.getCodePostal());
        client.setPays(dto.getPays());

        // ========== AJOUT : Géocoder l'adresse ==========
        updateClientCoordinates(client);

        return convertToDTO(clientRepository.save(client));
    }

    @Transactional
    public ClientDTO updateClient(Long id, ClientDTO dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        client.setNom(dto.getNom());
        client.setPrenom(dto.getPrenom());
        client.setEmail(dto.getEmail());
        client.setTelephone(dto.getTelephone());
        client.setAdresse(dto.getAdresse());
        client.setVille(dto.getVille());
        client.setCodePostal(dto.getCodePostal());
        client.setPays(dto.getPays());
        client.setUpdatedAt(LocalDateTime.now());

        // ========== AJOUT : Géocoder l'adresse ==========
        updateClientCoordinates(client);

        return convertToDTO(clientRepository.save(client));
    }

    @Transactional
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }

    private ClientDTO convertToDTO(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setNom(client.getNom());
        dto.setPrenom(client.getPrenom());
        dto.setEmail(client.getEmail());
        dto.setTelephone(client.getTelephone());
        dto.setAdresse(client.getAdresse());
        dto.setVille(client.getVille());
        dto.setCodePostal(client.getCodePostal());
        dto.setPays(client.getPays());
        dto.setCreatedAt(client.getCreatedAt());
        dto.setUpdatedAt(client.getUpdatedAt());
        // Ajout des coordonnées dans le DTO (si vous les avez ajoutées dans ClientDTO)
        // dto.setLatitude(client.getLatitude());
        // dto.setLongitude(client.getLongitude());
        return dto;
    }

    // ========== MÉTHODES AJOUTÉES POUR LE GÉOCODAGE ==========

    /**
     * Met à jour les coordonnées GPS du client à partir de son adresse.
     */
    private void updateClientCoordinates(Client client) {
        String fullAddress = buildFullAddress(client);
        if (fullAddress != null && !fullAddress.trim().isEmpty()) {
            Double[] coords = geocodingService.geocodeAddress(fullAddress);
            if (coords != null) {
                client.setLatitude(coords[0]);
                client.setLongitude(coords[1]);
                System.out.println("Géocodage réussi pour : " + fullAddress + " -> " + coords[0] + ", " + coords[1]);
            } else {
                System.out.println("Impossible de géocoder l'adresse : " + fullAddress);
            }
        }
    }

    /**
     * Construit une adresse complète à partir des champs du client.
     */
    private String buildFullAddress(Client client) {
        StringBuilder sb = new StringBuilder();
        if (client.getAdresse() != null && !client.getAdresse().isEmpty()) {
            sb.append(client.getAdresse());
        }
        if (client.getVille() != null && !client.getVille().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(client.getVille());
        }
        if (client.getCodePostal() != null && !client.getCodePostal().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(client.getCodePostal());
        }
        if (client.getPays() != null && !client.getPays().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(client.getPays());
        }
        return sb.toString();
    }
    @Transactional
    public void updateAllClientsCoordinates() {
        List<Client> clients = clientRepository.findAll();
        for (Client client : clients) {
            updateClientCoordinates(client);
            clientRepository.save(client);
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}