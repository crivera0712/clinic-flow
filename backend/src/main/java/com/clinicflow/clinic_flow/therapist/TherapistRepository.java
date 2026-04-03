package com.clinicflow.clinic_flow.therapist;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TherapistRepository extends JpaRepository<Therapist, Long> {
    @Query("""
            SELECT t
            FROM Therapist t
            WHERE lower(t.therapistName) LIKE lower(concat('%', :search, '%'))
               OR lower(cast(t.type as string)) LIKE lower(concat('%', :search, '%'))
            """)
    Page<Therapist> search(@Param("search") String search, Pageable pageable);

    Optional <Therapist> findByIdAndClinicId(@NotNull(message = "therapistId cannot be missing") Long therapistId, Long clinicId);
}
