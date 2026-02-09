package com.clinicflow.clinic_flow.repositories;

import com.clinicflow.clinic_flow.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseRepository extends JpaRepository<Case, Long> {
}