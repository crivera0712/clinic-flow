package com.clinicflow.clinic_flow.dtos.appointments;

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
    private LocalDateTime scheduledAt;
    private Instant modifiedAt;

    private Long caseId;
    private Long therapistId;
}
