package com.clinicflow.clinic_flow.appointment;

import java.time.LocalDateTime;

public interface AppointmentScheduleProjection {
    LocalDateTime getScheduledAt();
    Long getAptId();
    Long getCaseId();
    String getFirstName();
    String getLastName();
    Long getTherapistId();
    String getTherapistName();
    String getTherapistType();
    String getBodyRegionDisplayName();
    Appointment.Status getStatus();
}
