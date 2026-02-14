package com.clinicflow.clinic_flow.services;

import com.clinicflow.clinic_flow.dtos.AppointmentRequest;
import com.clinicflow.clinic_flow.dtos.AppointmentResponse;
import com.clinicflow.clinic_flow.dtos.AppointmentUiDto;
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
    private AppointmentRepository appointmentRepository;
    private AppointmentMapper appointmentMapper;
    private TherapistRepository therapistRepository;
    private CaseRepository caseRepository;

    public List<AppointmentResponse> getAllAppointments(){
        return appointmentRepository
                .findAll()
                .stream()
                .map(appointmentMapper::entityToAppointmentResponse)
                .toList();
    }

    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {

        Appointment newAppointmentRequest = appointmentMapper.requestToAppointmentEntity(request);
        Therapist therapist = therapistRepository.findById(request.getTherapistId()).orElseThrow();
        Case ptCase =  caseRepository.findById(request.getCaseId()).orElseThrow();

        newAppointmentRequest.setTherapist(therapist);
        newAppointmentRequest.setPtCase(ptCase);
        newAppointmentRequest.setCreatedAt(Instant.now());

        var newAppointment = appointmentRepository.save(newAppointmentRequest);
        return appointmentMapper.entityToAppointmentResponse(newAppointment);
    }

    @Transactional
    public List<AppointmentUiDto> getAppointmentsByDate(LocalDate date){
        var appts = appointmentRepository.findDailyAppointments(date);
        return appts.
                stream()
                .map(appointmentMapper::toAppointmentUiDto)
                .toList();
    }
}
