package com.clinicflow.clinic_flow.appointment.dtos;

import com.clinicflow.clinic_flow.appointment.Appointment;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppointmentRequestDto {
    @NotNull(message = "scheduled time cannot be missing")
    private LocalDateTime scheduledAt;

    @NotNull(message = "therapistId cannot be missing")
    private Long therapistId;

    @NotNull(message = "appointment type cannot be null")
    private Appointment.Type type;

    // Provide exactly one of: an existing patient id, or new patient fields (created inline).
    private Long patientId;
    private NewPatient patient;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewPatient {
        private String firstName;
        private String lastName;
    }
}
