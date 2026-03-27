package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.auth.JwtService;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.cases.dtos.CaseResponseDto;
import com.clinicflow.clinic_flow.exception.BodyRegionNotFoundException;
import com.clinicflow.clinic_flow.exception.CaseNotFoundException;
import com.clinicflow.clinic_flow.exception.GlobalExceptionHandler;
import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
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
import java.util.Date;
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

@WebMvcTest(CaseController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CaseService caseService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthSessionService authSessionService;

    @Test
    void shouldReturnCases_whenGetCasesIsCalled() throws Exception {
        // Arrange
        when(caseService.getCases()).thenReturn(List.of(
                response(1L, 10L, 20L),
                response(2L, 11L, 21L)
        ));

        // Act
        var response = mockMvc.perform(get("/api/cases"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].patientId").value(10L))
                .andExpect(jsonPath("$[0].bodyRegionId").value(20L))
                .andExpect(jsonPath("$[1].id").value(2L));
        verify(caseService).getCases();
    }

    @Test
    void shouldReturnCase_whenGetCaseByIdFindsCase() throws Exception {
        // Arrange
        when(caseService.getCase(5L)).thenReturn(response(5L, 10L, 20L));

        // Act
        var response = mockMvc.perform(get("/api/cases/5"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.patientId").value(10L))
                .andExpect(jsonPath("$.bodyRegionId").value(20L));
        verify(caseService).getCase(5L);
    }

    @Test
    void shouldReturnNotFound_whenGetCaseByIdDoesNotFindCase() throws Exception {
        // Arrange
        when(caseService.getCase(5L)).thenThrow(new CaseNotFoundException(5L));

        // Act
        var response = mockMvc.perform(get("/api/cases/5"));

        // Assert
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Could not find case by id 5"));
    }

    @Test
    void shouldReturnCases_whenGetCasesByPatientFindsCases() throws Exception {
        // Arrange
        when(caseService.searchByPatient(10L)).thenReturn(List.of(response(1L, 10L, 20L)));

        // Act
        var response = mockMvc.perform(get("/api/cases/patient/10"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].patientId").value(10L))
                .andExpect(jsonPath("$[0].bodyRegionId").value(20L));
        verify(caseService).searchByPatient(10L);
    }

    @Test
    void shouldCreateCase_whenPostCaseReceivesValidRequest() throws Exception {
        // Arrange
        String json = """
                {
                  "patientId": 10,
                  "bodyRegionId": 20
                }
                """;
        when(caseService.createCase(any())).thenReturn(response(12L, 10L, 20L));

        // Act
        var response = mockMvc.perform(post("/api/cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/cases/12")))
                .andExpect(jsonPath("$.id").value(12L))
                .andExpect(jsonPath("$.patientId").value(10L))
                .andExpect(jsonPath("$.bodyRegionId").value(20L));
        verify(caseService).createCase(any());
    }

    @Test
    void shouldReturnBadRequest_whenPostCaseReceivesInvalidInput() throws Exception {
        // Arrange
        String json = """
                {
                  "patientId": null,
                  "bodyRegionId": null
                }
                """;

        // Act
        var response = mockMvc.perform(post("/api/cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnBadRequest_whenPostCaseReceivesMalformedJson() throws Exception {
        // Arrange
        String malformedJson = """
                {
                  "patientId": 10,
                  "bodyRegionId":
                }
                """;

        // Act
        var response = mockMvc.perform(post("/api/cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnNotFound_whenPostCaseDoesNotFindPatient() throws Exception {
        // Arrange
        String json = """
                {
                  "patientId": 10,
                  "bodyRegionId": 20
                }
                """;
        when(caseService.createCase(any())).thenThrow(new PatientNotFoundException(10L));

        // Act
        var response = mockMvc.perform(post("/api/cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Patient with id 10 does not exist"));
    }

    @Test
    void shouldReturnNotFound_whenPostCaseDoesNotFindBodyRegion() throws Exception {
        // Arrange
        String json = """
                {
                  "patientId": 10,
                  "bodyRegionId": 20
                }
                """;
        when(caseService.createCase(any())).thenThrow(new BodyRegionNotFoundException(20L));

        // Act
        var response = mockMvc.perform(post("/api/cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Could not find body region with id 20"));
    }

    @Test
    void shouldReturnUpdatedCase_whenPatchCaseReceivesValidRequest() throws Exception {
        // Arrange
        String json = objectMapper.writeValueAsString(new PatchRequest(30L));
        when(caseService.updateCase(eq(9L), any())).thenReturn(response(9L, 10L, 30L));

        // Act
        var response = mockMvc.perform(patch("/api/cases/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9L))
                .andExpect(jsonPath("$.patientId").value(10L))
                .andExpect(jsonPath("$.bodyRegionId").value(30L));
        verify(caseService).updateCase(eq(9L), any());
    }

    @Test
    void shouldReturnBadRequest_whenPatchCaseReceivesInvalidInput() throws Exception {
        // Arrange
        String json = """
                {
                  "bodyRegionId": null
                }
                """;

        // Act
        var response = mockMvc.perform(patch("/api/cases/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnBadRequest_whenPatchCaseReceivesMalformedJson() throws Exception {
        // Arrange
        String malformedJson = """
                {
                  "bodyRegionId":
                }
                """;

        // Act
        var response = mockMvc.perform(patch("/api/cases/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnNotFound_whenPatchCaseDoesNotFindCase() throws Exception {
        // Arrange
        String json = objectMapper.writeValueAsString(new PatchRequest(30L));
        when(caseService.updateCase(eq(9L), any())).thenThrow(new CaseNotFoundException(9L));

        // Act
        var response = mockMvc.perform(patch("/api/cases/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Could not find case by id 9"));
    }

    @Test
    void shouldReturnNotFound_whenPatchCaseDoesNotFindBodyRegion() throws Exception {
        // Arrange
        String json = objectMapper.writeValueAsString(new PatchRequest(30L));
        when(caseService.updateCase(eq(9L), any())).thenThrow(new BodyRegionNotFoundException(30L));

        // Act
        var response = mockMvc.perform(patch("/api/cases/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Could not find body region with id 30"));
    }

    private CaseResponseDto response(Long id, Long patientId, Long bodyRegionId) {
        CaseResponseDto response = new CaseResponseDto();
        response.setId(id);
        response.setPatientId(patientId);
        response.setBodyRegionId(bodyRegionId);
        response.setCreatedAt(Date.from(Instant.parse("2026-03-01T10:00:00Z")));
        return response;
    }

    private record PatchRequest(Long bodyRegionId) {
    }
}
