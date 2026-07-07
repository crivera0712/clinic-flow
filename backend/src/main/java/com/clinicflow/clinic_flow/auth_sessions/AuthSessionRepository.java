package com.clinicflow.clinic_flow.auth_sessions;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSessions, UUID> {
    Optional<AuthSessions> findByIdAndClinicId(UUID uuid, Long clinicId);
}
