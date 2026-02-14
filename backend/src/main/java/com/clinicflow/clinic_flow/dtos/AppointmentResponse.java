package com.clinicflow.clinic_flow.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class AppointmentResponse {
    @NotNull(message = "appointment id cannot be missing")
    private Long id;
    @NotNull(message = "scheduled time cannot be missing")
    private LocalDateTime scheduledAt;
    private Instant modifiedAt;
    @NotNull(message = "caseId cannot be missing")
    private Long caseId;
    @NotNull(message = "therapistId cannot be missing")
    private Long therapistId;
}
