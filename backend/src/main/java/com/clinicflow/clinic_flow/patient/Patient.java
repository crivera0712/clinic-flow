package com.clinicflow.clinic_flow.patient;

import com.clinicflow.clinic_flow.cases.Case;
import com.clinicflow.clinic_flow.clinics.Clinics;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "p_id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "display_name")
    private String displayName;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Case> cases = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "clinic_id")
    private Clinics clinic;
}
