package com.clinicflow.clinic_flow.services;

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
        return appointments.
                stream()
                .map(appointmentMapper::toAppointmentUiDto)
                .toList();
    }
}
