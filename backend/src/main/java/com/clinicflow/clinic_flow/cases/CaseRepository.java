package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.patient.Patient;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CaseRepository extends JpaRepository<Case, Long> {
    @Query(value = """
    SELECT *
    FROM cases AS c
    WHERE
        c.clinic_id = :clinicId
    AND
        c.p_id = :id
    ORDER BY c.created_at
""", nativeQuery = true)
    List<Case> getCaseByPatient(@Param("id")Long id, @Param("clinicID") Long clinicID);

    Optional<Case> findByIdAndClinicId(@NotNull(message = "caseId cannot be missing") Long caseId, Long clinicId);

    List<Case> findAllByClinicId(Long clinicId);
}