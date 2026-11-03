package com.clinicflow.clinic_flow.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinicflow.clinic_flow.auth_sessions.AuthSessionRepository;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.clinics.ClinicsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Disabling and re-enabling users through {@code PATCH /api/users/{id}}, checked only through the HTTP API
 * (login, refresh, PATCH, GET), as written in Step 5 of docs/security-hardening-plan.md. Users are seeded
 * through repositories; nothing here looks at how the feature is implemented.
 *
 * <p>Not {@code @Transactional}: each request commits on its own, as in production, so a disable must really
 * reach the database to lock the user out.
 */
@SpringBootTest(
        properties = {
            "spring.jwt.secret=security-wiring-test-secret-that-is-at-least-32-bytes",
            "spring.jwt.accessTokenExpiration=60",
            "spring.jwt.refreshTokenExpiration=300"
        })
@AutoConfigureMockMvc
class UserEnabledIntegrationTest {

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
    private AuthSessionRepository authSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final List<Clinics> seededClinics = new ArrayList<>();
    private final List<Users> seededUsers = new ArrayList<>();

    private Users clinicAdmin;
    private Users clinicDisplay;
    private Users otherClinicDisplay;
    private Users demoDisplay;

    @BeforeEach
    void seed() {
        var clinic = saveClinic("enabled-test", false);
        clinicAdmin = saveUser("enabled_admin", Users.RoleName.ADMIN, clinic);
        clinicDisplay = saveUser("enabled_display", Users.RoleName.DISPLAY, clinic);

        var otherClinic = saveClinic("enabled-test-other", false);
        otherClinicDisplay = saveUser("enabled_other_display", Users.RoleName.DISPLAY, otherClinic);

        var demoClinic = saveClinic("enabled-test-demo", true);
        saveUser("enabled_demo_admin", Users.RoleName.ADMIN, demoClinic);
        demoDisplay = saveUser("enabled_demo_display", Users.RoleName.DISPLAY, demoClinic);
    }

    @AfterEach
    void cleanUp() {
        authSessionRepository.deleteAll();
        usersRepository.deleteAll(seededUsers);
        clinicsRepository.deleteAll(seededClinics);
    }

    // 1
    @Test
    void patchWithNeitherFieldIsRejected() throws Exception {
        var admin = login("enabled_admin");

        patchUser(admin, clinicDisplay, "{}").andExpect(status().isBadRequest());
    }

    // 2
    @Test
    void disablingReturnsTheUserAsDisabledWithTheSameRole() throws Exception {
        var admin = login("enabled_admin");

        patchUser(admin, clinicDisplay, "{\"enabled\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("enabled_display"))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.roleName").value("DISPLAY"));
    }

    // 3
    @Test
    void disabledUserCannotLogIn() throws Exception {
        var admin = login("enabled_admin");
        patchUser(admin, clinicDisplay, "{\"enabled\":false}").andExpect(status().isOk());

        attemptLogin("enabled_display").andExpect(status().isUnauthorized());
    }

    // 3: the refusal must not reveal that the account exists but is disabled
    @Test
    void disabledLoginLooksTheSameAsAWrongPassword() throws Exception {
        var admin = login("enabled_admin");
        patchUser(admin, clinicDisplay, "{\"enabled\":false}").andExpect(status().isOk());

        var disabled = attemptLogin("enabled_display").andReturn().getResponse();
        var wrongPassword =
                attemptLogin("enabled_admin", "not-the-password").andReturn().getResponse();

        assertThat(disabled.getStatus()).isEqualTo(wrongPassword.getStatus());
        assertThat(disabled.getContentAsString()).isEqualTo(wrongPassword.getContentAsString());
    }

    // 4
    @Test
    void disablingEndsTheUsersOpenSessionImmediately() throws Exception {
        var display = loginSession("enabled_display");
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, display.bearer()))
                .andExpect(status().isOk());

        var admin = login("enabled_admin");
        patchUser(admin, clinicDisplay, "{\"enabled\":false}").andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, display.bearer()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").cookie(display.refreshCookie()))
                .andExpect(status().isUnauthorized());
    }

    // 5
    @Test
    void reEnabledUserCanLogInAgain() throws Exception {
        var admin = login("enabled_admin");
        patchUser(admin, clinicDisplay, "{\"enabled\":false}").andExpect(status().isOk());
        attemptLogin("enabled_display").andExpect(status().isUnauthorized());

        patchUser(admin, clinicDisplay, "{\"enabled\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        attemptLogin("enabled_display").andExpect(status().isOk());
    }

    // 6
    @Test
    void roleChangeAloneStillWorksAndLeavesTheUserEnabled() throws Exception {
        var admin = login("enabled_admin");

        patchUser(admin, clinicDisplay, "{\"roleName\":\"ADMIN\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("ADMIN"))
                .andExpect(jsonPath("$.enabled").value(true));

        // The promoted user can sign in and reach an admin-only route.
        var promoted = login("enabled_display");
        mockMvc.perform(get("/api/therapists").header(HttpHeaders.AUTHORIZATION, "Bearer " + promoted))
                .andExpect(status().isOk());
    }

    // 7
    @Test
    void adminCannotDisableThemselves() throws Exception {
        var admin = login("enabled_admin");

        patchUser(admin, clinicAdmin, "{\"enabled\":false}").andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + admin))
                .andExpect(status().isOk());
        attemptLogin("enabled_admin").andExpect(status().isOk());
    }

    // 8
    @Test
    void userInAnotherClinicCannotBeChanged() throws Exception {
        var admin = login("enabled_admin");

        patchUser(admin, otherClinicDisplay, "{\"enabled\":false}").andExpect(status().isNotFound());

        attemptLogin("enabled_other_display").andExpect(status().isOk());
    }

    // 9
    @Test
    void demoClinicAdminCannotChangeUsers() throws Exception {
        var demoAdmin = login("enabled_demo_admin");

        patchUser(demoAdmin, demoDisplay, "{\"enabled\":false}").andExpect(status().isForbidden());
        patchUser(demoAdmin, demoDisplay, "{\"roleName\":\"ADMIN\"}").andExpect(status().isForbidden());

        attemptLogin("enabled_demo_display").andExpect(status().isOk());
        mockMvc.perform(get("/api/users/" + demoDisplay.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + demoAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("DISPLAY"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    // 10
    @Test
    void userListAndDetailShowWhetherEachUserIsEnabled() throws Exception {
        var admin = login("enabled_admin");
        patchUser(admin, clinicDisplay, "{\"enabled\":false}").andExpect(status().isOk());

        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'enabled_display')].enabled", contains(false)))
                .andExpect(jsonPath("$[?(@.username == 'enabled_admin')].enabled", contains(true)));
        mockMvc.perform(get("/api/users/" + clinicDisplay.getId()).header(HttpHeaders.AUTHORIZATION, "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    private ResultActions patchUser(String accessToken, Users target, String body) throws Exception {
        return mockMvc.perform(patch("/api/users/" + target.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String login(String username) throws Exception {
        return loginSession(username).accessToken();
    }

    private Session loginSession(String username) throws Exception {
        var response =
                attemptLogin(username).andExpect(status().isOk()).andReturn().getResponse();
        var token = objectMapper
                .readTree(response.getContentAsString())
                .get("token")
                .asText();
        return new Session(token, response.getCookie("refreshToken"));
    }

    private ResultActions attemptLogin(String username) throws Exception {
        return attemptLogin(username, PASSWORD);
    }

    private ResultActions attemptLogin(String username, String password) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of("username", username, "password", password));
        return mockMvc.perform(
                post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private Clinics saveClinic(String slug, boolean isDemo) {
        var clinic = clinicsRepository.save(new Clinics(null, slug, slug, isDemo, LocalDateTime.now()));
        seededClinics.add(clinic);
        return clinic;
    }

    private Users saveUser(String username, Users.RoleName role, Clinics clinic) {
        var user = new Users();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRoleName(role);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setClinic(clinic);
        var saved = usersRepository.save(user);
        seededUsers.add(saved);
        return saved;
    }

    private record Session(String accessToken, Cookie refreshCookie) {
        String bearer() {
            return "Bearer " + accessToken;
        }
    }
}
