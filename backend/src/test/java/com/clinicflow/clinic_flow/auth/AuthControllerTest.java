package com.clinicflow.clinic_flow.auth;

import com.clinicflow.clinic_flow.auth.controllers.AuthController;
import com.clinicflow.clinic_flow.auth.records.AuthPrincipal;
import com.clinicflow.clinic_flow.auth.dtos.LoginRequest;
import com.clinicflow.clinic_flow.auth.dtos.LoginResponse;
import com.clinicflow.clinic_flow.auth.records.LoginResult;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.config.JwtConfig;
import com.clinicflow.clinic_flow.exception.GlobalExceptionHandler;
import com.clinicflow.clinic_flow.users.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtConfig jwtConfig;

    @MockitoBean
    private AuthSessionService authSessionService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldLogin_whenRequestIsValid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("sam");
        request.setPassword("secret1");

        Jwt accessToken = mock(Jwt.class);
        Jwt refreshToken = mock(Jwt.class);

        when(accessToken.toString()).thenReturn("access-token");
        when(refreshToken.toString()).thenReturn("refresh-token");
        when(jwtConfig.getRefreshTokenExpiration()).thenReturn(3600);
        when(jwtConfig.isCookieSecure()).thenReturn(true);
        when(authService.login(any(LoginRequest.class))).thenReturn(new LoginResult(accessToken, refreshToken));

        mockMvc.perform(post("/api/auth/demo-clinic/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(cookie().value("refreshToken", "refresh-token"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/api/auth/refresh"))
                .andExpect(cookie().maxAge("refreshToken", 3600));

        verify(authService).login(argThat(loginRequest ->
                "sam".equals(loginRequest.getUsername())
                        && "secret1".equals(loginRequest.getPassword())
                        && "demo-clinic".equals(loginRequest.getSlug())
        ));
    }

    @Test
    void shouldReturnBadRequest_whenLoginRequestIsInvalid() throws Exception {
        String json = """
                {
                  "username": "",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/demo-clinic/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasItem("username: username required")))
                .andExpect(jsonPath("$.errors", hasItem("password: password required")));
    }

    @Test
    void shouldReturnUnauthorized_whenLoginCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("sam");
        request.setPassword("wrong-password");

        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/demo-clinic/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLogout_whenAuthenticatedUserExists() throws Exception {
        when(jwtConfig.isCookieSecure()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new AuthPrincipal(7L, "sam", "session-123", 2L), null, List.of())
        );

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().value("refreshToken", ""))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", false))
                .andExpect(cookie().path("refreshToken", "/api/auth/refresh"))
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(authService).logout("session-123", 2L);
    }

    @Test
    void shouldReturnAccessToken_whenRefreshTokenIsValid() throws Exception {
        Jwt refreshJwt = mock(Jwt.class);
        Jwt accessJwt = mock(Jwt.class);
        Jwt newRefreshJwt = mock(Jwt.class);

        when(jwtService.parseToken("refresh-token")).thenReturn(refreshJwt);
        when(refreshJwt.isExpired()).thenReturn(false);
        when(refreshJwt.getTokenType()).thenReturn("refresh");
        when(authService.refreshToken(refreshJwt)).thenReturn(new LoginResult(accessJwt, newRefreshJwt));
        when(jwtConfig.getRefreshTokenExpiration()).thenReturn(3600);
        when(jwtConfig.isCookieSecure()).thenReturn(true);
        when(accessJwt.toString()).thenReturn("new-access-token");
        when(newRefreshJwt.toString()).thenReturn("new-refresh-token");

        mockMvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie("refreshToken", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(cookie().value("refreshToken", "new-refresh-token"));

        verify(authService).refreshToken(refreshJwt);
    }

    @Test
    void shouldReturnUnauthorized_whenRefreshTokenCannotBeParsed() throws Exception {
        when(jwtService.parseToken("bad-token")).thenReturn(null);

        mockMvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie("refreshToken", "bad-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorized_whenRefreshTokenIsExpired() throws Exception {
        Jwt refreshJwt = mock(Jwt.class);

        when(jwtService.parseToken("expired-token")).thenReturn(refreshJwt);
        when(refreshJwt.isExpired()).thenReturn(true);

        mockMvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie("refreshToken", "expired-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorized_whenRefreshTokenHasWrongType() throws Exception {
        Jwt refreshJwt = mock(Jwt.class);

        when(jwtService.parseToken("access-token")).thenReturn(refreshJwt);
        when(refreshJwt.isExpired()).thenReturn(false);
        when(refreshJwt.getTokenType()).thenReturn("access");

        mockMvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie("refreshToken", "access-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnValidationResult_whenValidateEndpointIsCalled() throws Exception {
        when(authService.validateToken("access-token")).thenReturn(true);

        mockMvc.perform(post("/api/auth/validate").header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(authService).validateToken("access-token");
    }

    @Test
    void shouldReturnCurrentUser_whenMeFindsUser() throws Exception {
        LoginResponse response = new LoginResponse();
        response.setId(11L);
        response.setUsername("sam");
        response.setRoleName(Users.RoleName.ADMIN);
        response.setIsDemo(true);

        when(authService.me()).thenReturn(response);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.username").value("sam"))
                .andExpect(jsonPath("$.roleName").value("ADMIN"))
                .andExpect(jsonPath("$.isDemo").value(true));
    }

    @Test
    void shouldReturnNotFound_whenMeDoesNotFindUser() throws Exception {
        when(authService.me()).thenReturn(null);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isNotFound());
    }
}
