package com.clinicflow.clinic_flow.services;

import com.clinicflow.clinic_flow.dtos.appointments.AppointmentPatchDto;
import com.clinicflow.clinic_flow.dtos.appointments.AppointmentRequestDto;
import com.clinicflow.clinic_flow.dtos.appointments.AppointmentResponseDto;
import com.clinicflow.clinic_flow.dtos.appointments.AppointmentUiDto;
import com.clinicflow.clinic_flow.entity.Appointment;
import com.clinicflow.clinic_flow.entity.Case;
import com.clinicflow.clinic_flow.entity.Therapist;
import com.clinicflow.clinic_flow.mappers.AppointmentMapper;
import com.clinicflow.clinic_flow.repositories.AppointmentRepository;
import com.clinicflow.clinic_flow.repositories.CaseRepository;
import com.clinicflow.clinic_flow.repositories.TherapistRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final TherapistRepository therapistRepository;
    private final CaseRepository caseRepository;

    public List<AppointmentResponseDto> getAllAppointments(){
        return appointmentRepository
                .findAll()
                .stream()
                .map(appointmentMapper::entityToAppointmentResponseDto)
                .toList();
    }

    public AppointmentResponseDto getAppointmentById(Long id){
        var appointment = appointmentRepository.findById(id).orElseThrow();
        return appointmentMapper.entityToAppointmentResponseDto(appointment);
    }

    @Transactional
    public AppointmentResponseDto createAppointment(AppointmentRequestDto request) {

        Appointment appointment = appointmentMapper.requestToAppointmentEntityDto(request);
        Therapist therapist = therapistRepository.findById(request.getTherapistId()).orElseThrow();
        Case ptCase =  caseRepository.findById(request.getCaseId()).orElseThrow();

        appointment.setStatus(Appointment.Status.SCHEDULED);
        appointment.setTherapist(therapist);
        appointment.setPtCase(ptCase);
        appointment.setCreatedAt(Instant.now());

        System.out.println("REQ scheduledAt = " + request.getScheduledAt());
        System.out.println("REQ therapistId = " + request.getTherapistId());
        System.out.println("ENTITY scheduledAt = " + appointment.getScheduledAt());

        var newAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.entityToAppointmentResponseDto(newAppointment);
    }

    @Transactional
    public List<AppointmentUiDto> getAppointmentsByDate(LocalDate date){
        var appointments = appointmentRepository.findDailyAppointments(date);
        appointments.forEach(appointment -> {
            System.out.println("firstName = " + appointment.getFirstName()
            + ", lastName = " + appointment.getLastName());
        });
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
                    new RuntimeException("Therapist not found with id " + request.getTherapistId()));
            appointment.setTherapist(therapist);
        }

        if (request.getCaseId() != null) {
            var patchCase = caseRepository.findById(request.getCaseId()).orElseThrow( () ->
                    new RuntimeException("Case not found with id " + request.getCaseId()));
            appointment.setPtCase(patchCase);
        }

        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }

        appointment.setModifiedAt(Instant.now());

        return appointmentMapper.entityToAppointmentResponseDto(appointmentRepository.save(appointment));
    }

    public void deleteAppointmentById(Long id){
        appointmentRepository.deleteById(id);
    }
}
