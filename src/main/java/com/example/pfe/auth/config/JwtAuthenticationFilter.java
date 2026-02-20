package com.example.pfe.auth.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends GenericFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authHeader = httpRequest.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if(jwtService.isTokenValid(token)){
                String email = jwtService.extractUsername(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }
}
