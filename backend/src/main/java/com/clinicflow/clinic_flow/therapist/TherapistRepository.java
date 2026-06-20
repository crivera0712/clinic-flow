package com.clinicflow.clinic_flow.therapist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TherapistRepository extends JpaRepository<Therapist, Long> {
    Optional<Therapist> findByIdAndClinicId(Long therapistId, Long clinicId);

    List<Therapist> findAllByClinicId(Long clinicId);
}
