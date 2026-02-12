package com.clinicflow.clinic_flow.controllers;

import com.clinicflow.clinic_flow.dtos.AppointmentDto;
import com.clinicflow.clinic_flow.dtos.AppointmentUiDto;
import com.clinicflow.clinic_flow.entity.Appointment;
import com.clinicflow.clinic_flow.mappers.AppointmentMapper;
import com.clinicflow.clinic_flow.repositories.AppointmentRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@AllArgsConstructor
@RequestMapping("/appoinments")
@RestController
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    // TODO: ADD response entity response to this method
    @GetMapping
    List<AppointmentUiDto> getAllAppointments(
            @RequestParam(required = false, defaultValue = "", name = "sort") String sort
            ) {
        if (sort == null || sort.isEmpty())
            sort = "scheduledAt";
        return appointmentRepository.findAll(Sort.by(sort))
                .stream()
                .map(appointmentMapper::toAppointmentDto)
                .toList();
    }
    @Transactional
    @GetMapping("/{date}")
    public ResponseEntity<List<AppointmentUiDto>> getAppointmentsByDate (@PathVariable LocalDate date){
        var appts = appointmentRepository.findDailyAppointments(date);
        if (appts.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(appts
                .stream().map(appointmentMapper::toAppointmentUiDto)
                .toList()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<AppointmentDto> createAppointment(
            @RequestBody AppointmentDto newAppointmentDto) {

        AppointmentDto newAppointmentRequest = appointmentRepository.save(newAppointmentDto);
        return ResponseEntity.created()
    }

}
