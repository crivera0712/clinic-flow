package com.clinicflow.clinic_flow.auth_sessions;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository <AuthSessions, String> {
}
