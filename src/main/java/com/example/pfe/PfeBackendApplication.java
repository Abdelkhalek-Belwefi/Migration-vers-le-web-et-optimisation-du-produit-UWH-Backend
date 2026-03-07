package com.example.pfe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // ✅ Active les tâches planifiées (notifications stock bas, etc.)
public class PfeBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(PfeBackendApplication.class, args);
    }
}