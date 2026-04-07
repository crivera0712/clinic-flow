package com.clinicflow.clinic_flow.appointment.dtos;

import com.clinicflow.clinic_flow.appointment.Appointment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AppointmentPatchDto {
    private LocalDateTime scheduledAt;
    private Long caseId;
    private Long therapistId;
    private Appointment.Status status;
    private Appointment.Type type;
}
