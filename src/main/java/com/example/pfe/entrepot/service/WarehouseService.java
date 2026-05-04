package com.example.pfe.entrepot.service;

import com.example.pfe.entrepot.dto.WarehouseDTO;
import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
import com.example.pfe.notification.enums.NotificationType;
import com.example.pfe.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final NotificationService notificationService;

    public WarehouseService(WarehouseRepository warehouseRepository,
                            NotificationService notificationService) {
        this.warehouseRepository = warehouseRepository;
        this.notificationService = notificationService;
    }

    public List<WarehouseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public WarehouseDTO getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));
        return convertToDTO(warehouse);
    }

    @Transactional
    public WarehouseDTO createWarehouse(WarehouseDTO dto) {
        Warehouse warehouse = new Warehouse();
        updateEntityFromDTO(warehouse, dto);
        warehouse = warehouseRepository.save(warehouse);

        // 🔔 NOTIFICATION : Nouvel entrepôt créé (pour tous les administrateurs)
        try {
            notificationService.createNotification(
                    1L, // ID de l'administrateur principal (ou parcourir tous)
                    "🏭 Nouvel entrepôt créé",
                    String.format("Un nouvel entrepôt a été créé : %s à %s",
                            warehouse.getNom(), warehouse.getVille()),
                    NotificationType.INFO,
                    "/admin?tab=entrepots",
                    warehouse.getId(),
                    "WAREHOUSE"
            );
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }

        return convertToDTO(warehouse);
    }

    @Transactional
    public WarehouseDTO updateWarehouse(Long id, WarehouseDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));

        String ancienNom = warehouse.getNom();

        updateEntityFromDTO(warehouse, dto);
        warehouse = warehouseRepository.save(warehouse);

        // 🔔 NOTIFICATION : Entrepôt modifié (pour tous les administrateurs)
        try {
            notificationService.createNotification(
                    1L,
                    "✏️ Entrepôt modifié",
                    String.format("L'entrepôt '%s' a été modifié. Nouveau nom: %s",
                            ancienNom, warehouse.getNom()),
                    NotificationType.INFO,
                    "/admin?tab=entrepots",
                    warehouse.getId(),
                    "WAREHOUSE"
            );
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }

        return convertToDTO(warehouse);
    }

    @Transactional
    public void deleteWarehouse(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new RuntimeException("Entrepôt non trouvé");
        }

        Warehouse warehouse = warehouseRepository.findById(id).orElse(null);
        warehouseRepository.deleteById(id);

        // 🔔 NOTIFICATION : Entrepôt supprimé (pour tous les administrateurs)
        if (warehouse != null) {
            try {
                notificationService.createNotification(
                        1L,
                        "🏭 Entrepôt supprimé",
                        String.format("L'entrepôt '%s' a été supprimé du système.",
                                warehouse.getNom()),
                        NotificationType.ERREUR,
                        "/admin?tab=entrepots",
                        id,
                        "WAREHOUSE"
                );
            } catch (Exception e) {
                System.out.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
            }
        }
    }

    private void updateEntityFromDTO(Warehouse warehouse, WarehouseDTO dto) {
        warehouse.setNom(dto.getNom());
        warehouse.setAdresse(dto.getAdresse());
        warehouse.setVille(dto.getVille());
        warehouse.setCodePostal(dto.getCodePostal());
        warehouse.setPays(dto.getPays());
        warehouse.setResponsableNom(dto.getResponsableNom());
        warehouse.setTelephone(dto.getTelephone());
        warehouse.setEmail(dto.getEmail());
        warehouse.setActif(dto.isActif());
        warehouse.setLatitude(dto.getLatitude());
        warehouse.setLongitude(dto.getLongitude());
    }

    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        WarehouseDTO dto = new WarehouseDTO();
        dto.setId(warehouse.getId());
        dto.setNom(warehouse.getNom());
        dto.setAdresse(warehouse.getAdresse());
        dto.setVille(warehouse.getVille());
        dto.setCodePostal(warehouse.getCodePostal());
        dto.setPays(warehouse.getPays());
        dto.setResponsableNom(warehouse.getResponsableNom());
        dto.setTelephone(warehouse.getTelephone());
        dto.setEmail(warehouse.getEmail());
        dto.setActif(warehouse.isActif());
        dto.setCreatedAt(warehouse.getCreatedAt());
        dto.setUpdatedAt(warehouse.getUpdatedAt());
        dto.setLatitude(warehouse.getLatitude());
        dto.setLongitude(warehouse.getLongitude());
        return dto;
    }
}