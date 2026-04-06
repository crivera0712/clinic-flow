package com.clinicflow.clinic_flow.clinics;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
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
