package com.clinicflow.clinic_flow.repositories;

import com.clinicflow.clinic_flow.entity.Therapist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TherapistRepository extends JpaRepository<Therapist, Long> {
    Therapist findById(long id);
}