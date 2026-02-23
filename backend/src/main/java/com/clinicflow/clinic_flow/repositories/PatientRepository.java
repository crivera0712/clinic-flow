package com.clinicflow.clinic_flow.repositories;

import com.clinicflow.clinic_flow.dtos.patients.PatientResponseDto;
import com.clinicflow.clinic_flow.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient getPatientById(Long id);

    @Query(value = """
    SELECT *
    FROM patients AS p
    WHERE p.first_name LIKE CONCAT(:q, '%')
    OR p.last_name  LIKE CONCAT(:q, '%')
""", nativeQuery = true
    )
    List<PatientResponseDto> searchPatientByOneToken(@Param("q") String q);

    @Query(value = """
    SELECT *
    FROM patients p
    WHERE (
        p.first_name LIKE CONCAT(:q1, '%')
        AND p.last_name LIKE CONCAT(:q2, '%')
    )
    OR (
        p.first_name LIKE CONCAT(:q2, '%')
        AND p.last_name LIKE CONCAT(:q1, '%')
)""", nativeQuery = true)
    List<PatientResponseDto> searchPatientByTwoTokens(String q1,  String q2);
}
