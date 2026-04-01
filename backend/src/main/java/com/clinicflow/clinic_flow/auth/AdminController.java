package com.clinicflow.clinic_flow.auth;

import com.clinicflow.clinic_flow.appointment.AppointmentService;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentResponseDto;
import com.clinicflow.clinic_flow.common.PageResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AppointmentService appointmentService;

    @GetMapping()
    public ResponseEntity<PageResponse<AppointmentResponseDto>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentResponseDto> result = appointmentService.getAllAppointments(pageable);

        return ResponseEntity.ok(PageResponse.from(result));
    }

}
