package com.clinicflow.clinic_flow.appointment;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.clinicflow.clinic_flow.appointment.dtos.BoardRowDto;
import com.clinicflow.clinic_flow.auth.JwtService;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.exception.AppointmentAtTimeExistsException;
import com.clinicflow.clinic_flow.exception.AppointmentNotFoundException;
import com.clinicflow.clinic_flow.exception.DemoClinicReadOnlyException;
import com.clinicflow.clinic_flow.exception.GlobalExceptionHandler;
import com.clinicflow.clinic_flow.patient.PatientService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthSessionService authSessionService;

    private BoardRowDto boardRow(Long id) {
        return new BoardRowDto(
                id,
                LocalDateTime.of(2026, 2, 13, 9, 0),
                Appointment.Type.EVALUATION,
                Appointment.Status.SCHEDULED,
                7L,
                "Jane Doe",
                3L,
                "Dr. Smith");
    }

    @Test
    void getAppointmentsByDate_returnsBoardRows() throws Exception {
        LocalDate date = LocalDate.of(2026, 2, 13);
        when(appointmentService.getAppointmentsByDate(date)).thenReturn(List.of(boardRow(1L)));

        mockMvc.perform(get("/api/appointments/date").param("date", "2026-02-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].scheduledAt").value("2026-02-13T09:00:00"))
                .andExpect(jsonPath("$[0].type").value("EVALUATION"))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$[0].patientId").value(7L))
                .andExpect(jsonPath("$[0].patientName").value("Jane Doe"))
                .andExpect(jsonPath("$[0].therapistId").value(3L))
                .andExpect(jsonPath("$[0].therapistName").value("Dr. Smith"));

        verify(appointmentService).getAppointmentsByDate(date);
    }

    @Test
    void getAppointmentsByDate_defaultsToToday_whenDateOmitted() throws Exception {
        when(appointmentService.getAppointmentsByDate(any(LocalDate.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/appointments/date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(appointmentService).getAppointmentsByDate(any(LocalDate.class));
    }

    @Test
    void createAppointment_withExistingPatient_returnsCreated() throws Exception {
        String json =
                """
                { "scheduledAt": "2026-02-13T09:00:00", "type": "EVALUATION", "therapistId": 3, "patientId": 7 }
                """;
        when(appointmentService.createAppointment(any())).thenReturn(boardRow(12L));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/appointments/12")))
                .andExpect(jsonPath("$.id").value(12L))
                .andExpect(jsonPath("$.patientName").value("Jane Doe"));

        verify(appointmentService).createAppointment(any());
    }

    @Test
    void createAppointment_withInlineNewPatient_returnsCreated() throws Exception {
        String json =
                """
                { "scheduledAt": "2026-02-13T09:00:00", "type": "EVALUATION", "therapistId": 3,
                  "patient": { "firstName": "New", "lastName": "Patient" } }
                """;
        when(appointmentService.createAppointment(any())).thenReturn(boardRow(13L));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(13L));
    }

    @Test
    void createAppointment_inDemoClinic_returnsForbidden() throws Exception {
        String json =
                """
                { "scheduledAt": "2026-02-13T09:00:00", "type": "EVALUATION", "therapistId": 3, "patientId": 7 }
                """;
        when(appointmentService.createAppointment(any())).thenThrow(new DemoClinicReadOnlyException());

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAppointment_withInvalidInput_returnsBadRequest() throws Exception {
        String invalidJson =
                """
                { "scheduledAt": null, "type": null, "therapistId": null }
                """;

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void createAppointment_onConflict_returnsConflict() throws Exception {
        String json =
                """
                { "scheduledAt": "2026-02-13T09:00:00", "type": "EVALUATION", "therapistId": 3, "patientId": 7 }
                """;
        when(appointmentService.createAppointment(any()))
                .thenThrow(new AppointmentAtTimeExistsException(LocalDateTime.of(2026, 2, 13, 9, 0)));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void updateAppointment_returnsUpdatedRow() throws Exception {
        String json = """
                { "status": "WAITING" }
                """;
        BoardRowDto updated = new BoardRowDto(
                9L,
                LocalDateTime.of(2026, 2, 13, 9, 0),
                Appointment.Type.EVALUATION,
                Appointment.Status.WAITING,
                7L,
                "Jane Doe",
                3L,
                "Dr. Smith");
        when(appointmentService.updateAppointmentById(any(Long.class), any())).thenReturn(updated);

        mockMvc.perform(patch("/api/appointments/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9L))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(appointmentService).updateAppointmentById(any(Long.class), any());
    }

    @Test
    void updateAppointment_whenNotFound_returnsNotFound() throws Exception {
        when(appointmentService.updateAppointmentById(any(Long.class), any()))
                .thenThrow(new AppointmentNotFoundException("Appointment not found for this clinic"));

        mockMvc.perform(patch("/api/appointments/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAppointment_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/appointments/9")).andExpect(status().isNoContent());
        verify(appointmentService).deleteAppointmentById(9L);
    }
}
