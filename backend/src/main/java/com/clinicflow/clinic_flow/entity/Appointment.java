package com.clinicflow.clinic_flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "appointments")
public class Appointment {

    public enum Status {
        CHECKED_IN,
        IN_SESSION,
        FINISHED,
        SCHEDULED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apt_id")
    private Long id;

    @Setter
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Setter
    @Column(name = "created_at")
    private Instant createdAt;

    @Setter
    @Column(name = "modified_at")
    private Instant modifiedAt;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Setter
    @ManyToOne
    @JoinColumn(name = "c_id")
    private Case ptCase;

    @Setter
    @ManyToOne
    @JoinColumn(name = "t_id")
    private Therapist therapist;

}
