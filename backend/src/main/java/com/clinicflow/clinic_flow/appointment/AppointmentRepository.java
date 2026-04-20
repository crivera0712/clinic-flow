package com.clinicflow.clinic_flow.appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    Page<Appointment> findByClinicIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThan(
            Long clinic_id, LocalDateTime scheduledAt, LocalDateTime scheduledAt2, Pageable pageable
    );

    Page<Appointment> findByClinicIdAndPtCaseIdOrderByScheduledAtDesc(Long clinicId, Long caseId, Pageable pageable);

    Optional<Appointment> findByScheduledAtAndClinicIdAndTherapistId(
            LocalDateTime scheduledAt,  Long clinicId, Long therapistId
    );

    Optional<Appointment> findByIdAndClinicId(Long id, Long clinicId);


    Page<Appointment> findAllByClinicId(Long clinicId, Pageable pageable);

    Page<Appointment> findAllByTherapistIdAndClinicId(Long therapistId, Long clinicId, Pageable pageable);

    Page<Appointment> findByTherapistIdAndClinicIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThan(
            Long therapistId, Long clinicId, LocalDateTime start, LocalDateTime end, Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM Appointment a WHERE a.clinic.id = :clinicId")
    void deleteAllByClinicId(@Param("clinicId") Long clinicId);


    @Query(value = """
    SELECT EXISTS (
        SELECT 1
        FROM appointments AS a
        JOIN cases AS c ON c.c_id = a.c_id
        WHERE a.clinic_id = :clinicId
          AND c.p_id = :patientId
          AND a.scheduled_at = :scheduledAt
    )
    """, nativeQuery = true)
    boolean existsPatientAppointmentConflict(
            @Param("clinicId") Long clinicId,
            @Param("patientId") Long patientId,
            @Param("scheduledAt") LocalDateTime scheduledAt
    );

    @Query(value = """
    SELECT EXISTS (
        SELECT 1
        FROM appointments AS a
        JOIN cases AS c ON c.c_id = a.c_id
        WHERE a.clinic_id = :clinicId
          AND c.p_id = :patientId
          AND a.scheduled_at = :scheduledAt
          AND a.apt_id <> :appointmentId
    )
    """, nativeQuery = true)
    boolean existsPatientAppointmentConflictExcludingAppointment(
            @Param("clinicId") Long clinicId,
            @Param("patientId") Long patientId,
            @Param("scheduledAt") LocalDateTime scheduledAt,
            @Param("appointmentId") Long appointmentId
    );
}
