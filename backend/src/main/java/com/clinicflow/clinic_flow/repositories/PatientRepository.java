package com.clinicflow.clinic_flow.repositories;

import com.clinicflow.clinic_flow.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
