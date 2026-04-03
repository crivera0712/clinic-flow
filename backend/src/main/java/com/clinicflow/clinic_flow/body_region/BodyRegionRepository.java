package com.clinicflow.clinic_flow.body_region;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.lang.ScopedValue;
import java.util.Optional;

public interface BodyRegionRepository extends JpaRepository<BodyRegion, Long> {
    Page<BodyRegion> findByCodeContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
            String code,
            String displayName,
            Pageable pageable
    );
}
