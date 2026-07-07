package com.clinicflow.clinic_flow.auth_sessions;

import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.users.Users;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "auth_sessions")
public class AuthSessions {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "refresh_expires_at")
    private LocalDateTime refreshExpiresAt;

    @ManyToOne
    @JoinColumn(name = "clinic_id")
    private Clinics clinic;
}
