package com.clinicflow.clinic_flow.auth;

import com.clinicflow.clinic_flow.auth.dtos.*;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessions;
import com.clinicflow.clinic_flow.config.JwtConfig;
import com.clinicflow.clinic_flow.exception.UserNotFoundException;
import com.clinicflow.clinic_flow.users.UsersMapper;
import com.clinicflow.clinic_flow.users.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final JwtConfig jwtConfig;
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

        return issueTokenPair(user);
    }

    @Transactional
    public void logout(String sid) {
        authSessionService.revokeSession(sid);
    }

    @Transactional
    public LoginResult refreshToken(Jwt refreshToken) {
        if (refreshToken == null || !"refresh".equals(refreshToken.getTokenType()) || refreshToken.isExpired()) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        var session = authSessionService.requireRefreshableSession(refreshToken.getSid());
        var user = usersRepository.findById(refreshToken.getUserId()).orElseThrow();
        authSessionService.revokeSession(session.getId());
        return issueTokenPair(user);
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

    public boolean validateToken(String token) {
        if (token == null) {
            return false;
        }

        var jwt = jwtService.parseToken(token);

        if (jwt != null && authSessionService.checkRevokedAt(jwt.getSid())) {
            return !jwt.isExpired();
        }
        return false;
    }

    private LoginResult issueTokenPair(com.clinicflow.clinic_flow.users.Users user) {
        AuthSessions session = authSessionService.createSession(
                user,
                LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpiration())
        );
        var accessToken = jwtService.generateAccessToken(user, session.getId());
        var refreshToken = jwtService.generateRefreshToken(user, session.getId());
        return new LoginResult(accessToken, refreshToken);
    }


}
