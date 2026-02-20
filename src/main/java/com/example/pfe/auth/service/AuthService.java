package com.example.pfe.auth.service;

import com.example.pfe.auth.dto.AuthResponse;
import com.example.pfe.auth.dto.LoginRequest;
import com.example.pfe.auth.dto.RegisterRequest;
import com.example.pfe.auth.entity.Role;
import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.repository.UserRepository;
import com.example.pfe.auth.config.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    public AuthResponse register(RegisterRequest request){

        if(repository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already exists");

        User user = new User();

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setNumTelephone(request.getNumTelephone());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(Role.PERSONNEL);



        repository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, user.getRole().name());
    }

    public AuthResponse login(LoginRequest request){

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(!encoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid password");

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, user.getRole().name());
    }
}
