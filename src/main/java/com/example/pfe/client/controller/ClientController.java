package com.example.pfe.client.controller;

import com.example.pfe.client.dto.ClientDTO;
import com.example.pfe.client.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "http://localhost:5173")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<ClientDTO> createClient(@RequestBody ClientDTO clientDTO) {
        return ResponseEntity.ok(clientService.createClient(clientDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable Long id, @RequestBody ClientDTO clientDTO) {
        return ResponseEntity.ok(clientService.updateClient(id, clientDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SERVICE_COMMERCIAL', 'ADMINISTRATEUR')")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok().build();
    }
}