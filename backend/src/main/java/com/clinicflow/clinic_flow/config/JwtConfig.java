package com.clinicflow.clinic_flow.config;

import jakarta.annotation.PostConstruct;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
@ConfigurationProperties(prefix = "spring.jwt")
@Data
public class JwtConfig {
    private String secret;
    private int accessTokenExpiration;
    private int refreshTokenExpiration;
    private boolean cookieSecure;
    private String cookieSameSite = "Lax";

    @PostConstruct
    void validate() {
        if (cookieSameSite == null || cookieSameSite.isBlank()) {
            throw new IllegalStateException("spring.jwt.cookieSameSite must be set to Lax or None");
        }

        if ("lax".equalsIgnoreCase(cookieSameSite)) {
            cookieSameSite = "Lax";
        } else if ("none".equalsIgnoreCase(cookieSameSite)) {
            cookieSameSite = "None";
        } else {
            throw new IllegalStateException("spring.jwt.cookieSameSite must be either Lax or None");
        }

        if ("None".equals(cookieSameSite) && !cookieSecure) {
            throw new IllegalStateException("spring.jwt.cookieSecure must be true when spring.jwt.cookieSameSite=None");
        }
    }

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
