package com.clinicflow.clinic_flow.therapist;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TherapistRepository extends JpaRepository<Therapist, Long> {
    @Query(value = """
    SELECT *
    FROM therapists t
    WHERE t.clinic_id = :clinicId
        AND (
            LOWER(t.therapist_name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(t.therapist_type) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM therapists t
    WHERE t.clinic_id = :clinicId
    AND (
            LOWER(t.therapist_name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(t.therapist_type) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    """,
            nativeQuery = true)
    Page<Therapist> search(@Param("search") String search, Pageable pageable, @Param("clinicId") Long clinicId);

    Optional<Therapist> searchByTherapistNameAndClinicId(String therapistName, Long clinicId);

    Optional <Therapist> findByIdAndClinicId(@NotNull(message = "therapistId cannot be missing") Long therapistId, Long clinicId);

    Page<Therapist> findAllByClinicId(Long clinicId, Pageable pageable);

    List<Therapist> findAllByClinicId(Long clinicId);
}
