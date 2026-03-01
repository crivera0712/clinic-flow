package com.clinicflow.clinic_flow.repositories;
import com.clinicflow.clinic_flow.entity.Appointment;
import com.clinicflow.clinic_flow.projections.AppointmentScheduleProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query(value = """
    SELECT
        a.scheduled_at AS scheduledAt,
        a.apt_id AS aptId,
        a.c_id AS caseId,
        a.status as status,
        p.first_name AS firstName,
        p.last_name AS lastName,
        t.t_id AS therapistId,
        t.therapist_name AS therapistName,
        t.therapist_type AS therapistType,
        br.display_name AS bodyRegionDisplayName
    FROM appointments a
        JOIN therapists t ON t.t_id = a.t_id
        JOIN cases c ON c.c_id = a.c_id
        JOIN patients p ON p.p_id = c.p_id
        JOIN body_regions br ON br.br_id = c.br_id
    WHERE DATE(a.scheduled_at) = :date
    ORDER BY a.scheduled_at
    """, nativeQuery = true)
    List<AppointmentScheduleProjection> findDailyAppointments(@Param("date") LocalDate date);
}