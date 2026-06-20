package com.clinicflow.clinic_flow.appointment;

import java.time.LocalDateTime;

/** Native-query projection backing the daily board (see AppointmentRepository.findDailyAppointments). */
public interface AppointmentBoardProjection {
    Long getId();
    LocalDateTime getScheduledAt();
    Appointment.Type getType();
    Appointment.Status getStatus();
    Long getPatientId();
    String getPatientName();
    Long getTherapistId();
    String getTherapistName();
}
