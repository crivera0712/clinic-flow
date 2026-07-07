package com.clinicflow.clinic_flow.therapist;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinicflow.clinic_flow.auth.JwtService;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.exception.DemoClinicReadOnlyException;
import com.clinicflow.clinic_flow.exception.GlobalExceptionHandler;
import com.clinicflow.clinic_flow.exception.TherapistNotFoundException;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TherapistController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TherapistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TherapistService therapistService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthSessionService authSessionService;

    @Test
    void getTherapists_returnsArray() throws Exception {
        when(therapistService.getTherapists()).thenReturn(List.of(new TherapistsResponseDto(1L, "Taylor")));

        mockMvc.perform(get("/api/therapists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Taylor"));

        verify(therapistService).getTherapists();
    }

    @Test
    void getTherapistById_returnsTherapist() throws Exception {
        when(therapistService.getTherapistById(5L)).thenReturn(new TherapistsResponseDto(5L, "Taylor"));

        mockMvc.perform(get("/api/therapists/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.name").value("Taylor"));
    }

    @Test
    void getTherapistById_whenMissing_returnsNotFound() throws Exception {
        when(therapistService.getTherapistById(5L)).thenThrow(new TherapistNotFoundException(5L));

        mockMvc.perform(get("/api/therapists/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Therapist with id 5 not found"));
    }

    @Test
    void createTherapist_returnsCreated() throws Exception {
        String json = """
                { "name": "Taylor" }
                """;
        when(therapistService.createTherapist(any())).thenReturn(new TherapistsResponseDto(12L, "Taylor"));

        mockMvc.perform(post("/api/therapists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/therapists/12")))
                .andExpect(jsonPath("$.id").value(12L))
                .andExpect(jsonPath("$.name").value("Taylor"));
    }

    @Test
    void createTherapist_inDemoClinic_returnsForbidden() throws Exception {
        when(therapistService.createTherapist(any())).thenThrow(new DemoClinicReadOnlyException());

        mockMvc.perform(post("/api/therapists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Taylor\" }"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTherapist_missingName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/therapists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
}
