package com.clinicflow.clinic_flow.controllers;

import com.clinicflow.clinic_flow.dtos.appointments.AppointmentPatchDto;
import com.clinicflow.clinic_flow.dtos.appointments.AppointmentRequestDto;
import com.clinicflow.clinic_flow.dtos.appointments.AppointmentResponseDto;
import com.clinicflow.clinic_flow.dtos.appointments.AppointmentUiDto;
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

    private final AppointmentService appointmentService;

    @GetMapping()
    public List<AppointmentResponseDto> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    public AppointmentResponseDto getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }

    @GetMapping("/date")
    public ResponseEntity<List<AppointmentUiDto>> getAppointmentsByDate (
            @RequestParam(required = false) LocalDate date){
        if (date == null) date =  LocalDate.now();
        var appointments = appointmentService.getAppointmentsByDate(date);
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/create")
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @RequestBody @Valid AppointmentRequestDto request) {

        var createdAppointmentDto = appointmentService.createAppointment(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAppointmentDto.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdAppointmentDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> updateAppointment(
            @PathVariable Long id, @RequestBody AppointmentPatchDto request) {
        var patchAppointment = appointmentService.updateAppointmentById(id,  request);
        return ResponseEntity.ok(patchAppointment);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteAppointmentById(@PathVariable Long id) {
        appointmentService.deleteAppointmentById(id);
        return ResponseEntity.ok().build();
    }

}
