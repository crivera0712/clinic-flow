package com.clinicflow.clinic_flow.appointment.dtos;

import com.clinicflow.clinic_flow.appointment.Appointment;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentPatchDto {
    private LocalDateTime scheduledAt;
    private Long therapistId;
    private Appointment.Status status;
    private Appointment.Type type;
}
