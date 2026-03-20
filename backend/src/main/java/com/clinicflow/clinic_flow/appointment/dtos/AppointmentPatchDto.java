package com.clinicflow.clinic_flow.appointment.dtos;

import com.clinicflow.clinic_flow.appointment.Appointment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AppointmentPatchDto {
    private LocalDateTime scheduledAt;
    private Long caseId;
    private Long therapistId;
    private Appointment.Status status;
}
