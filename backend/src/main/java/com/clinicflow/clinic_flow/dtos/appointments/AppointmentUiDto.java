package com.clinicflow.clinic_flow.dtos.appointments;

import com.clinicflow.clinic_flow.entity.Appointment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class AppointmentUiDto {
    private Long aptId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime scheduledAt;
    private Long caseId;
    private String firstName;
    private String lastName;
    private Long therapistId;
    private String therapistName;
    private String therapistType;
    private String bodyRegionDisplayName;
    private Appointment.Status status;
}
