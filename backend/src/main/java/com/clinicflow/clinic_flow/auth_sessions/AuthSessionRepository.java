package com.clinicflow.clinic_flow.auth_sessions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository <AuthSessions, UUID> {
}
