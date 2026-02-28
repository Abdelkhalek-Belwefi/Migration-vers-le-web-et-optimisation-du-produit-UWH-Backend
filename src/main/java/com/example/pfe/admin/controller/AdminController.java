package com.example.pfe.admin.controller;

import com.example.pfe.admin.dto.UserDTO;
import com.example.pfe.admin.dto.RoleUpdateRequest;
import com.example.pfe.admin.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateUserRole(id, request.getRole()));
    }

    @PutMapping("/users/{id}/activer")
    public ResponseEntity<UserDTO> activerCompte(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.activerCompte(id));
    }

    @PutMapping("/users/{id}/desactiver")
    public ResponseEntity<UserDTO> desactiverCompte(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.desactiverCompte(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAllRoles() {
        return ResponseEntity.ok(adminService.getAllRoles());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(adminService.createUser(userDTO));
    }
}