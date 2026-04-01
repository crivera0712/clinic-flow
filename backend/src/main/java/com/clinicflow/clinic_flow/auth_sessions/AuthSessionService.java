package com.clinicflow.clinic_flow.auth_sessions;

import com.clinicflow.clinic_flow.exception.InvalidSessionException;
import com.clinicflow.clinic_flow.users.Users;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthSessionService {

    private final AuthSessionRepository authSessionRepository;

    public boolean isAccessSessionActive(String sid) {
        var session = authSessionRepository.findById(sid).orElse(null);
        return session != null && session.getRevokedAt() == null;
    }

    @Transactional
    public AuthSessions createSession(Users user, LocalDateTime refreshExpiresAt) {
        AuthSessions session = new AuthSessions();
        session.setId(UUID.randomUUID().toString());
        session.setUser(user);
        session.setCreatedAt(LocalDateTime.now());
        session.setRefreshExpiresAt(refreshExpiresAt);
        return authSessionRepository.save(session);
    }

    @Transactional
    public AuthSessions requireRefreshableSession(String sid) {
        var session = authSessionRepository.findById(sid).orElseThrow(() ->
                new InvalidSessionException("Could not find session"));

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
    public void revokeSession(String sid) {
        var session = authSessionRepository.findById(sid).orElseThrow(() ->
                new InvalidSessionException("Could not find session"));

        if (session.getRevokedAt() == null) {
            session.setRevokedAt(LocalDateTime.now());
            authSessionRepository.save(session);
        }
    }

    @Transactional
    public boolean checkRevokedAt(String sid){
        var session = authSessionRepository.findById(sid).orElse(null);

        if  (session == null) {
            throw new InvalidSessionException("Could not find session");
        }

        if (session.getRefreshExpiresAt().isBefore(LocalDateTime.now()) && session.getRevokedAt() == null) {
            session.setRevokedAt(LocalDateTime.now());
            authSessionRepository.save(session);
            return false;
        }
        return session.getRevokedAt() == null;
    }

}
