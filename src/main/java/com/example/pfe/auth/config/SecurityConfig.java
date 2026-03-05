package com.example.pfe.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        // Routes admin
                        .requestMatchers("/api/admin/**").hasAuthority("ADMINISTRATEUR")

                        // ===== ROUTES POUR LES ARTICLES (SPRINT 1) =====
                        // Consultation - accessible à tous les rôles métier
                        .requestMatchers(HttpMethod.GET, "/api/articles/**").hasAnyAuthority(
                                "RESPONSABLE_ENTREPOT",
                                "OPERATEUR_ENTREPOT",
                                "ADMINISTRATEUR"
                        )

                        // ✅ Activation/Désactivation - accessible à ADMIN + RESPONSABLE
                        .requestMatchers(HttpMethod.PUT, "/api/articles/{id}/activer").hasAnyAuthority("ADMINISTRATEUR", "RESPONSABLE_ENTREPOT")
                        .requestMatchers(HttpMethod.PUT, "/api/articles/{id}/desactiver").hasAnyAuthority("ADMINISTRATEUR", "RESPONSABLE_ENTREPOT")

                        // ✅ Création, modification, suppression - ADMIN uniquement
                        .requestMatchers(HttpMethod.POST, "/api/articles").hasAuthority("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/articles/{id}").hasAuthority("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/articles/**").hasAuthority("ADMINISTRATEUR")

                        // Synchronisation ERP
                        .requestMatchers(HttpMethod.POST, "/api/articles/sync").permitAll()

                        // ===== AUTRES ROUTES =====
                        .requestMatchers("/api/reception/**").hasAnyAuthority("OPERATEUR_ENTREPOT", "RESPONSABLE_ENTREPOT")
                        .requestMatchers("/api/picking/**").hasAnyAuthority("OPERATEUR_ENTREPOT", "RESPONSABLE_ENTREPOT")
                        .requestMatchers("/api/expedition/**").hasAuthority("RESPONSABLE_ENTREPOT")
                        .requestMatchers("/api/stock/**").hasAnyAuthority("RESPONSABLE_ENTREPOT", "OPERATEUR_ENTREPOT")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With"
        ));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}