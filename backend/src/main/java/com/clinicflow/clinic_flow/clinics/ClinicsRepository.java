package com.clinicflow.clinic_flow.clinics;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicsRepository extends JpaRepository<Clinics, Long> {
    Optional<Clinics> findClinicsBySlug(String slug);
}
