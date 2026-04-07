package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.appointment.Appointment;
import com.clinicflow.clinic_flow.clinics.Clinics;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "therapists")
public class Therapist {

    @Getter
    public enum TherapistType{
        PHYSICAL_THERAPY_ASSISTANT("Physcial Therapy Assistant"),
        PHYSICAL_THERAPIST("Physical Therapist"),
        OCCUPATIONAL_THERAPIST("Occupational Therapist");

        private final String displayName;

        TherapistType(String displayName) {
            this.displayName = displayName;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "t_id")
    private Long id;

    @Column(name = "therapist_name")
    private String therapistName;

    @Enumerated(EnumType.STRING)
    @Column(name = "therapist_type", nullable = false)
    private TherapistType type;

    @OneToMany(mappedBy = "therapist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Appointment> appointments = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "clinic_id")
    private Clinics clinic;


}
