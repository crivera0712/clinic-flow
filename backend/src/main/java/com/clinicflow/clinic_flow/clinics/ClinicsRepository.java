package com.clinicflow.clinic_flow.clinics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClinicsRepository extends JpaRepository<Clinics, Long> {
    Optional<Clinics> findClinicsBySlug(String slug);
}
