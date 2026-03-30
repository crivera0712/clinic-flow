package com.clinicflow.clinic_flow.auth_sessions;

import com.clinicflow.clinic_flow.config.JwtConfig;
import com.clinicflow.clinic_flow.exception.InvalidSessionException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AuthSessionService {

    private final AuthSessionRepository authSessionRepository;
    private final JwtConfig jwtConfig;

    public boolean isAccessSessionActive(String sid) {
        var session = authSessionRepository.findById(sid).orElse(null);
        return session != null && session.getRevokedAt() == null;
    }

    public AuthSessions requireRefreshableSession(String sid) {
        var session = authSessionRepository.findById(sid).orElseThrow(() ->
                new InvalidSessionException("Could not find session"));

        if (session.getRevokedAt() != null) {
            throw new InvalidSessionException("Session has been revoked");
        }

        if (session.getRefreshExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidSessionException("Session has expired");
        }

        return session;
    }

    @Transactional
    public void checkRevokedAt(String sid){
        var session = authSessionRepository.findById(sid).orElse(null);

        if (
                session != null && session.getRevokedAt() == null &&
                session.getRefreshExpiresAt().isBefore(LocalDateTime.now())
        ) {
            session.setRevokedAt(LocalDateTime.now());
            authSessionRepository.save(session);
        }
    }

}
