package com.clinicflow.clinic_flow.auth_sessions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.exception.InvalidSessionException;
import com.clinicflow.clinic_flow.users.CurrentUserService;
import com.clinicflow.clinic_flow.users.Users;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthSessionServiceTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AuthSessionService authSessionService;

    @Test
    void shouldReturnFalseWhenAccessSessionExistsOnlyInAnotherClinic() {
        UUID sessionId = UUID.randomUUID();

        when(authSessionRepository.findByIdAndClinicId(sessionId, 7L)).thenReturn(Optional.empty());

        boolean result = authSessionService.isAccessSessionActive(sessionId.toString(), 7L);

        assertFalse(result);
    }

    @Test
    void shouldThrowWhenRefreshingSessionFromAnotherClinic() {
        UUID sessionId = UUID.randomUUID();

        when(authSessionRepository.findByIdAndClinicId(sessionId, 7L)).thenReturn(Optional.empty());

        assertThrows(
                InvalidSessionException.class,
                () -> authSessionService.requireRefreshableSession(sessionId.toString(), 7L));
    }

    @Test
    void shouldRevokeOnlyMatchingClinicSession() {
        UUID sessionId = UUID.randomUUID();
        AuthSessions session = session(sessionId, 7L);

        when(authSessionRepository.findByIdAndClinicId(sessionId, 7L)).thenReturn(Optional.of(session));

        authSessionService.revokeSession(sessionId.toString(), 7L);

        assertTrue(session.getRevokedAt() != null);
        verify(authSessionRepository).save(session);
    }

    @Test
    void shouldThrowWhenRevokingSessionFromAnotherClinic() {
        UUID sessionId = UUID.randomUUID();

        when(authSessionRepository.findByIdAndClinicId(sessionId, 7L)).thenReturn(Optional.empty());

        assertThrows(InvalidSessionException.class, () -> authSessionService.revokeSession(sessionId.toString(), 7L));

        verify(authSessionRepository, never()).save(org.mockito.ArgumentMatchers.any(AuthSessions.class));
    }

    private AuthSessions session(UUID id, Long clinicId) {
        Clinics clinic = new Clinics();
        clinic.setId(clinicId);
        Users user = new Users();
        user.setId(1L);
        user.setClinic(clinic);

        AuthSessions session = new AuthSessions();
        session.setId(id);
        session.setClinic(clinic);
        session.setUser(user);
        session.setCreatedAt(LocalDateTime.now());
        session.setRefreshExpiresAt(LocalDateTime.now().plusDays(1));
        return session;
    }
}
