package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.body_region.BodyRegion;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.patient.Patient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

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

    @ManyToOne
    @JoinColumn(name = "clinic_id")
    private Clinics clinic;

}
