package com.example.pfe.config;

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
                        // ===== ROUTES PUBLIQUES =====
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ocr/extract").permitAll()

                        // ===== ROUTES TRANSFERT (SÉCURISÉES) =====
                        .requestMatchers(HttpMethod.GET, "/api/stocks/faibles").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/commandes/transfert").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.GET, "/api/commandes/transfert/recues").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PATCH, "/api/commandes/transfert/*/accepter").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PATCH, "/api/commandes/transfert/*/refuser").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")

                        // ===== NOUVELLE ROUTE ENTREPÔT POUR RESPONSABLE =====
                        .requestMatchers(HttpMethod.GET, "/api/admin/warehouses/public").hasAnyAuthority("ADMINISTRATEUR", "RESPONSABLE_ENTREPOT")

                        // ===== ROUTES ADMIN =====
                        .requestMatchers("/api/admin/**").hasAuthority("ADMINISTRATEUR")
                        .requestMatchers("/api/admin/warehouses/**").hasAuthority("ADMINISTRATEUR")

                        // ===== ARTICLES =====
                        .requestMatchers(HttpMethod.GET, "/api/articles/**").hasAnyAuthority(
                                "RESPONSABLE_ENTREPOT", "OPERATEUR_ENTREPOT", "ADMINISTRATEUR", "SERVICE_COMMERCIAL"
                        )
                        .requestMatchers(HttpMethod.PUT, "/api/articles/{id}/activer").hasAnyAuthority("ADMINISTRATEUR", "RESPONSABLE_ENTREPOT")
                        .requestMatchers(HttpMethod.PUT, "/api/articles/{id}/desactiver").hasAnyAuthority("ADMINISTRATEUR", "RESPONSABLE_ENTREPOT")
                        .requestMatchers(HttpMethod.POST, "/api/articles").hasAuthority("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/articles/{id}").hasAuthority("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/articles/**").hasAuthority("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/articles/sync").permitAll()

                        // ===== STOCKS =====
                        .requestMatchers(HttpMethod.GET, "/api/stocks/**").hasAnyAuthority(
                                "RESPONSABLE_ENTREPOT", "ADMINISTRATEUR", "SERVICE_COMMERCIAL", "OPERATEUR_ENTREPOT"
                        )
                        .requestMatchers(HttpMethod.POST, "/api/stocks/augmenter").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/stocks/diminuer").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/stocks/{id}/statut").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")

                        // ===== RÉCEPTION =====
                        .requestMatchers(HttpMethod.POST, "/api/reception/**").hasAnyAuthority("OPERATEUR_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/reception/lines/**").hasAnyAuthority("OPERATEUR_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.GET, "/api/reception/**").hasAnyAuthority("OPERATEUR_ENTREPOT", "RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/reception/*/valider").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")

                        // ===== RANGEMENT =====
                        .requestMatchers(HttpMethod.GET, "/api/rangement/**").hasAnyAuthority("OPERATEUR_ENTREPOT", "RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/rangement/*/commencer").hasAnyAuthority("OPERATEUR_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/rangement/*/terminer").hasAnyAuthority("OPERATEUR_ENTREPOT", "ADMINISTRATEUR")

                        // ===== SERVICE GS1 =====
                        .requestMatchers(HttpMethod.GET, "/api/gs1/**").hasAnyAuthority("OPERATEUR_ENTREPOT", "RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")

                        // ===== MOUVEMENTS =====
                        .requestMatchers(HttpMethod.GET, "/api/mouvements/**").hasAnyAuthority(
                                "RESPONSABLE_ENTREPOT", "ADMINISTRATEUR", "SERVICE_COMMERCIAL"
                        )
                        .requestMatchers(HttpMethod.POST, "/api/mouvements/entree").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/mouvements/sortie").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/mouvements/transfert").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")

                        // ===== CLIENTS =====
                        .requestMatchers("/api/clients/**").hasAnyAuthority("SERVICE_COMMERCIAL", "ADMINISTRATEUR")

                        // ===== COMMANDES =====
                        .requestMatchers(HttpMethod.GET, "/api/commandes/**").hasAnyAuthority(
                                "SERVICE_COMMERCIAL", "ADMINISTRATEUR", "OPERATEUR_ENTREPOT", "RESPONSABLE_ENTREPOT"
                        )
                        .requestMatchers(HttpMethod.POST, "/api/commandes/**").hasAnyAuthority("SERVICE_COMMERCIAL", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/commandes/**").hasAnyAuthority("SERVICE_COMMERCIAL", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PATCH, "/api/commandes/**").hasAnyAuthority(
                                "SERVICE_COMMERCIAL", "ADMINISTRATEUR", "OPERATEUR_ENTREPOT", "RESPONSABLE_ENTREPOT"
                        )
                        .requestMatchers(HttpMethod.DELETE, "/api/commandes/**").hasAnyAuthority("SERVICE_COMMERCIAL", "ADMINISTRATEUR")

                        // ===== EXPÉDITIONS =====
                        .requestMatchers("/api/expeditions/**").hasAnyAuthority("RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")

                        // ===== NOUVELLE ROUTE POUR LIVRAISONS EN ATTENTE (doit être AVANT la règle transporteur) =====
                        .requestMatchers(HttpMethod.GET, "/api/transporteur/livraisons/entrepot/attente").hasAnyAuthority("OPERATEUR_ENTREPOT", "RESPONSABLE_ENTREPOT", "ADMINISTRATEUR")

                        // ===== ROUTES TRANSPORTEUR =====
                        .requestMatchers("/api/transporteur/**").hasAuthority("TRANSPORTEUR")

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