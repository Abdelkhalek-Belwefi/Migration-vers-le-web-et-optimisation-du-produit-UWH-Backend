package com.example.pfe.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        // 🔥 IMPORTANT : autoriser OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // autoriser auth
                        .requestMatchers("/api/auth/**").permitAll()

                        // autres routes protégées
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
