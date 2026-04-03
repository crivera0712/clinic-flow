package com.clinicflow.clinic_flow.clinics;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "clinics")
public class Clinics {

    @Id
    @Column(name = "clinic_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "slug")
    private String slug;

    @Column(name = "is_demo")
    private Boolean isDemo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
