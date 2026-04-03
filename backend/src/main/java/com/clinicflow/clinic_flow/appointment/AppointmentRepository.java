package com.clinicflow.clinic_flow.appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query(value = """
    SELECT
        a.clinic_id,
        a.scheduled_at AS scheduledAt,
        a.apt_id AS aptId,
        a.c_id AS caseId,
        a.status as status,
        a.type as type,
        p.first_name AS firstName,
        p.last_name AS lastName,
        p.display_name AS displayName,
        t.t_id AS therapistId,
        t.therapist_name AS therapistName,
        t.therapist_type AS therapistType,
        br.display_name AS bodyRegionDisplayName
    FROM appointments a
        JOIN therapists t ON t.t_id = a.t_id
        JOIN cases c ON c.c_id = a.c_id
        JOIN patients p ON p.p_id = c.p_id
        JOIN body_regions br ON br.br_id = c.br_id
    WHERE
            a.clinic_id = :clinicId
            AND CAST(a.scheduled_at AS DATE) = :date
    ORDER BY a.scheduled_at
    """, nativeQuery = true)
    List<AppointmentScheduleProjection> findDailyAppointments(
            @Param("date") LocalDate date,
            @Param("clinicId") Long clinicId);

    Page<Appointment> findByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
            LocalDateTime startInclusive,
            LocalDateTime endExclusive,
            Pageable pageable,
            Long clinicId
    );

    Page<Appointment> findByClinicIdAndPtCaseIdOrderByScheduledAtDesc(Long clinicId, Long caseId, Pageable pageable);

    Optional<Appointment> findByScheduledAtAndClinicIdAndTherapistId(
            LocalDateTime scheduledAt,  Long clinicId, Long therapistId
    );

    Page<Appointment> findByIdAndClinicIdAndPageable(Long id, Long clinicId, Pageable pageable);

    Optional<Appointment> findByIdAndClinicId(Long id, Long clinicId);


    Page<Appointment> findAllByClinicId(Long clinicId, Pageable pageable);
}
