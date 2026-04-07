package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.auth.JwtService;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.exception.DemoClinicReadOnlyException;
import com.clinicflow.clinic_flow.exception.GlobalExceptionHandler;
import com.clinicflow.clinic_flow.exception.TherapistNotFoundException;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TherapistController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TherapistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TherapistService therapistService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthSessionService authSessionService;

    @Test
    void shouldReturnTherapists_whenSearchParamIsMissing() throws Exception {
        var page = new PageImpl<>(List.of(
                new TherapistsResponseDto(1L, "Taylor", "Physical Therapist")
        ));
        when(therapistService.getTherapists(eq(null), any(Pageable.class))).thenReturn(page);

        var response = mockMvc.perform(get("/api/therapists")
                .param("page", "0")
                .param("size", "10"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].therapistId").value(1L))
                .andExpect(jsonPath("$.content[0].therapistName").value("Taylor"))
                .andExpect(jsonPath("$.content[0].type").value("Physical Therapist"));
        verify(therapistService).getTherapists(eq(null), any(Pageable.class));
    }

    @Test
    void shouldReturnTherapists_whenSearchParamIsBlank() throws Exception {
        var page = new PageImpl<>(List.of(
                new TherapistsResponseDto(1L, "Taylor", "Physical Therapist")
        ));
        when(therapistService.getTherapists(eq("   "), any(Pageable.class))).thenReturn(page);

        var response = mockMvc.perform(get("/api/therapists")
                .param("search", "   ")
                .param("page", "0")
                .param("size", "10"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].therapistId").value(1L));
        verify(therapistService).getTherapists(eq("   "), any(Pageable.class));
    }

    @Test
    void shouldReturnTherapists_whenSearchParamIsProvided() throws Exception {
        var page = new PageImpl<>(List.of(
                new TherapistsResponseDto(1L, "Sam Taylor", "Physical Therapist")
        ));
        when(therapistService.getTherapists(eq("sam"), any(Pageable.class))).thenReturn(page);

        var response = mockMvc.perform(get("/api/therapists")
                .param("search", "sam")
                .param("page", "0")
                .param("size", "10"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].therapistName").value("Sam Taylor"));
        verify(therapistService).getTherapists(eq("sam"), any(Pageable.class));
    }

    @Test
    void shouldReturnTherapist_whenGetTherapistByIdFindsMatch() throws Exception {
        when(therapistService.getTherapistById(5L))
                .thenReturn(new TherapistsResponseDto(5L, "Taylor", "Physical Therapist"));

        var response = mockMvc.perform(get("/api/therapists/5"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.therapistId").value(5L))
                .andExpect(jsonPath("$.therapistName").value("Taylor"));
    }

    @Test
    void shouldReturnNotFound_whenGetTherapistByIdDoesNotFindMatch() throws Exception {
        when(therapistService.getTherapistById(5L)).thenThrow(new TherapistNotFoundException(5L));

        var response = mockMvc.perform(get("/api/therapists/5"));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Therapist with id 5 not found"));
    }

    @Test
    void shouldCreateTherapist_whenPostReceivesValidRequest() throws Exception {
        String json = """
                {
                  "therapistName": "Taylor",
                  "type": "PHYSICAL_THERAPIST"
                }
                """;
        when(therapistService.createTherapist(any()))
                .thenReturn(new TherapistsResponseDto(12L, "Taylor", "Physical Therapist"));

        var response = mockMvc.perform(post("/api/therapists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        response.andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/therapists/12")))
                .andExpect(jsonPath("$.therapistId").value(12L));
    }

    @Test
    void shouldReturnForbidden_whenPostTherapistRunsInDemoClinic() throws Exception {
        String json = """
                {
                  "therapistName": "Taylor",
                  "type": "PHYSICAL_THERAPIST"
                }
                """;
        when(therapistService.createTherapist(any())).thenThrow(new DemoClinicReadOnlyException());

        mockMvc.perform(post("/api/therapists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Demo clinic is read-only"));
    }

    @Test
    void shouldReturnBadRequest_whenPostTherapistMissingName() throws Exception {
        String json = """
                {
                  "type": "PHYSICAL_THERAPIST"
                }
                """;

        var response = mockMvc.perform(post("/api/therapists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldPatchTherapist_whenPatchReceivesValidRequest() throws Exception {
        String json = objectMapper.writeValueAsString(new PatchRequest("Jordan", "OCCUPATIONAL_THERAPIST"));
        when(therapistService.updateTherapist(eq(9L), any()))
                .thenReturn(new TherapistsResponseDto(9L, "Jordan", "Occupational Therapist"));

        var response = mockMvc.perform(patch("/api/therapists/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.therapistId").value(9L))
                .andExpect(jsonPath("$.therapistName").value("Jordan"));
        verify(therapistService).updateTherapist(eq(9L), any());
    }

    private record PatchRequest(String therapistName, String therapistType) {
    }
}
