package com.clinicflow.clinic_flow.users;

import com.clinicflow.clinic_flow.clinics.Clinics;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {

    public enum RoleName {
        DISPLAY,
        ADMIN;

        @Override
        public String toString() {
            return super.toString();
        }
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private RoleName roleName;

    @ManyToOne
    @JoinColumn(name = "clinic_id")
    private Clinics clinic;
}
