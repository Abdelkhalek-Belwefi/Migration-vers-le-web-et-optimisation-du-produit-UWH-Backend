package com.example.pfe.picking.controller;

import com.example.pfe.picking.dto.PickingTaskDTO;
import com.example.pfe.picking.service.PickingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/picking")
@CrossOrigin(origins = "http://localhost:5173")
public class PickingController {

    private final PickingService pickingService;

    public PickingController(PickingService pickingService) {
        this.pickingService = pickingService;
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<PickingTaskDTO>> getUnassignedTasks() {
        return ResponseEntity.ok(pickingService.getUnassignedTasks());
    }

    @PostMapping("/{taskId}/assign")
    @PreAuthorize("hasAnyRole('RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<PickingTaskDTO> assignTask(@PathVariable Long taskId, @RequestParam String operatorId) {
        return ResponseEntity.ok(pickingService.assignTask(taskId, operatorId));
    }

    @GetMapping("/operator/{operatorId}")
    @PreAuthorize("hasAnyRole('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<List<PickingTaskDTO>> getOperatorTasks(@PathVariable String operatorId) {
        return ResponseEntity.ok(pickingService.getTasksForOperator(operatorId));
    }

    @PostMapping("/{taskId}/pick")
    @PreAuthorize("hasAnyRole('OPERATEUR_ENTREPOT', 'RESPONSABLE_ENTREPOT', 'ADMINISTRATEUR')")
    public ResponseEntity<PickingTaskDTO> pickQuantity(@PathVariable Long taskId, @RequestParam int quantity) {
        return ResponseEntity.ok(pickingService.pickQuantity(taskId, quantity));
    }
}