package com.example.pfe.auth.repository;

import com.example.pfe.auth.entity.User;
import com.example.pfe.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // NOUVEAU
    List<User> findByRole(Role role);
}