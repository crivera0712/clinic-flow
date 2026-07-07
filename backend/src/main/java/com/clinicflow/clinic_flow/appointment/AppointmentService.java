package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.appointment.dtos.AppointmentPatchDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentRequestDto;
import com.clinicflow.clinic_flow.appointment.dtos.BoardRowDto;
import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.exception.AppointmentAtTimeExistsException;
import com.clinicflow.clinic_flow.exception.AppointmentNotFoundException;
import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
import com.clinicflow.clinic_flow.exception.TherapistNotFoundException;
import com.clinicflow.clinic_flow.patient.Patient;
import com.clinicflow.clinic_flow.patient.PatientRepository;
import com.clinicflow.clinic_flow.therapist.Therapist;
import com.clinicflow.clinic_flow.therapist.TherapistRepository;
import com.clinicflow.clinic_flow.users.CurrentUserService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final TherapistRepository therapistRepository;
    private final PatientRepository patientRepository;
    private final CurrentUserService currentUserService;
    private final ClinicContextService clinicContextService;

    public List<BoardRowDto> getAppointmentsByDate(LocalDate date) {
        var clinicId = currentUserService.getCurrentClinicId();
        return appointmentRepository.findDailyAppointments(date, clinicId).stream()
                .map(appointmentMapper::projectionToBoardRow)
                .toList();
    }

    @Transactional
    public BoardRowDto createAppointment(AppointmentRequestDto request) {
        var clinic = clinicContextService.requireWritableClinic();
        var clinicId = clinic.getId();

        Therapist therapist = findTherapistOrThrow(request.getTherapistId(), clinicId);
        Patient patient = resolvePatient(request, clinic, clinicId);

        if (hasAppointmentConflict(clinicId, patient.getId(), request.getScheduledAt(), therapist.getId())) {
            throw new AppointmentAtTimeExistsException(request.getScheduledAt());
        }

        Appointment appointment = new Appointment();
        appointment.setScheduledAt(request.getScheduledAt());
        appointment.setType(request.getType());
        appointment.setStatus(Appointment.Status.SCHEDULED);
        appointment.setPatient(patient);
        appointment.setTherapist(therapist);
        appointment.setClinic(clinic);
        appointment.setCreatedAt(Instant.now());

        return appointmentMapper.entityToBoardRow(appointmentRepository.save(appointment));
    }

    @Transactional
    public BoardRowDto updateAppointmentById(Long id, AppointmentPatchDto request) {
        clinicContextService.assertWritableClinic();
        var clinicId = currentUserService.getCurrentClinicId();
        var appointment = findAppointmentOrThrow(id, clinicId);

        if (request == null) return appointmentMapper.entityToBoardRow(appointment);

        applySchedulingChangesAndValidateAppointmentConflict(appointment, request, clinicId);

        if (request.getStatus() != null) appointment.setStatus(request.getStatus());
        if (request.getType() != null) appointment.setType(request.getType());

        appointment.setModifiedAt(Instant.now());

        return appointmentMapper.entityToBoardRow(appointmentRepository.save(appointment));
    }

    public void deleteAppointmentById(Long id) {
        clinicContextService.assertWritableClinic();
        var clinicId = currentUserService.getCurrentClinicId();
        var appointment = findAppointmentOrThrow(id, clinicId);
        appointmentRepository.delete(appointment);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Patient resolvePatient(AppointmentRequestDto request, Clinics clinic, Long clinicId) {
        if (request.getPatientId() != null) {
            return patientRepository
                    .findByIdAndClinicId(request.getPatientId(), clinicId)
                    .orElseThrow(() -> new PatientNotFoundException(request.getPatientId()));
        }
        if (request.getPatient() != null) {
            Patient patient = new Patient();
            patient.setFirstName(request.getPatient().getFirstName());
            patient.setLastName(request.getPatient().getLastName());
            patient.setClinic(clinic);
            return patientRepository.save(patient);
        }
        throw new IllegalArgumentException("Either patientId or patient must be provided");
    }

    private void applySchedulingChangesAndValidateAppointmentConflict(
            Appointment appointment, AppointmentPatchDto request, Long clinicId) {
        var therapist = appointment.getTherapist();
        var scheduledAt = appointment.getScheduledAt();

        if (request.getTherapistId() != null) {
            therapist = findTherapistOrThrow(request.getTherapistId(), clinicId);
        }
        if (request.getScheduledAt() != null) {
            scheduledAt = request.getScheduledAt();
        }

        if (request.getTherapistId() != null || request.getScheduledAt() != null) {
            if (hasAppointmentConflictExcludingCurrent(
                    clinicId, appointment.getPatient().getId(), scheduledAt, therapist.getId(), appointment.getId())) {
                throw new AppointmentAtTimeExistsException(scheduledAt);
            }
        }

        appointment.setTherapist(therapist);
        appointment.setScheduledAt(scheduledAt);
    }

    private Appointment findAppointmentOrThrow(Long id, Long clinicId) {
        return appointmentRepository
                .findByIdAndClinicId(id, clinicId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found for this clinic"));
    }

    private Therapist findTherapistOrThrow(Long therapistId, Long clinicId) {
        return therapistRepository
                .findByIdAndClinicId(therapistId, clinicId)
                .orElseThrow(() -> new TherapistNotFoundException(therapistId));
    }

    private boolean hasAppointmentConflict(Long clinicId, Long patientId, LocalDateTime scheduledAt, Long therapistId) {
        if (appointmentRepository
                .findByScheduledAtAndClinicIdAndTherapistId(scheduledAt, clinicId, therapistId)
                .isPresent()) {
            return true;
        }
        return appointmentRepository.existsPatientAppointmentConflict(clinicId, patientId, scheduledAt);
    }

    private boolean hasAppointmentConflictExcludingCurrent(
            Long clinicId, Long patientId, LocalDateTime scheduledAt, Long therapistId, Long appointmentId) {
        var apptCheck =
                appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(scheduledAt, clinicId, therapistId);
        if (apptCheck.isPresent() && !apptCheck.get().getId().equals(appointmentId)) {
            return true;
        }
        return appointmentRepository.existsPatientAppointmentConflictExcludingAppointment(
                clinicId, patientId, scheduledAt, appointmentId);
    }
}
