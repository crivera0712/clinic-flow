package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.appointment.dtos.AppointmentPatchDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentRequestDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentResponseDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentUiDto;
import com.clinicflow.clinic_flow.common.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Validated
@AllArgsConstructor
@RequestMapping("/api/appointments")
@RestController
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping()
    public ResponseEntity<PageResponse<AppointmentResponseDto>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Long caseId) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentResponseDto> result = appointmentService.getAllAppointments(pageable, date, caseId);

        return ResponseEntity.ok(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    public AppointmentResponseDto getAppointmentById(@PathVariable @NotNull Long id) {
        return appointmentService.getAppointmentById(id);
    }

    @GetMapping("/date")
    public ResponseEntity<List<AppointmentUiDto>> getAppointmentsByDate (
            @RequestParam(required = false) LocalDate date
    ) {

        if (date == null) date =  LocalDate.now();
        var appointments = appointmentService.getAppointmentsByDate(date);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/therapist/{therapistId}")
    public ResponseEntity<PageResponse<AppointmentResponseDto>> getAllAppointmentsByTherapistId(
            @PathVariable @NotNull Long therapistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) LocalDate date) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentResponseDto> result = appointmentService.getAllAppointmentsByTherapist(pageable, therapistId, date);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    @PostMapping()
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
            @PathVariable @NotNull Long id, @RequestBody AppointmentPatchDto request) {
        var patchAppointment = appointmentService.updateAppointmentById(id,  request);
        return ResponseEntity.ok(patchAppointment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointmentById(@PathVariable @NotNull Long id) {
        appointmentService.deleteAppointmentById(id);
        return ResponseEntity.noContent().build();
    }

}
