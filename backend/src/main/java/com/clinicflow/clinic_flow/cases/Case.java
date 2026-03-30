package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.appointment.Appointment;
import com.clinicflow.clinic_flow.body_region.BodyRegion;
import com.clinicflow.clinic_flow.patient.Patient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "cases")
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "c_id")
    private Long id;

    @Column(name = "created_at")
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "p_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "br_id")
    private BodyRegion bodyRegion;

    /*
    @OneToMany(mappedBy = "ptCase", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private Set<Appointment> appointments = new HashSet<>();
*/

}
