package com.clinicflow.clinic_flow.patient;

import com.clinicflow.clinic_flow.auth.JwtService;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.exception.GlobalExceptionHandler;
import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
import com.clinicflow.clinic_flow.patient.dtos.PatientResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthSessionService authSessionService;

    @Test
    void shouldReturnPatients_whenGetPatientsIsCalled() throws Exception {
        // Arrange
        when(patientService.getPatients()).thenReturn(List.of(
                new PatientResponseDto(1L, "Sam", "Lee"),
                new PatientResponseDto(2L, "Alex", "Kim")
        ));

        // Act
        var response = mockMvc.perform(get("/api/patients"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("Sam"))
                .andExpect(jsonPath("$[0].lastName").value("Lee"))
                .andExpect(jsonPath("$[1].id").value(2L));
        verify(patientService).getPatients();
    }

    @Test
    void shouldReturnPatient_whenGetPatientByIdFindsPatient() throws Exception {
        // Arrange
        when(patientService.getPatient(5L)).thenReturn(new PatientResponseDto(5L, "Sam", "Lee"));

        // Act
        var response = mockMvc.perform(get("/api/patients/5"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.firstName").value("Sam"))
                .andExpect(jsonPath("$.lastName").value("Lee"));
        verify(patientService).getPatient(5L);
    }

    @Test
    void shouldReturnNotFound_whenGetPatientByIdDoesNotFindPatient() throws Exception {
        // Arrange
        when(patientService.getPatient(5L)).thenThrow(new PatientNotFoundException(5L));

        // Act
        var response = mockMvc.perform(get("/api/patients/5"));

        // Assert
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Patient with id 5 does not exist"));
    }

    @Test
    void shouldReturnPatients_whenSearchPatientReceivesOneTokenQuery() throws Exception {
        // Arrange
        when(patientService.searchPatient("sam")).thenReturn(List.of(new PatientResponseDto(1L, "Sam", "Lee")));

        // Act
        var response = mockMvc.perform(get("/api/patients/search").param("q", "sam"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("Sam"))
                .andExpect(jsonPath("$[0].lastName").value("Lee"));
        verify(patientService).searchPatient("sam");
    }

    @Test
    void shouldReturnPatients_whenSearchPatientReceivesTwoTokenQuery() throws Exception {
        // Arrange
        when(patientService.searchPatient("sam lee")).thenReturn(List.of(new PatientResponseDto(1L, "Sam", "Lee")));

        // Act
        var response = mockMvc.perform(get("/api/patients/search").param("q", "sam lee"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("Sam"))
                .andExpect(jsonPath("$[0].lastName").value("Lee"));
        verify(patientService).searchPatient("sam lee");
    }

    @Test
    void shouldReturnBadRequest_whenSearchPatientOmitsQueryParam() throws Exception {
        // Arrange

        // Act
        var response = mockMvc.perform(get("/api/patients/search"));

        // Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenSearchPatientReceivesBlankQuery() throws Exception {
        // Arrange
        when(patientService.searchPatient("   ")).thenThrow(new IllegalArgumentException("search query must not be blank"));

        // Act
        var response = mockMvc.perform(get("/api/patients/search").param("q", "   "));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("search query must not be blank"));
    }

    @Test
    void shouldCreatePatient_whenPostPatientReceivesValidRequest() throws Exception {
        // Arrange
        String json = """
                {
                  "firstName": "Sam",
                  "lastName": "Lee"
                }
                """;
        when(patientService.createPatient(any())).thenReturn(new PatientResponseDto(12L, "Sam", "Lee"));

        // Act
        var response = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/patients/12")))
                .andExpect(jsonPath("$.id").value(12L))
                .andExpect(jsonPath("$.firstName").value("Sam"))
                .andExpect(jsonPath("$.lastName").value("Lee"));
        verify(patientService).createPatient(any());
    }

    @Test
    void shouldReturnBadRequest_whenPostPatientMissingFirstName() throws Exception {
        // Arrange
        String json = """
                {
                  "lastName": "Lee"
                }
                """;

        // Act
        var response = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnBadRequest_whenPostPatientMissingLastName() throws Exception {
        // Arrange
        String json = """
                {
                  "firstName": "Sam"
                }
                """;

        // Act
        var response = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnBadRequest_whenPostPatientReceivesBlankNames() throws Exception {
        // Arrange
        String json = """
                {
                  "firstName": " ",
                  "lastName": ""
                }
                """;

        // Act
        var response = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnBadRequest_whenPostPatientReceivesTooLongFirstName() throws Exception {
        // Arrange
        String longName = "a".repeat(256);
        String json = objectMapper.writeValueAsString(new CreateRequest(longName, "Lee"));

        // Act
        var response = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnBadRequest_whenPostPatientReceivesTooLongLastName() throws Exception {
        // Arrange
        String longName = "a".repeat(256);
        String json = objectMapper.writeValueAsString(new CreateRequest("Sam", longName));

        // Act
        var response = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnBadRequest_whenPostPatientReceivesMalformedJson() throws Exception {
        // Arrange
        String malformedJson = """
                {
                  "firstName": "Sam",
                  "lastName":
                }
                """;

        // Act
        var response = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnUpdatedPatient_whenPatchPatientReceivesValidRequest() throws Exception {
        // Arrange
        String json = objectMapper.writeValueAsString(new PatchRequest("Samuel", "Leeds"));
        when(patientService.updatePatient(eq(9L), any())).thenReturn(new PatientResponseDto(9L, "Samuel", "Leeds"));

        // Act
        var response = mockMvc.perform(patch("/api/patients/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9L))
                .andExpect(jsonPath("$.firstName").value("Samuel"))
                .andExpect(jsonPath("$.lastName").value("Leeds"));
        verify(patientService).updatePatient(eq(9L), any());
    }

    @Test
    void shouldReturnNotFound_whenPatchPatientDoesNotFindPatient() throws Exception {
        // Arrange
        String json = objectMapper.writeValueAsString(new PatchRequest("Samuel", "Leeds"));
        when(patientService.updatePatient(eq(9L), any())).thenThrow(new PatientNotFoundException(9L));

        // Act
        var response = mockMvc.perform(patch("/api/patients/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Patient with id 9 does not exist"));
    }

    @Test
    void shouldReturnBadRequest_whenPatchPatientReceivesMalformedJson() throws Exception {
        // Arrange
        String malformedJson = """
                {
                  "firstName": "Samuel",
                  "lastName":
                }
                """;

        // Act
        var response = mockMvc.perform(patch("/api/patients/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    private record CreateRequest(String firstName, String lastName) {
    }

    private record PatchRequest(String firstName, String lastName) {
    }
}
