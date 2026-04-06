package com.example.pfe.client.service;

import com.example.pfe.client.dto.ClientDTO;
import com.example.pfe.client.entity.Client;
import com.example.pfe.client.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
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
        return dto;
    }
}