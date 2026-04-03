package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.cases.Case;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.therapist.Therapist;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "appointments")
public class Appointment {

    public enum Status {
        CHECKED_IN,
        IN_SESSION,
        FINISHED,
        SCHEDULED
    }

    public enum Type {
        EVALUATION,
        REASSESSMENT,
        FOLLOW_UP
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apt_id")
    private Long id;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "modified_at")
    private Instant modifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Type type;

    @ManyToOne
    @JoinColumn(name = "c_id")
    private Case ptCase;

    @ManyToOne
    @JoinColumn(name = "t_id")
    private Therapist therapist;

    @ManyToOne
    @JoinColumn(name = "clinic_id")
    private Clinics clinic;

}
