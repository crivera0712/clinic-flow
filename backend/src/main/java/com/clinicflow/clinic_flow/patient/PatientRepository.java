package com.clinicflow.clinic_flow.patient;

import com.clinicflow.clinic_flow.therapist.TherapistMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByIdAndClinicId(Long id, Long clinicId);

    @Query(value = """
    SELECT *
    FROM patients AS p
    WHERE p.clinic_id = :clinicId AND (
        p.first_name LIKE CONCAT(:q, '%')
    OR p.last_name  LIKE CONCAT(:q, '%')
        )
""", nativeQuery = true
    )
    List<Patient> searchPatientByOneToken(@Param("q") String q, @Param("clinicId") Long clinicId);

    @Query(value = """
    SELECT *
    FROM patients AS p
    WHERE p.clinic_id = :clinicId
    AND (
            (
            p.first_name LIKE CONCAT(:q1, '%')
            AND p.last_name LIKE CONCAT(:q2, '%')
        )
        OR (
            p.first_name LIKE CONCAT(:q2, '%')
            AND p.last_name LIKE CONCAT(:q1, '%')
        )
    )
""", nativeQuery = true)
    List<Patient> searchPatientByTwoTokens(
            @Param("q1") String q1,
            @Param("q2") String q2,
            @Param("clinicId") Long clinicId);

    Page<Patient> findAllByClinicId(Long clinicId, Pageable pageable);
}
