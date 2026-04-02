package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.appointment.dtos.*;
import com.clinicflow.clinic_flow.cases.Case;
import com.clinicflow.clinic_flow.therapist.Therapist;
import com.clinicflow.clinic_flow.exception.AppointmentAtTimeExistsException;
import com.clinicflow.clinic_flow.exception.CaseNotFoundException;
import com.clinicflow.clinic_flow.exception.TherapistNotFoundException;
import com.clinicflow.clinic_flow.cases.CaseRepository;
import com.clinicflow.clinic_flow.therapist.TherapistRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
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

    public Page<AppointmentResponseDto> getAllAppointments(Pageable pageable, LocalDate date) {
        Page<Appointment> result;
        if (date == null) {
            result = appointmentRepository.findAll(pageable);
        } else {
            result = appointmentRepository.findByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                    date.atStartOfDay(),
                    date.plusDays(1).atStartOfDay(),
                    pageable
            );
        }
        return result
                .map(appointmentMapper::entityToAppointmentResponseDto);
    }

    public AppointmentResponseDto getAppointmentById(Long id){
        var appointment = appointmentRepository.findById(id).orElseThrow();
        return appointmentMapper.entityToAppointmentResponseDto(appointment);
    }

    @Transactional
    public AppointmentResponseDto createAppointment(AppointmentRequestDto request) {

        Optional<Appointment> apptCheck = appointmentRepository.findByScheduledAt(request.getScheduledAt());

        if (apptCheck.isPresent()) {
            throw new AppointmentAtTimeExistsException(request.getScheduledAt());
        }

        Appointment appointment = appointmentMapper.requestToAppointmentEntityDto(request);
        Therapist therapist = therapistRepository.findById(request.getTherapistId()).orElseThrow(() ->
                new TherapistNotFoundException(request.getTherapistId()));
        Case ptCase =  caseRepository.findById(request.getCaseId()).orElseThrow(() ->
                new CaseNotFoundException(request.getCaseId()));

        appointment.setStatus(Appointment.Status.SCHEDULED);
        appointment.setTherapist(therapist);
        appointment.setPtCase(ptCase);
        appointment.setCreatedAt(Instant.now());

        var newAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.entityToAppointmentResponseDto(newAppointment);
    }

    @Transactional
    public List<AppointmentUiDto> getAppointmentsByDate(LocalDate date){
        var appointments = appointmentRepository.findDailyAppointments(date);
        return appointments.
                stream()
                .map(appointmentMapper::toAppointmentUiDto)
                .toList();
    }

    @Transactional
    public AppointmentResponseDto updateAppointmentById(Long id, AppointmentPatchDto request) {
        var appointment = appointmentRepository.findById(id).orElseThrow( () ->
                new RuntimeException("Appointment not found with id " + id));

        if (request == null) return appointmentMapper.entityToAppointmentResponseDto(appointment);

        if (request.getScheduledAt() != null) {
            appointment.setScheduledAt(request.getScheduledAt());
        }

        if (request.getTherapistId() != null) {
            var therapist = therapistRepository.findById(request.getTherapistId()).orElseThrow( ()->
                    new TherapistNotFoundException(request.getTherapistId()));
            appointment.setTherapist(therapist);
        }

        if (request.getCaseId() != null) {
            var patchCase = caseRepository.findById(request.getCaseId()).orElseThrow( () ->
                    new CaseNotFoundException(request.getCaseId()));
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

        simpMessagingTemplate.convertAndSend("/topic/display-board." + saved.getScheduledAt().toLocalDate(),
        new DisplayBoardUpdateMessage("APPOINTMENT_UPDATED", saved.getScheduledAt().toLocalDate()));

        return appointmentMapper.entityToAppointmentResponseDto(saved);
    }

    public void deleteAppointmentById(Long id){
        var appointment = appointmentRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Appointment not found with id " + id));
        appointmentRepository.delete(appointment);
    }
}
