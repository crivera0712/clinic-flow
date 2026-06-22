package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.appointment.dtos.AppointmentPatchDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentRequestDto;
import com.clinicflow.clinic_flow.appointment.dtos.BoardRowDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
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

    @GetMapping("/date")
    public ResponseEntity<List<BoardRowDto>> getAppointmentsByDate(
            @RequestParam(required = false) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(appointmentService.getAppointmentsByDate(date));
    }

    @PostMapping()
    public ResponseEntity<BoardRowDto> createAppointment(@RequestBody @Valid AppointmentRequestDto request) {
        var created = appointmentService.createAppointment(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BoardRowDto> updateAppointment(
            @PathVariable @NotNull Long id, @RequestBody AppointmentPatchDto request) {
        return ResponseEntity.ok(appointmentService.updateAppointmentById(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointmentById(@PathVariable @NotNull Long id) {
        appointmentService.deleteAppointmentById(id);
        return ResponseEntity.noContent().build();
    }
}
