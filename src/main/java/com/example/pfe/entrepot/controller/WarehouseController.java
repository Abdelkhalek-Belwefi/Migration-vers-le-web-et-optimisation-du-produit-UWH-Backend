package com.example.pfe.entrepot.controller;

import com.example.pfe.entrepot.dto.WarehouseDTO;
import com.example.pfe.entrepot.service.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/warehouses")
@CrossOrigin(origins = "http://localhost:5173")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    // ===== ROUTES ADMIN (CRUD complet) - Réservées à ADMINISTRATEUR =====

    @GetMapping
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<List<WarehouseDTO>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<WarehouseDTO> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<WarehouseDTO> createWarehouse(@Valid @RequestBody WarehouseDTO dto) {
        return ResponseEntity.ok(warehouseService.createWarehouse(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<WarehouseDTO> updateWarehouse(@PathVariable Long id, @Valid @RequestBody WarehouseDTO dto) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok().build();
    }

    // ===== NOUVEAU : ROUTE POUR RESPONSABLE ENTREPOT (lecture seule) =====
    @GetMapping("/public")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATEUR', 'RESPONSABLE_ENTREPOT')")
    public ResponseEntity<List<WarehouseDTO>> getAllWarehousesPublic() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }
}