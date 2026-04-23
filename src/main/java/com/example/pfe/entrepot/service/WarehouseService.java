package com.example.pfe.entrepot.service;

import com.example.pfe.entrepot.dto.WarehouseDTO;
import com.example.pfe.entrepot.entity.Warehouse;
import com.example.pfe.entrepot.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
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
        return convertToDTO(warehouse);
    }

    @Transactional
    public WarehouseDTO updateWarehouse(Long id, WarehouseDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));
        updateEntityFromDTO(warehouse, dto);
        warehouse = warehouseRepository.save(warehouse);
        return convertToDTO(warehouse);
    }

    @Transactional
    public void deleteWarehouse(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new RuntimeException("Entrepôt non trouvé");
        }
        warehouseRepository.deleteById(id);
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
        return dto;
    }
}