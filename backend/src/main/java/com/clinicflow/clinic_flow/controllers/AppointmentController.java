package com.clinicflow.clinic_flow.controllers;

import com.clinicflow.clinic_flow.dtos.AppointmentRequest;
import com.clinicflow.clinic_flow.dtos.AppointmentResponse;
import com.clinicflow.clinic_flow.dtos.AppointmentUiDto;
import com.clinicflow.clinic_flow.mappers.AppointmentMapper;
import com.clinicflow.clinic_flow.repositories.AppointmentRepository;
import com.clinicflow.clinic_flow.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;


@AllArgsConstructor
@RequestMapping("/appointments")
@RestController
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentService appointmentService;

    // TODO: ADD response entity response to this method
    @GetMapping()
    List<AppointmentResponse> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    public AppointmentResponse getAppointmentById(@PathVariable Long id) {
        var appointment = appointmentRepository.findById(id).orElseThrow();
        return appointmentMapper.entityToAppointmentResponse(appointment);
    }

    @GetMapping("/date")
    public ResponseEntity<List<AppointmentUiDto>> getAppointmentsByDate (
            @RequestParam(required = false) LocalDate date){
        if (date == null) date =  LocalDate.now();
        var appts = appointmentService.getAppointmentsByDate(date);
        return ResponseEntity.ok(appts);
    }

    @PostMapping("/create")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @RequestBody @Valid AppointmentRequest request) {

        var createdAppointmentDto = appointmentService.createAppointment(request);

        // should i check params?
        System.out.println("Debug statements to test create Post endpoint for appointments**");
        System.out.println(request.toString());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAppointmentDto.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdAppointmentDto);
    }

}
