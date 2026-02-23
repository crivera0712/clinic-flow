package com.clinicflow.clinic_flow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.processing.Exclude;

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

    public enum TherapistType{
        PHYSICAL_THERAPY_ASSISTANT("Physcial Therapy Assistant"),
        PHYSICAL_THERAPIST("Physical Therapist"),
        OCCUPATIONAL_THERAPIST("Occupational Therapist");

        private final String displayName;

        TherapistType(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
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


}
