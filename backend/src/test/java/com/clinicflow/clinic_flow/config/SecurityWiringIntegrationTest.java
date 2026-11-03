package com.clinicflow.clinic_flow.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.clinics.ClinicsRepository;
import com.clinicflow.clinic_flow.users.Users;
import com.clinicflow.clinic_flow.users.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * End-to-end check that the security chain is wired in the full application context: a real login
 * creates a real session, and the token it returns is accepted or refused by role. The per-route
 * rules themselves are covered by {@link SecurityRulesTest}.
 */
@SpringBootTest(
        properties = {
            "spring.jwt.secret=security-wiring-test-secret-that-is-at-least-32-bytes",
            "spring.jwt.accessTokenExpiration=60",
            "spring.jwt.refreshTokenExpiration=300"
        })
@AutoConfigureMockMvc
@Transactional
class SecurityWiringIntegrationTest {

    private static final String PASSWORD = "secret1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClinicsRepository clinicsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedClinic() {
        var clinic = clinicsRepository.save(
                new Clinics(null, "Wiring Test Clinic", "wiring-test", false, LocalDateTime.now()));
        saveUser("wiring_admin", Users.RoleName.ADMIN, clinic);
        saveUser("wiring_display", Users.RoleName.DISPLAY, clinic);
    }

    @Test
    void displayLoginCanReachSignedInRoutesButNotAdminRoutes() throws Exception {
        var token = login("wiring_display");

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/therapists").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminLoginCanReachAdminRoutes() throws Exception {
        var token = login("wiring_admin");

        mockMvc.perform(get("/api/therapists").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void tokenStopsWorkingAfterLogout() throws Exception {
        var token = login("wiring_admin");

        mockMvc.perform(post("/api/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/therapists").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/therapists")).andExpect(status().isUnauthorized());
    }

    private String login(String username) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of("username", username, "password", PASSWORD));
        var response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private void saveUser(String username, Users.RoleName role, Clinics clinic) {
        var user = new Users();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRoleName(role);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setClinic(clinic);
        usersRepository.save(user);
    }
}
