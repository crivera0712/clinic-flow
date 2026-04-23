package com.clinicflow.clinic_flow.auth;

import com.clinicflow.clinic_flow.auth.dtos.LoginRequest;
import com.clinicflow.clinic_flow.auth.records.LoginResult;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessions;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.clinics.ClinicsRepository;
import com.clinicflow.clinic_flow.config.JwtConfig;
import com.clinicflow.clinic_flow.users.Users;
import com.clinicflow.clinic_flow.users.UsersMapper;
import com.clinicflow.clinic_flow.users.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private UsersMapper usersMapper;

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private AuthSessionService authSessionService;

    @Mock
    private ClinicsRepository clinicsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginWhenUserAndPasswordMatch() {
        Clinics clinic = clinic(5L, "demo");
        Users user = user(1L, "sam", clinic);
        LoginRequest request = new LoginRequest();
        request.setUsername("sam");
        request.setPassword("secret1");
        Jwt accessJwt = mock(Jwt.class);
        Jwt refreshJwt = mock(Jwt.class);
        AuthSessions session = new AuthSessions();
        session.setId(UUID.randomUUID());
        session.setClinic(clinic);

        when(usersRepository.findByUsername("sam")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret1", "encoded")).thenReturn(true);
        when(jwtConfig.getRefreshTokenExpiration()).thenReturn(3600);
        when(authSessionService.createSession(any(), any())).thenReturn(session);
        when(jwtService.generateAccessToken(user, session.getId().toString(), 5L)).thenReturn(accessJwt);
        when(jwtService.generateRefreshToken(user, session.getId().toString(), 5L)).thenReturn(refreshJwt);

        LoginResult result = authService.login(request);

        assertSame(result.accessToken(), accessJwt);
        assertSame(result.refreshToken(), refreshJwt);
        verify(usersRepository).findByUsername("sam");
        verify(passwordEncoder).matches("secret1", "encoded");
    }

    @Test
    void shouldThrowBadCredentialsWhenUserDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setUsername("sam");
        request.setPassword("secret1");

        when(usersRepository.findByUsername("sam")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(usersRepository).findByUsername("sam");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(authSessionService, never()).createSession(any(), any());
    }

    @Test
    void shouldThrowBadCredentialsWhenPasswordDoesNotMatch() {
        Clinics clinic = clinic(5L, "demo");
        Users user = user(1L, "sam", clinic);
        LoginRequest request = new LoginRequest();
        request.setUsername("sam");
        request.setPassword("wrong");

        when(usersRepository.findByUsername("sam")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(usersRepository).findByUsername("sam");
        verify(passwordEncoder).matches("wrong", "encoded");
        verify(authSessionService, never()).createSession(any(), any());
    }

    @Test
    void shouldFailRefreshWhenUsernameExistsOnlyInAnotherClinic() {
        Jwt refreshJwt = mock(Jwt.class);
        AuthSessions session = new AuthSessions();
        session.setId(UUID.randomUUID());

        when(refreshJwt.getTokenType()).thenReturn("refresh");
        when(refreshJwt.isExpired()).thenReturn(false);
        when(refreshJwt.getClinicId()).thenReturn(7L);
        when(refreshJwt.getSid()).thenReturn(session.getId().toString());
        when(refreshJwt.getUsername()).thenReturn("sam");
        when(authSessionService.requireRefreshableSession(session.getId().toString(), 7L)).thenReturn(session);
        when(usersRepository.findByUsernameAndClinicId("sam", 7L)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(refreshJwt));

        verify(usersRepository).findByUsernameAndClinicId("sam", 7L);
        verify(authSessionService, never()).revokeSession(session.getId(), 7L);
    }

    @Test
    void shouldRefreshUserWithinClinicScope() {
        Clinics clinic = clinic(7L, "demo");
        Users user = user(2L, "sam", clinic);
        Jwt refreshJwt = mock(Jwt.class);
        Jwt accessJwt = mock(Jwt.class);
        Jwt newRefreshJwt = mock(Jwt.class);
        AuthSessions session = new AuthSessions();
        session.setId(UUID.randomUUID());
        session.setClinic(clinic);

        when(refreshJwt.getTokenType()).thenReturn("refresh");
        when(refreshJwt.isExpired()).thenReturn(false);
        when(refreshJwt.getClinicId()).thenReturn(7L);
        when(refreshJwt.getSid()).thenReturn(session.getId().toString());
        when(refreshJwt.getUsername()).thenReturn("sam");
        when(authSessionService.requireRefreshableSession(session.getId().toString(), 7L)).thenReturn(session);
        when(usersRepository.findByUsernameAndClinicId("sam", 7L)).thenReturn(Optional.of(user));
        when(jwtConfig.getRefreshTokenExpiration()).thenReturn(3600);
        when(authSessionService.createSession(any(), any())).thenReturn(session);
        when(jwtService.generateAccessToken(user, session.getId().toString(), 7L)).thenReturn(accessJwt);
        when(jwtService.generateRefreshToken(user, session.getId().toString(), 7L)).thenReturn(newRefreshJwt);

        LoginResult result = authService.refreshToken(refreshJwt);

        assertSame(accessJwt, result.accessToken());
        assertSame(newRefreshJwt, result.refreshToken());
        verify(usersRepository).findByUsernameAndClinicId("sam", 7L);
        verify(authSessionService).revokeSession(session.getId(), 7L);
    }

    private Clinics clinic(Long id, String slug) {
        Clinics clinic = new Clinics();
        clinic.setId(id);
        clinic.setSlug(slug);
        return clinic;
    }

    private Users user(Long id, String username, Clinics clinic) {
        Users user = new Users();
        user.setId(id);
        user.setUsername(username);
        user.setPasswordHash("encoded");
        user.setRoleName(Users.RoleName.ADMIN);
        user.setClinic(clinic);
        user.setEnabled(true);
        return user;
    }
}
