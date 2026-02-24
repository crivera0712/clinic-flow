package com.clinicflow.clinic_flow.repositories;

import com.clinicflow.clinic_flow.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CaseRepository extends JpaRepository<Case, Long> {
    Case getCaseById(Long id);

    @Query(value = """
    SELECT *
    FROM cases AS c
    WHERE c.p_id = :id
    ORDER BY c.created_at
""", nativeQuery = true)
    List<Case> getCaseByPatient(@Param("id")Long id);

}