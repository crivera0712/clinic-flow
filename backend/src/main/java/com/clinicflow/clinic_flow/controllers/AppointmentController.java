package com.clinicflow.clinic_flow.controllers;

import com.clinicflow.clinic_flow.dtos.AppointmentDto;
import com.clinicflow.clinic_flow.mappers.AppointmentMapper;
import com.clinicflow.clinic_flow.repositories.AppointmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@AllArgsConstructor
@RequestMapping("/appts")
@RestController
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    @GetMapping
    List<AppointmentDto> getAllAppointments(
            @RequestParam(required = false, defaultValue = "", name = "sort") String sort
            ) {
        if (sort == null || sort.isEmpty())
            sort = "scheduledAt";
        return appointmentRepository.findAll(Sort.by(sort))
                .stream()
                .map(appointmentMapper::toAppointmentDto)
                .toList();
    }
}
