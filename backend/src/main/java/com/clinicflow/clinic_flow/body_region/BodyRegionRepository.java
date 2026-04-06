package com.clinicflow.clinic_flow.body_region;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BodyRegionRepository extends JpaRepository<BodyRegion, Long> {
    Page<BodyRegion> findByCodeContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
            String code,
            String displayName,
            Pageable pageable
    );
}
