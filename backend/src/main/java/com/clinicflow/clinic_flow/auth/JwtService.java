package com.clinicflow.clinic_flow.auth;

import com.clinicflow.clinic_flow.config.JwtConfig;
import com.clinicflow.clinic_flow.users.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class JwtService {

    private final JwtConfig jwtConfig;

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Jwt generateAccessToken(Users user, String sessionId) {
        return generateToken(user, jwtConfig.getAccessTokenExpiration(), sessionId, "access");
    }

    public Jwt generateRefreshToken(Users user, String sessionId) {
        return generateToken(user, jwtConfig.getRefreshTokenExpiration(), sessionId, "refresh");
    }

    private Jwt generateToken(Users user, long tokenExpiration, String sessionId, String tokenType) {
        var claims = Jwts.claims()
                .subject(user.getId().toString())
                .add("username", user.getUsername())
                .add("role", user.getRoleName())
                .add("sid", sessionId)
                .add("token_type", tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                .build();

        return new Jwt(sessionId, tokenType, claims, jwtConfig.getSecretKey());
    }

    public Jwt parseToken(String token) {
        try {
            var claims = getClaims(token);
            return new Jwt(claims.get(
                    "sid", String.class), claims.get("token_type", String.class) ,claims, jwtConfig.getSecretKey()
            );
        } catch (JwtException e) {
            return null;
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }


}
