package com.clinicflow.clinic_flow.dtos.appointments;

import com.clinicflow.clinic_flow.entity.Appointment;
import jakarta.validation.constraints.NotBlank;
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
public class AppointmentRequestDto {
    @NotBlank(message = "scheduled time cannot be missing")
    private LocalDateTime scheduledAt;

    private Instant modifiedAt;

    @NotBlank(message = "caseId cannot be missing")
    private Long caseId;

    @NotBlank(message = "therapistId cannot be missing")
    private Long therapistId;

    private Appointment.Status status;
}
