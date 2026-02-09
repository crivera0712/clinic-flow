package com.clinicflow.clinic_flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "body_regions")
public class BodyRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "br_id")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "is_active")
    private Boolean isActive;

}
