package com.example.bankcards;

import com.example.bankcards.security.JWTUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestJWTConfig {

    @Bean
    public JWTUtil jwtUtil() {
        return new JWTUtil() {
            @Override
            public String generateToken(String username) {
                return "mocked-jwt-token";
            }

            @Override
            public boolean validateToken(String token) {
                return true;
            }

            @Override
            public String getUsernameFromToken(String token) {
                return "testuser";
            }
        };
    }
}