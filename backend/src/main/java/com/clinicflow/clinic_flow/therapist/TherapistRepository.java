package com.clinicflow.clinic_flow.therapist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TherapistRepository extends JpaRepository<Therapist, Long> {
    @Query("""
            SELECT t
            FROM Therapist t
            WHERE :search IS NULL
               OR trim(:search) = ''
               OR lower(t.therapistName) LIKE lower(concat('%', :search, '%'))
               OR lower(cast(t.type as string)) LIKE lower(concat('%', :search, '%'))
            """)
    Page<Therapist> search(@Param("search") String search, Pageable pageable);
}
