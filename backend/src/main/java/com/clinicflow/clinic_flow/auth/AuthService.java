package com.clinicflow.clinic_flow.auth;

import com.clinicflow.clinic_flow.auth.dtos.*;
import com.clinicflow.clinic_flow.auth.records.AuthPrincipal;
import com.clinicflow.clinic_flow.auth.records.LoginResult;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessions;
import com.clinicflow.clinic_flow.clinics.ClinicsRepository;
import com.clinicflow.clinic_flow.config.JwtConfig;
import com.clinicflow.clinic_flow.exception.ClinicNotFoundException;
import com.clinicflow.clinic_flow.exception.UserAlreadyExistsException;
import com.clinicflow.clinic_flow.users.Users;
import com.clinicflow.clinic_flow.users.UsersMapper;
import com.clinicflow.clinic_flow.users.UsersRepository;
import com.clinicflow.clinic_flow.users.dtos.CreateUserRequest;
import com.clinicflow.clinic_flow.users.dtos.UsersResponseDto;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final JwtConfig jwtConfig;
    private final AuthSessionService authSessionService;
    private final ClinicsRepository clinicsRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResult login(LoginRequest request) {
        log.info("Login attempt for username={}", request.getUsername());

        var user = usersRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed for user={}", user.getUsername());
            throw new BadCredentialsException("Bad credentials");
        }

        log.info(
                "Login success for user={} clinicId={}",
                user.getUsername(),
                user.getClinic().getId());
        return issueTokenPair(user);
    }

    @Transactional
    public UsersResponseDto register(String clinicSlug, CreateUserRequest request) {
        log.info("Register attempt for username={} clinicSlug={}", request.getUsername(), clinicSlug);

        var clinic = clinicsRepository
                .findClinicsBySlug(clinicSlug)
                .orElseThrow(() -> new ClinicNotFoundException("Clinic not found"));

        if (usersRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException(request.getUsername());
        }

        var user = usersMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setRoleName(Users.RoleName.DISPLAY);
        user.setEnabled(Boolean.TRUE);
        user.setClinic(clinic);

        return usersMapper.toUsersResponseDto(usersRepository.save(user));
    }

    @Transactional
    public void logout(String sid, Long clinicId) {
        authSessionService.revokeSession(sid, clinicId);
    }

    @Transactional
    public LoginResult refreshToken(Jwt refreshToken) {
        if (refreshToken == null || !"refresh".equals(refreshToken.getTokenType()) || refreshToken.isExpired()) {
            log.warn("Refresh token expired");
            throw new BadCredentialsException("Invalid refresh token");
        }
        var clinicId = refreshToken.getClinicId();

        var session = authSessionService.requireRefreshableSession(refreshToken.getSid(), clinicId);
        var user = usersRepository
                .findByUsernameAndClinicId(refreshToken.getUsername(), clinicId)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        authSessionService.revokeSession(session.getId(), clinicId);

        log.info("Token refreshed userId={} sessionId={}", user.getId(), session.getId());

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

        if (jwt != null && authSessionService.checkRevokedAt(jwt.getSid(), jwt.getClinicId())) {
            return !jwt.isExpired();
        }
        return false;
    }

    @Transactional
    protected LoginResult issueTokenPair(Users user) {
        AuthSessions session = authSessionService.createSession(
                user, LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpiration()));
        if (user.getClinic() == null) {
            throw new ClinicNotFoundException("Not able to associate user with clinic");
        }
        var sessionId = session.getId().toString();
        var clinicId = user.getClinic().getId();
        var accessToken = jwtService.generateAccessToken(user, sessionId, clinicId);
        var refreshToken = jwtService.generateRefreshToken(user, sessionId, clinicId);
        return new LoginResult(accessToken, refreshToken);
    }
}
