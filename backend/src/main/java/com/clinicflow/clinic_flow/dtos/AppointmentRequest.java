package com.clinicflow.clinic_flow.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppointmentRequest {
    @NotNull(message = "scheduled time cannot be missing")
    private LocalDateTime scheduledAt;
    private Instant modifiedAt;
    @NotNull(message = "caseId cannot be missing")
    private Long caseId;
    @NotNull(message = "therapistId cannot be missing")
    private Long therapistId;
}
