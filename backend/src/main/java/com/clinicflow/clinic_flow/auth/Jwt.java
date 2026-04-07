package com.clinicflow.clinic_flow.auth;

import com.clinicflow.clinic_flow.users.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.*;

import javax.crypto.SecretKey;
import java.util.Date;

@Getter
@AllArgsConstructor
public class Jwt {

    private final String sid;
    private final String tokenType;
    private final Claims claims;
    private final SecretKey key;
    private final Long clinicId;

    public boolean isExpired(){
        return claims.getExpiration().before(new Date());
    }

    public Long getUserId(){
        return Long.valueOf(claims.getSubject());
    }

    public Users.RoleName getRole(){
        return Users.RoleName.valueOf(claims.get("role", String.class));
    }

    public String getSid(){
        return claims.get("sid", String.class);
    }

    public String getUsername() {
        return claims.get("username", String.class);
    }

    @Override
    public String toString() {
        return Jwts.builder().claims(claims).signWith(key).compact();
    }
}
