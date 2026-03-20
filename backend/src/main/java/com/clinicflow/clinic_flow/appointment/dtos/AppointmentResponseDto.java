package com.clinicflow.clinic_flow.appointment.dtos;

import com.clinicflow.clinic_flow.appointment.Appointment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AppointmentResponseDto {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime scheduledAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Instant modifiedAt;
    private Long caseId;
    private Long therapistId;
    private Appointment.Status status;
}
