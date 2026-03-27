package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.auth.JwtService;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentResponseDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentUiDto;
import com.clinicflow.clinic_flow.exception.AppointmentAtTimeExistsException;
import com.clinicflow.clinic_flow.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthSessionService authSessionService;

    @Test
    void shouldReturnAppointments_whenGetAllAppointmentsIsCalled() throws Exception {
        // Arrange
        when(appointmentService.getAllAppointments()).thenReturn(List.of(responseDto(1L), responseDto(2L)));

        // Act
        var response = mockMvc.perform(get("/api/appointments"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].scheduledAt").value("2026-02-13 09:00"))
                .andExpect(jsonPath("$[0].caseId").value(4L))
                .andExpect(jsonPath("$[0].therapistId").value(3L))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$[1].id").value(2L));
        verify(appointmentService).getAllAppointments();
    }

    @Test
    void shouldReturnAppointment_whenGetAppointmentByIdFindsAppointment() throws Exception {
        // Arrange
        when(appointmentService.getAppointmentById(5L)).thenReturn(responseDto(5L));

        // Act
        var response = mockMvc.perform(get("/api/appointments/5"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.scheduledAt").value("2026-02-13 09:00"))
                .andExpect(jsonPath("$.caseId").value(4L))
                .andExpect(jsonPath("$.therapistId").value(3L))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
        verify(appointmentService).getAppointmentById(5L);
    }

    @Test
    void shouldReturnAppointmentsForDate_whenGetAppointmentsByDateReceivesDateParam() throws Exception {
        // Arrange
        LocalDate date = LocalDate.of(2026, 2, 13);
        AppointmentUiDto dto = new AppointmentUiDto(
                21L,
                LocalDateTime.of(2026, 2, 13, 9, 0),
                31L,
                "Sam",
                "Lee",
                41L,
                "Taylor",
                "PHYSICAL_THERAPIST",
                "Shoulder",
                Appointment.Status.SCHEDULED
        );
        when(appointmentService.getAppointmentsByDate(date)).thenReturn(List.of(dto));

        // Act
        var response = mockMvc.perform(get("/api/appointments/date").param("date", "2026-02-13"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].aptId").value(21L))
                .andExpect(jsonPath("$[0].scheduledAt").value("2026-02-13 09:00"))
                .andExpect(jsonPath("$[0].caseId").value(31L))
                .andExpect(jsonPath("$[0].firstName").value("Sam"))
                .andExpect(jsonPath("$[0].lastName").value("Lee"))
                .andExpect(jsonPath("$[0].therapistId").value(41L))
                .andExpect(jsonPath("$[0].therapistName").value("Taylor"))
                .andExpect(jsonPath("$[0].therapistType").value("PHYSICAL_THERAPIST"))
                .andExpect(jsonPath("$[0].bodyRegionDisplayName").value("Shoulder"))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
        verify(appointmentService).getAppointmentsByDate(date);
    }

    @Test
    void shouldReturnAppointmentsForCurrentDate_whenGetAppointmentsByDateOmitsDateParam() throws Exception {
        // Arrange
        when(appointmentService.getAppointmentsByDate(any(LocalDate.class))).thenReturn(List.of());

        // Act
        var response = mockMvc.perform(get("/api/appointments/date"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(appointmentService).getAppointmentsByDate(any(LocalDate.class));
    }

    @Test
    void shouldCreateAppointment_whenPostAppointmentReceivesValidRequest() throws Exception {
        // Arrange
        String json = """
                {
                  "scheduledAt": "2026-02-13T09:00:00",
                  "therapistId": 3,
                  "caseId": 4
                }
                """;
        AppointmentResponseDto responseDto = responseDto(12L);
        when(appointmentService.createAppointment(any())).thenReturn(responseDto);

        // Act
        var response = mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/appointments/12")))
                .andExpect(jsonPath("$.id").value(12L))
                .andExpect(jsonPath("$.scheduledAt").value("2026-02-13 09:00"))
                .andExpect(jsonPath("$.caseId").value(4L))
                .andExpect(jsonPath("$.therapistId").value(3L))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
        verify(appointmentService).createAppointment(any());
    }

    @Test
    void shouldReturnBadRequest_whenPostAppointmentReceivesMalformedJson() throws Exception {
        // Arrange
        String malformedJson = """
                {
                  "scheduledAt": "2026-02-13T09:00:00",
                  "therapistId":
                }
                """;

        // Act
        var response = mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson));

        // Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenPostAppointmentReceivesInvalidInput() throws Exception {
        // Arrange
        String invalidJson = """
                {
                  "scheduledAt": null,
                  "therapistId": null,
                  "caseId": null
                }
                """;

        // Act
        var response = mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnConflict_whenPostAppointmentFindsAppointmentConflict() throws Exception {
        // Arrange
        String json = """
                {
                  "scheduledAt": "2026-02-13T09:00:00",
                  "therapistId": 3,
                  "caseId": 4
                }
                """;
        when(appointmentService.createAppointment(any()))
                .thenThrow(new AppointmentAtTimeExistsException(LocalDateTime.of(2026, 2, 13, 9, 0)));

        // Act
        var response = mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void shouldReturnUpdatedAppointment_whenPatchAppointmentReceivesValidRequest() throws Exception {
        // Arrange
        String json = objectMapper.writeValueAsString(new PatchRequest(
                "2026-02-14T11:30:00",
                17L,
                16L,
                "FINISHED"
        ));
        AppointmentResponseDto responseDto = new AppointmentResponseDto(
                9L,
                LocalDateTime.of(2026, 2, 14, 11, 30),
                Instant.parse("2026-02-02T12:00:00Z"),
                17L,
                16L,
                Appointment.Status.FINISHED
        );
        when(appointmentService.updateAppointmentById(any(Long.class), any())).thenReturn(responseDto);

        // Act
        var response = mockMvc.perform(patch("/api/appointments/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9L))
                .andExpect(jsonPath("$.scheduledAt").value("2026-02-14 11:30"))
                .andExpect(jsonPath("$.caseId").value(17L))
                .andExpect(jsonPath("$.therapistId").value(16L))
                .andExpect(jsonPath("$.status").value("FINISHED"));
        verify(appointmentService).updateAppointmentById(any(Long.class), any());
    }

    @Test
    void shouldReturnInternalServerError_whenPatchAppointmentDoesNotFindAppointment() throws Exception {
        // Arrange
        String json = objectMapper.writeValueAsString(new PatchRequest(
                null,
                null,
                null,
                "CHECKED_IN"
        ));
        when(appointmentService.updateAppointmentById(any(Long.class), any()))
                .thenThrow(new RuntimeException("Appointment not found with id 9"));

        // Act
        var response = mockMvc.perform(patch("/api/appointments/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isInternalServerError());
    }

    @Test
    void shouldDeleteAppointment_whenDeleteAppointmentByIdIsCalled() throws Exception {
        // Arrange

        // Act
        var response = mockMvc.perform(delete("/api/appointments/9"));

        // Assert
        response.andExpect(status().isOk());
        verify(appointmentService).deleteAppointmentById(9L);
    }

    private AppointmentResponseDto responseDto(Long id) {
        return new AppointmentResponseDto(
                id,
                LocalDateTime.of(2026, 2, 13, 9, 0),
                Instant.parse("2026-02-01T12:00:00Z"),
                4L,
                3L,
                Appointment.Status.SCHEDULED
        );
    }

    private record PatchRequest(
            String scheduledAt,
            Long caseId,
            Long therapistId,
            String status
    ) {
    }
}
