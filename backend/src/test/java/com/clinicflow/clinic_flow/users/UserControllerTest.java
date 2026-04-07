package com.clinicflow.clinic_flow.users;

import com.clinicflow.clinic_flow.auth.JwtService;
import com.clinicflow.clinic_flow.auth_sessions.AuthSessionService;
import com.clinicflow.clinic_flow.exception.DemoClinicReadOnlyException;
import com.clinicflow.clinic_flow.exception.GlobalExceptionHandler;
import com.clinicflow.clinic_flow.exception.UserAlreadyExistsException;
import com.clinicflow.clinic_flow.exception.UserNotFoundException;
import com.clinicflow.clinic_flow.users.dtos.UsersResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsersService usersService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthSessionService authSessionService;

    @Test
    void shouldReturnUsers_whenGetUsersIsCalled() throws Exception {
        // Arrange
        when(usersService.getUsers()).thenReturn(List.of(
                response(1L, "sam", Users.RoleName.DISPLAY),
                response(2L, "alex", Users.RoleName.ADMIN)
        ));

        // Act
        var response = mockMvc.perform(get("/api/users"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("sam"))
                .andExpect(jsonPath("$[0].roleName").value(Users.RoleName.DISPLAY.toString()))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].roleName").value(Users.RoleName.ADMIN.toString()));
        verify(usersService).getUsers();
    }

    @Test
    void shouldReturnUser_whenGetUserFindsUser() throws Exception {
        // Arrange
        when(usersService.getUserById(5L)).thenReturn(response(5L, "sam", Users.RoleName.DISPLAY));

        // Act
        var response = mockMvc.perform(get("/api/users/5"));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.username").value("sam"))
                .andExpect(jsonPath("$.roleName").value(Users.RoleName.DISPLAY.toString()));
        verify(usersService).getUserById(5L);
    }

    @Test
    void shouldReturnNotFound_whenGetUserDoesNotFindUser() throws Exception {
        // Arrange
        when(usersService.getUserById(5L)).thenThrow(new UserNotFoundException(5L));

        // Act
        var response = mockMvc.perform(get("/api/users/5"));

        // Assert
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id 5 not found"));
    }

    @Test
    void shouldCreateUser_whenPostUserReceivesValidRequest() throws Exception {
        // Arrange
        String json = """
                {
                  "username": "sam",
                  "passwordHash": "secret1",
                  "enabled": true,
                  "role": "ADMIN"
                }
                """;
        when(usersService.createUser(any())).thenReturn(response(12L, "sam", Users.RoleName.DISPLAY));

        // Act
        var response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/users/12")))
                .andExpect(jsonPath("$.id").value(12L))
                .andExpect(jsonPath("$.username").value("sam"))
                .andExpect(jsonPath("$.roleName").value(Users.RoleName.DISPLAY.toString()));
        verify(usersService).createUser(any());
    }

    @Test
    void shouldReturnForbidden_whenPostUserRunsInDemoClinic() throws Exception {
        String json = """
                {
                  "username": "sam",
                  "passwordHash": "secret1",
                  "enabled": true,
                  "role": "ADMIN"
                }
                """;
        when(usersService.createUser(any())).thenThrow(new DemoClinicReadOnlyException());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Demo clinic is read-only"));
    }

    @Test
    void shouldReturnBadRequest_whenPostUserMissingUsername() throws Exception {
        // Arrange
        String json = """
                {
                  "passwordHash": "secret1",
                  "enabled": true
                }
                """;

        // Act
        var response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnBadRequest_whenPostUserReceivesShortPassword() throws Exception {
        // Arrange
        String json = """
                {
                  "username": "sam",
                  "passwordHash": "123",
                  "enabled": true
                }
                """;

        // Act
        var response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnBadRequest_whenPostUserReceivesMalformedJson() throws Exception {
        // Arrange
        String malformedJson = """
                {
                  "username": "sam",
                  "passwordHash":
                }
                """;

        // Act
        var response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnConflict_whenPostUserReceivesDuplicateUsername() throws Exception {
        // Arrange
        String json = """
                {
                  "username": "sam",
                  "passwordHash": "secret1",
                  "enabled": true
                }
                """;
        when(usersService.createUser(any())).thenThrow(new UserAlreadyExistsException("sam"));

        // Act
        var response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username sam already exists"));
    }

    @Test
    void shouldReturnUpdatedUser_whenPatchUserReceivesValidRole() throws Exception {
        // Arrange
        String json = objectMapper.writeValueAsString(new PatchRequest("ADMIN"));
        when(usersService.updateUser(eq(9L), any())).thenReturn(response(9L, "sam", Users.RoleName.ADMIN));

        // Act
        var response = mockMvc.perform(patch("/api/users/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9L))
                .andExpect(jsonPath("$.username").value("sam"))
                .andExpect(jsonPath("$.roleName").value("ADMIN"));
        verify(usersService).updateUser(eq(9L), any());
    }

    @Test
    void shouldReturnBadRequest_whenPatchUserReceivesNullRole() throws Exception {
        // Arrange
        String json = """
                {
                  "roleName": null
                }
                """;

        // Act
        var response = mockMvc.perform(patch("/api/users/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnBadRequest_whenPatchUserReceivesMalformedJson() throws Exception {
        // Arrange
        String malformedJson = """
                {
                  "roleName":
                }
                """;

        // Act
        var response = mockMvc.perform(patch("/api/users/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson));

        // Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnNotFound_whenPatchUserDoesNotFindUser() throws Exception {
        // Arrange
        String json = objectMapper.writeValueAsString(new PatchRequest("ADMIN"));
        when(usersService.updateUser(eq(9L), any())).thenThrow(new UserNotFoundException(9L));

        // Act
        var response = mockMvc.perform(patch("/api/users/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id 9 not found"));
    }

    private UsersResponseDto response(Long id, String username, Users.RoleName role) {
        UsersResponseDto response = new UsersResponseDto();
        response.setId(id);
        response.setUsername(username);
        response.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
        response.setRoleName(role);
        return response;
    }

    private record PatchRequest(String roleName) {
    }
}
