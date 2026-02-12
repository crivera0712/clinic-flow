package com.clinicflow.clinic_flow.entity;

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

    private enum therapistType{
        PHYSICAL_THERAPY_ASSISTANT,
        PHYSICAL_THERAPIST,
        OCCUPATIONAL_THERAPIST
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "t_id")
    private Long id;

    @Column(name = "therapist_name")
    private String therapistName;

    @Enumerated(EnumType.STRING)
    @Column(name = "therapist_type", nullable = false)
    private therapistType type;

    @OneToMany(mappedBy = "therapist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Appointment> appointments = new HashSet<>();


}
