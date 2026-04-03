package com.clinicflow.clinic_flow.auth.records;

public record AuthPrincipal(Long userId, String sid, Long clinicId) {
}
