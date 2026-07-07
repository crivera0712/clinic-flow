package com.clinicflow.clinic_flow.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query(
            value =
                    """
    SELECT
        a.apt_id AS id,
        a.scheduled_at AS scheduledAt,
        a.status AS status,
        a.type AS type,
        p.p_id AS patientId,
        CONCAT(p.first_name, ' ', p.last_name) AS patientName,
        t.t_id AS therapistId,
        t.therapist_name AS therapistName
    FROM appointments a
        JOIN therapists t ON t.t_id = a.t_id
        JOIN patients p ON p.p_id = a.p_id
    WHERE a.clinic_id = :clinicId
        AND CAST(a.scheduled_at AS DATE) = :date
    ORDER BY a.scheduled_at
    """,
            nativeQuery = true)
    List<AppointmentBoardProjection> findDailyAppointments(
            @Param("date") LocalDate date, @Param("clinicId") Long clinicId);

    Optional<Appointment> findByIdAndClinicId(Long id, Long clinicId);

    Optional<Appointment> findByScheduledAtAndClinicIdAndTherapistId(
            LocalDateTime scheduledAt, Long clinicId, Long therapistId);

    @Modifying
    @Query("DELETE FROM Appointment a WHERE a.clinic.id = :clinicId")
    void deleteAllByClinicId(@Param("clinicId") Long clinicId);

    @Query(
            value =
                    """
    SELECT EXISTS (
        SELECT 1 FROM appointments a
        WHERE a.clinic_id = :clinicId
          AND a.p_id = :patientId
          AND a.scheduled_at = :scheduledAt
    )
    """,
            nativeQuery = true)
    boolean existsPatientAppointmentConflict(
            @Param("clinicId") Long clinicId,
            @Param("patientId") Long patientId,
            @Param("scheduledAt") LocalDateTime scheduledAt);

    @Query(
            value =
                    """
    SELECT EXISTS (
        SELECT 1 FROM appointments a
        WHERE a.clinic_id = :clinicId
          AND a.p_id = :patientId
          AND a.scheduled_at = :scheduledAt
          AND a.apt_id <> :appointmentId
    )
    """,
            nativeQuery = true)
    boolean existsPatientAppointmentConflictExcludingAppointment(
            @Param("clinicId") Long clinicId,
            @Param("patientId") Long patientId,
            @Param("scheduledAt") LocalDateTime scheduledAt,
            @Param("appointmentId") Long appointmentId);
}
