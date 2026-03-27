package com.clinicflow.clinic_flow.auth;

import com.clinicflow.clinic_flow.auth.dtos.*;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionRepository;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessions;
import com.clinicflow.clinic_flow.config.JwtConfig;
import com.clinicflow.clinic_flow.exception.InvalidSessionException;
import com.clinicflow.clinic_flow.exception.UserNotFoundException;
import com.clinicflow.clinic_flow.users.UsersMapper;
import com.clinicflow.clinic_flow.users.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final JwtConfig jwtConfig;
    private final AuthSessionRepository authSessionRepository;
    private final AuthSessionService authSessionService;

    @Transactional
    public LoginResult login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = usersRepository.findByUsername(request.getUsername()).orElseThrow( () ->
                new UserNotFoundException(request.getUsername()));

        AuthSessions session = new AuthSessions();
        session.setId(UUID.randomUUID().toString());
        session.setUser(user);
        session.setCreatedAt(LocalDateTime.now());
        session.setRefreshExpiresAt(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpiration()));
        authSessionRepository.save(session);

        var accessToken = jwtService.generateAccessToken(user, session.getId());
        var refreshToken = jwtService.generateRefreshToken(user, session.getId());

        return new LoginResult(accessToken, refreshToken);
    }

    @Transactional
    public void logout(String sid) {
        var session = authSessionRepository.findById(sid).orElseThrow(
                () -> new InvalidSessionException("Could not find session")
        );
        session.setRevokedAt(LocalDateTime.now());
        authSessionRepository.save(session);
    }

    public Jwt refreshToken(Jwt refreshToken) {
        authSessionService.requireRefreshableSession(refreshToken.getSid());
        var user = usersRepository.findById(refreshToken.getUserId()).orElseThrow();
        return jwtService.generateAccessToken(user, refreshToken.getSid());
    }

    public LoginResponse me() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AuthPrincipal) authentication.getPrincipal();
        var id = principal.userId();

        var user = usersRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        return usersMapper.toLoginResponse(user);
    }

    public boolean validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        var token = authHeader.replace("Bearer ", "");
        var jwt = jwtService.parseToken(token);
        if (jwt == null || jwt.isExpired()) {
            return false;
        }

        if (!"access".equals(jwt.getTokenType())) {
            return false;
        }

        return authSessionService.isAccessSessionActive(jwt.getSid());
    }


}
