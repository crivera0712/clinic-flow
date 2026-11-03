package com.clinicflow.clinic_flow.auth_sessions;

import com.clinicflow.clinic_flow.exception.InvalidSessionException;
import com.clinicflow.clinic_flow.users.Users;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AuthSessionService {

    private final AuthSessionRepository authSessionRepository;

    public boolean isAccessSessionActive(String sid, Long clinicId) {
        var session = authSessionRepository
                .findByIdAndClinicId(parseSessionId(sid), clinicId)
                .orElse(null);
        return session != null && session.getRevokedAt() == null;
    }

    @Transactional
    public AuthSessions createSession(Users user, LocalDateTime refreshExpiresAt) {

        AuthSessions session = new AuthSessions();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setClinic(user.getClinic());
        session.setCreatedAt(LocalDateTime.now());
        session.setRefreshExpiresAt(refreshExpiresAt);
        return authSessionRepository.save(session);
    }

    @Transactional
    public AuthSessions requireRefreshableSession(String sid, Long clinicId) {
        var session = findAuthSessionOrThrow(parseSessionId(sid), clinicId);

        if (session.getRefreshExpiresAt().isBefore(LocalDateTime.now()) && session.getRevokedAt() == null) {
            session.setRevokedAt(LocalDateTime.now());
            authSessionRepository.save(session);
        }

        if (session.getRevokedAt() != null) {
            throw new InvalidSessionException("Session has been revoked");
        }

        if (session.getRefreshExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidSessionException("Session has expired");
        }

        return session;
    }

    @Transactional
    public void revokeSession(String sid, Long clinicId) {
        var session = findAuthSessionOrThrow(parseSessionId(sid), clinicId);
        if (session.getRevokedAt() == null) {
            session.setRevokedAt(LocalDateTime.now());
            authSessionRepository.save(session);
            log.info("Logout success clinicId={}", clinicId);
        }
    }

    @Transactional
    public void revokeSession(UUID sid, Long clinicId) {
        var session = findAuthSessionOrThrow(sid, clinicId);

        if (session.getRevokedAt() == null) {
            session.setRevokedAt(LocalDateTime.now());
            authSessionRepository.save(session);
        }
    }

    // Signs a user out everywhere: every open session (and so every access token issued for it) stops working.
    @Transactional
    public void revokeAllSessions(Long userId, Long clinicId) {
        var now = LocalDateTime.now();
        var open = authSessionRepository.findAllByUserIdAndClinicIdAndRevokedAtIsNull(userId, clinicId);
        open.forEach(session -> session.setRevokedAt(now));
        authSessionRepository.saveAll(open);
        log.info("Revoked all sessions userId={} clinicId={} count={}", userId, clinicId, open.size());
    }

    @Transactional
    public boolean checkRevokedAt(String sid, Long clinicId) {
        var session = findAuthSessionOrThrow(parseSessionId(sid), clinicId);

        if (session.getRefreshExpiresAt().isBefore(LocalDateTime.now()) && session.getRevokedAt() == null) {
            session.setRevokedAt(LocalDateTime.now());
            authSessionRepository.save(session);
            return false;
        }
        return session.getRevokedAt() == null;
    }

    private UUID parseSessionId(String sid) {
        try {
            return UUID.fromString(sid);
        } catch (IllegalArgumentException e) {
            throw new InvalidSessionException("Invalid session ID");
        }
    }

    private AuthSessions findAuthSessionOrThrow(UUID sid, Long clinicId) {
        return authSessionRepository
                .findByIdAndClinicId(sid, clinicId)
                .orElseThrow(() -> new InvalidSessionException("Could not find session"));
    }
}
