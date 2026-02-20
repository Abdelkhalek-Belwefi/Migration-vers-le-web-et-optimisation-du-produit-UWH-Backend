package com.example.pfe.auth.controller;

import com.example.pfe.auth.dto.RegisterRequest;
import com.example.pfe.auth.dto.LoginRequest;
import com.example.pfe.auth.dto.AuthResponse;

import com.example.pfe.auth.dto.*;
import com.example.pfe.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5174")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService service;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request){
        return service.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request){
        return service.login(request);
    }
}
