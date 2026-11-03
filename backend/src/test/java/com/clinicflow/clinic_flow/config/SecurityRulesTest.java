package com.clinicflow.clinic_flow.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinicflow.clinic_flow.appointment.AppointmentService;
import com.clinicflow.clinic_flow.appointment.dtos.BoardRowDto;
import com.clinicflow.clinic_flow.auth.AuthService;
import com.clinicflow.clinic_flow.auth.JwtService;
import com.clinicflow.clinic_flow.auth.dtos.LoginResponse;
import com.clinicflow.clinic_flow.auth.records.LoginResult;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.exception.GlobalExceptionHandler;
import com.clinicflow.clinic_flow.patient.PatientService;
import com.clinicflow.clinic_flow.patient.dtos.PatientResponseDto;
import com.clinicflow.clinic_flow.therapist.TherapistService;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import com.clinicflow.clinic_flow.user_clinic_memberships.UserClinicMembershipService;
import com.clinicflow.clinic_flow.users.Users;
import com.clinicflow.clinic_flow.users.UsersService;
import com.clinicflow.clinic_flow.users.dtos.UsersResponseDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Runs every API route through the real security filter chain ({@link SecurityConfig} and
 * {@code JwtAuthenticationFilter}) with real signed tokens, and checks what an anonymous caller, a
 * DISPLAY user and an ADMIN user get back. Services are mocked so that a 401/403 here can only come
 * from security, never from a service (e.g. the demo clinic's read-only 403).
 */
@WebMvcTest(
        properties = {
            "spring.jwt.secret=" + SecurityRulesTest.SECRET,
            "spring.jwt.accessTokenExpiration=60",
            "spring.jwt.refreshTokenExpiration=300"
        })
@Import({SecurityConfig.class, CorsConfig.class, JwtConfig.class, JwtService.class, GlobalExceptionHandler.class})
class SecurityRulesTest {

    static final String SECRET = "security-rules-test-secret-that-is-at-least-32-bytes";
    private static final Long CLINIC_ID = 7L;

    private static final List<Route> ROUTES = List.of(
            // Appointments: the board read is open to both roles, every write is ADMIN only
            route(HttpMethod.GET, "/api/appointments/date", Access.SIGNED_IN),
            route(HttpMethod.POST, "/api/appointments", Access.ADMIN_ONLY)
                    .withBody(
                            "{\"scheduledAt\":\"2026-11-03T09:00:00\",\"therapistId\":1,\"type\":\"FOLLOW_UP\",\"patientId\":1}"),
            route(HttpMethod.PATCH, "/api/appointments/{id}", "/api/appointments/1", Access.ADMIN_ONLY)
                    .withBody("{\"status\":\"WAITING\"}"),
            route(HttpMethod.DELETE, "/api/appointments/{id}", "/api/appointments/1", Access.ADMIN_ONLY),

            // Patients
            route(HttpMethod.GET, "/api/patients", Access.ADMIN_ONLY),
            route(HttpMethod.GET, "/api/patients/{id}", "/api/patients/1", Access.ADMIN_ONLY),
            route(HttpMethod.GET, "/api/patients/search", "/api/patients/search?q=ada", Access.ADMIN_ONLY),
            route(HttpMethod.PATCH, "/api/patients/{id}", "/api/patients/1", Access.ADMIN_ONLY)
                    .withBody("{\"firstName\":\"Ada\"}"),
            route(HttpMethod.POST, "/api/patients", Access.ADMIN_ONLY)
                    .withBody("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}"),
            route(HttpMethod.DELETE, "/api/patients/{id}", "/api/patients/1", Access.ADMIN_ONLY),

            // Therapists
            route(HttpMethod.GET, "/api/therapists", Access.ADMIN_ONLY),
            route(HttpMethod.GET, "/api/therapists/{id}", "/api/therapists/1", Access.ADMIN_ONLY),
            route(HttpMethod.POST, "/api/therapists", Access.ADMIN_ONLY).withBody("{\"name\":\"Dana Scully\"}"),
            route(HttpMethod.DELETE, "/api/therapists/{id}", "/api/therapists/1", Access.ADMIN_ONLY),

            // Users
            route(HttpMethod.GET, "/api/users", Access.ADMIN_ONLY),
            route(HttpMethod.GET, "/api/users/{userId}", "/api/users/1", Access.ADMIN_ONLY),
            route(HttpMethod.POST, "/api/users", Access.ADMIN_ONLY)
                    .withBody("{\"username\":\"newuser\",\"passwordHash\":\"secret1\"}"),
            route(HttpMethod.PATCH, "/api/users/{id}", "/api/users/1", Access.ADMIN_ONLY)
                    .withBody("{\"roleName\":\"ADMIN\"}"),

            // Auth: signing in is public, everything about the current session needs a token
            route(HttpMethod.POST, "/api/auth/login", Access.PUBLIC)
                    .withBody("{\"username\":\"sam\",\"password\":\"secret1\"}"),
            route(HttpMethod.POST, "/api/auth/demo/{role}", "/api/auth/demo/DISPLAY", Access.PUBLIC),
            route(HttpMethod.POST, "/api/auth/refresh", Access.PUBLIC).withRefreshCookie(),
            route(HttpMethod.POST, "/api/auth/logout", Access.SIGNED_IN),
            route(HttpMethod.POST, "/api/auth/validate", Access.SIGNED_IN),
            route(HttpMethod.GET, "/api/auth/me", Access.SIGNED_IN));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @MockitoBean
    private AuthSessionService authSessionService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private TherapistService therapistService;

    @MockitoBean
    private UsersService usersService;

    @MockitoBean
    private UserClinicMembershipService userClinicMembershipService;

    @BeforeEach
    void stubServices() {
        when(authSessionService.isAccessSessionActive(anyString(), anyLong())).thenReturn(true);

        // Handlers that read fields off the service result need a non-null one to answer 2xx.
        var row = new BoardRowDto();
        row.setId(1L);
        when(appointmentService.createAppointment(any())).thenReturn(row);
        when(patientService.getPatients(any())).thenReturn(Page.empty());
        when(patientService.createPatient(any())).thenReturn(new PatientResponseDto(1L, "Ada", "Lovelace"));
        when(therapistService.createTherapist(any())).thenReturn(new TherapistsResponseDto(1L, "Dana Scully"));
        var createdUser = new UsersResponseDto();
        createdUser.setId(1L);
        when(usersService.createUser(any())).thenReturn(createdUser);

        var tokens = new LoginResult(
                jwtService.generateAccessToken(user(Users.RoleName.DISPLAY), "sid", CLINIC_ID),
                jwtService.generateRefreshToken(user(Users.RoleName.DISPLAY), "sid", CLINIC_ID));
        when(authService.login(any())).thenReturn(tokens);
        when(authService.loginAsDemo(any())).thenReturn(tokens);
        when(authService.refreshToken(any())).thenReturn(tokens);
        when(authService.me()).thenReturn(new LoginResponse());
    }

    @ParameterizedTest(name = "{1} {0}")
    @MethodSource("routesByCaller")
    void callerGetsExpectedStatus(Route route, Caller caller) throws Exception {
        var request = request(route.method(), route.path());
        if (route.body() != null)
            request.contentType(MediaType.APPLICATION_JSON).content(route.body());
        if (route.refreshCookie()) request.cookie(new Cookie("refreshToken", refreshToken()));
        if (caller != Caller.ANONYMOUS) request.header(HttpHeaders.AUTHORIZATION, bearer(caller.role));

        mockMvc.perform(request).andExpect(route.access().expectedFor(caller));
    }

    @Test
    void everyApiRouteHasASecurityRule() {
        var mapped = handlerMapping.getHandlerMethods().keySet().stream()
                .flatMap(info -> info.getPatternValues().stream()
                        .filter(pattern -> pattern.startsWith("/api/"))
                        .flatMap(pattern -> info.getMethodsCondition().getMethods().stream()
                                .map(method -> method.name() + " " + pattern)))
                .collect(Collectors.toSet());
        var covered = ROUTES.stream().map(Route::toString).collect(Collectors.toSet());

        assertThat(mapped).as("routes with no row in ROUTES").isSubsetOf(covered);
        assertThat(covered).as("rows for routes that no longer exist").isSubsetOf(mapped);
    }

    // Self-registration was removed: accounts are only created by an admin through POST /api/users.
    @Test
    void oldRegisterRouteIsNotPublic() throws Exception {
        mockMvc.perform(post("/api/auth/sunrise-pt/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"stranger\",\"passwordHash\":\"secret1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsRequestWithMalformedToken() throws Exception {
        mockMvc.perform(get("/api/therapists").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() throws Exception {
        var otherKey = Keys.hmacShaKeyFor("a-different-secret-that-is-also-at-least-32-bytes".getBytes());
        var token = signedToken(otherKey, "access", new Date(System.currentTimeMillis() + 60_000));

        mockMvc.perform(get("/api/therapists").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        var token = signedToken(jwtConfig.getSecretKey(), "access", new Date(System.currentTimeMillis() - 60_000));

        mockMvc.perform(get("/api/therapists").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsRefreshTokenUsedAsAccessToken() throws Exception {
        var token = jwtService
                .generateRefreshToken(user(Users.RoleName.ADMIN), "sid", CLINIC_ID)
                .toString();

        mockMvc.perform(get("/api/therapists").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsTokenWhoseSessionIsNoLongerActive() throws Exception {
        when(authSessionService.isAccessSessionActive(anyString(), anyLong())).thenReturn(false);

        mockMvc.perform(get("/api/therapists").header(HttpHeaders.AUTHORIZATION, bearer(Users.RoleName.ADMIN)))
                .andExpect(status().isUnauthorized());
    }

    static Stream<Arguments> routesByCaller() {
        return ROUTES.stream()
                .flatMap(route -> Arrays.stream(Caller.values()).map(caller -> Arguments.of(route, caller)));
    }

    private String bearer(Users.RoleName role) {
        return "Bearer "
                + jwtService.generateAccessToken(user(role), "sid", CLINIC_ID).toString();
    }

    private String refreshToken() {
        return jwtService
                .generateRefreshToken(user(Users.RoleName.DISPLAY), "sid", CLINIC_ID)
                .toString();
    }

    private static String signedToken(SecretKey key, String tokenType, Date expiration) {
        return Jwts.builder()
                .subject("1")
                .claim("username", "admin")
                .claim("role", Users.RoleName.ADMIN.name())
                .claim("sid", "sid")
                .claim("token_type", tokenType)
                .claim("clinic_id", CLINIC_ID)
                .issuedAt(new Date(expiration.getTime() - 120_000))
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    private static Users user(Users.RoleName role) {
        var user = new Users();
        user.setId(role == Users.RoleName.ADMIN ? 1L : 2L);
        user.setUsername(role.name().toLowerCase());
        user.setRoleName(role);
        return user;
    }

    private static Route route(HttpMethod method, String path, Access access) {
        return route(method, path, path, access);
    }

    private static Route route(HttpMethod method, String pattern, String path, Access access) {
        return new Route(method, pattern, path, null, false, access);
    }

    enum Caller {
        ANONYMOUS(null),
        DISPLAY(Users.RoleName.DISPLAY),
        ADMIN(Users.RoleName.ADMIN);

        private final Users.RoleName role;

        Caller(Users.RoleName role) {
            this.role = role;
        }
    }

    enum Access {
        PUBLIC,
        SIGNED_IN,
        ADMIN_ONLY;

        ResultMatcher expectedFor(Caller caller) {
            if (this == PUBLIC) return status().is2xxSuccessful();
            if (caller == Caller.ANONYMOUS) return status().isUnauthorized();
            if (this == ADMIN_ONLY && caller == Caller.DISPLAY) return status().isForbidden();
            return status().is2xxSuccessful();
        }
    }

    record Route(HttpMethod method, String pattern, String path, String body, boolean refreshCookie, Access access) {

        Route withBody(String json) {
            return new Route(method, pattern, path, json, refreshCookie, access);
        }

        Route withRefreshCookie() {
            return new Route(method, pattern, path, body, true, access);
        }

        @Override
        public String toString() {
            return method.name() + " " + pattern;
        }
    }
}
