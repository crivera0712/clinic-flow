package com.clinicflow.clinic_flow.appointment.dtos;

import com.clinicflow.clinic_flow.appointment.Appointment;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Denormalized appointment row used by both the gym board and the front-desk console.
 * Serialized with ISO local date-time (e.g. "2026-06-20T14:30:00") so the frontend can
 * feed it straight into datetime-local inputs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardRowDto {
    private Long id;
    private LocalDateTime scheduledAt;
    private Appointment.Type type;
    private Appointment.Status status;
    private Long patientId;
    private String patientName;
    private Long therapistId;
    private String therapistName;
}
