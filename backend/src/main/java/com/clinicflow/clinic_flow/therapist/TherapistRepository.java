package com.clinicflow.clinic_flow.therapist;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TherapistRepository extends JpaRepository<Therapist, Long> {
    Therapist findById(long id);
}