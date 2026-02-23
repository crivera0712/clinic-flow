package com.clinicflow.clinic_flow.repositories;

import com.clinicflow.clinic_flow.entity.BodyRegion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodyRegionRepository extends JpaRepository<BodyRegion, Long> {
    BodyRegion getBodyRegionById(Long id);
}