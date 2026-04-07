package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.appointment.dtos.*;
import com.clinicflow.clinic_flow.cases.Case;
import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.exception.*;
import com.clinicflow.clinic_flow.therapist.Therapist;
import com.clinicflow.clinic_flow.cases.CaseRepository;
import com.clinicflow.clinic_flow.therapist.TherapistRepository;
import com.clinicflow.clinic_flow.users.CurrentUserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final TherapistRepository therapistRepository;
    private final CaseRepository caseRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CurrentUserService currentUserService;
    private final ClinicContextService clinicContextService;

    public Page<AppointmentResponseDto> getAllAppointments(
            Pageable pageable, LocalDate date, Long caseId
    ) {
        Page<Appointment> result;

        var clinicId = currentUserService.getCurrentClinicId();

        if (caseId != null) {
            result = appointmentRepository.findByClinicIdAndPtCaseIdOrderByScheduledAtDesc(clinicId, caseId, pageable);
        } else if (date == null) {
            result = appointmentRepository.findAllByClinicId(clinicId, pageable);
        } else {
            result = appointmentRepository.findByClinicIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                    clinicId,
                    date.atStartOfDay(),
                    date.plusDays(1).atStartOfDay(),
                    pageable
            );
        }
        return result
                .map(appointmentMapper::entityToAppointmentResponseDto);
    }

    public AppointmentResponseDto getAppointmentById(Long id){
        var clinicId = currentUserService.getCurrentClinicId();
        var appointment = findAppointmentOrThrow(id, clinicId);
        return appointmentMapper.entityToAppointmentResponseDto(appointment);
    }

    @Transactional
    public AppointmentResponseDto createAppointment(AppointmentRequestDto request) {

        var clinic = clinicContextService.requireWritableClinic();
        var clinicId = clinic.getId();

        Therapist therapist = findTherapistOrThrow(request.getTherapistId(), clinicId);
        Case ptCase =  findCaseOrThrow(request.getCaseId(), clinicId);

        if (hasAppointmentConflict(clinicId, ptCase.getPatient().getId(), request.getScheduledAt(), therapist.getId())) {
            throw new AppointmentAtTimeExistsException(request.getScheduledAt());
        }

        Appointment appointment = appointmentMapper.requestToAppointmentEntityDto(request);

        appointment.setStatus(Appointment.Status.SCHEDULED);
        appointment.setTherapist(therapist);
        appointment.setPtCase(ptCase);
        appointment.setCreatedAt(Instant.now());
        appointment.setClinic(clinic);

        var newAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.entityToAppointmentResponseDto(newAppointment);
    }

    @Transactional
    public List<AppointmentUiDto> getAppointmentsByDate(LocalDate date) {
        var clinicId = currentUserService.getCurrentClinicId();

        var appointments = appointmentRepository.findDailyAppointments(date, clinicId);
        return appointments.
                stream()
                .map(appointmentMapper::toAppointmentUiDto)
                .toList();
    }

    @Transactional
    public AppointmentResponseDto updateAppointmentById(Long id, AppointmentPatchDto request) {
        clinicContextService.assertWritableClinic();
        var clinicId = currentUserService.getCurrentClinicId();

        var appointment = findAppointmentOrThrow(id, clinicId);

        if (request == null) return appointmentMapper.entityToAppointmentResponseDto(appointment);

        applySchedulingChangesAndValidateAppointmentConflict(appointment, request, clinicId);

        if (request.getCaseId() != null) {
            var patchCase = findCaseOrThrow(request.getCaseId(), clinicId);
            appointment.setPtCase(patchCase);
        }

        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }

        if (request.getType() != null) {
            appointment.setType(request.getType());
        }

        appointment.setModifiedAt(Instant.now());

        var saved =  appointmentRepository.save(appointment);

        return appointmentMapper.entityToAppointmentResponseDto(saved);
    }

    public void deleteAppointmentById(Long id){
        clinicContextService.assertWritableClinic();
        var clinicId = currentUserService.getCurrentClinicId();
        var appointment = findAppointmentOrThrow(id, clinicId);

        appointmentRepository.delete(appointment);
    }

    // helper functions
    private void applySchedulingChangesAndValidateAppointmentConflict(
            Appointment appointment,
            AppointmentPatchDto request,
            Long clinicId
    ) {
        var therapist = appointment.getTherapist();
        var scheduledAt = appointment.getScheduledAt();
        var ptCase = appointment.getPtCase();

        if (request.getTherapistId() != null) {
            therapist = findTherapistOrThrow(request.getTherapistId(), clinicId);
        }

        if (request.getScheduledAt() != null) {
            scheduledAt = request.getScheduledAt();
        }

        if (request.getCaseId() != null) {
            ptCase = findCaseOrThrow(request.getCaseId(), clinicId);
        }

        if (request.getTherapistId() != null || request.getScheduledAt() != null || request.getCaseId() != null) {
            if (hasAppointmentConflictExcludingCurrent(
                    clinicId,
                    ptCase.getPatient().getId(),
                    scheduledAt,
                    therapist.getId(),
                    appointment.getId()
            )) {
                throw new AppointmentAtTimeExistsException(scheduledAt);
            }
        }

        appointment.setTherapist(therapist);
        appointment.setScheduledAt(scheduledAt);
    }

    private Appointment findAppointmentOrThrow(Long id, Long clinicId) {
        return appointmentRepository.findByIdAndClinicId(id, clinicId).orElseThrow(
                () -> new AppointmentNotFoundException("Appointment not found for this clinic")
        );
    }

    private Therapist findTherapistOrThrow(Long therapistId, Long clinicId) {
        return therapistRepository.findByIdAndClinicId(therapistId, clinicId).orElseThrow(
                () -> new TherapistNotFoundException(therapistId)
        );
    }

    private Case findCaseOrThrow(Long caseId, Long clinicId) {
        return caseRepository.findByIdAndClinicId(caseId, clinicId).orElseThrow(
                () -> new CaseNotFoundException(caseId)
        );
    }

    private boolean hasAppointmentConflict(Long clinicId, Long patientId, LocalDateTime scheduledAt, Long therapistId) {
        var apptCheck = appointmentRepository
                .findByScheduledAtAndClinicIdAndTherapistId(
                        scheduledAt,
                        clinicId,
                        therapistId
                );
        if (apptCheck.isPresent()) {
            return true;
        }
        return appointmentRepository.existsPatientAppointmentConflict(clinicId, patientId, scheduledAt);
    }

    private boolean hasAppointmentConflictExcludingCurrent(
            Long clinicId,
            Long patientId,
            LocalDateTime scheduledAt,
            Long therapistId,
            Long appointmentId
    ) {
        var apptCheck = appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(
                scheduledAt,
                clinicId,
                therapistId
        );

        if (apptCheck.isPresent() && !apptCheck.get().getId().equals(appointmentId)) {
            return true;
        }

        return appointmentRepository.existsPatientAppointmentConflictExcludingAppointment(
                clinicId,
                patientId,
                scheduledAt,
                appointmentId
        );
    }
}
