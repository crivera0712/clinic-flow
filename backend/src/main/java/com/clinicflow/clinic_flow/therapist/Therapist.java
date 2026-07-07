package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.appointment.Appointment;
import com.clinicflow.clinic_flow.clinics.Clinics;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "therapists")
public class Therapist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "t_id")
    private Long id;

    @Column(name = "therapist_name")
    private String therapistName;

    @OneToMany(mappedBy = "therapist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Appointment> appointments = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "clinic_id")
    private Clinics clinic;
}
