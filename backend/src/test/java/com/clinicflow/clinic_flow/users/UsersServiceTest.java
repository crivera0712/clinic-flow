package com.clinicflow.clinic_flow.users;

import com.clinicflow.clinic_flow.exception.UserAlreadyExistsException;
import com.clinicflow.clinic_flow.exception.UserNotFoundException;
import com.clinicflow.clinic_flow.users.dtos.CreateUserRequest;
import com.clinicflow.clinic_flow.users.dtos.UserPatchDto;
import com.clinicflow.clinic_flow.users.dtos.UsersResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private UsersMapper usersMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsersService usersService;

    @Test
    void shouldReturnUsers_whenGetUsersFindsUsers() {
        // Arrange
        Users firstUser = user(1L, "sam", Users.RoleName.DISPLAY);
        Users secondUser = user(2L, "alex", Users.RoleName.ADMIN);
        UsersResponseDto firstResponse = response(1L, "sam", Users.RoleName.DISPLAY);
        UsersResponseDto secondResponse = response(2L, "alex", Users.RoleName.ADMIN);

        when(usersRepository.findAll()).thenReturn(List.of(firstUser, secondUser));
        when(usersMapper.toUsersResponseDto(firstUser)).thenReturn(firstResponse);
        when(usersMapper.toUsersResponseDto(secondUser)).thenReturn(secondResponse);

        // Act
        List<UsersResponseDto> result = usersService.getUsers();

        // Assert
        assertEquals(List.of(firstResponse, secondResponse), result);
        verify(usersRepository).findAll();
        verify(usersMapper).toUsersResponseDto(firstUser);
        verify(usersMapper).toUsersResponseDto(secondUser);
    }

    @Test
    void shouldReturnEmptyList_whenGetUsersFindsNoUsers() {
        // Arrange
        when(usersRepository.findAll()).thenReturn(List.of());

        // Act
        List<UsersResponseDto> result = usersService.getUsers();

        // Assert
        assertTrue(result.isEmpty());
        verify(usersRepository).findAll();
        verify(usersMapper, never()).toUsersResponseDto(any(Users.class));
    }

    @Test
    void shouldReturnUser_whenGetUserByIdFindsUser() {
        // Arrange
        Users user = user(5L, "sam", Users.RoleName.DISPLAY);
        UsersResponseDto response = response(5L, "sam", Users.RoleName.DISPLAY);

        when(usersRepository.findById(5L)).thenReturn(Optional.of(user));
        when(usersMapper.toUsersResponseDto(user)).thenReturn(response);

        // Act
        UsersResponseDto result = usersService.getUserById(5L);

        // Assert
        assertSame(response, result);
        verify(usersRepository).findById(5L);
        verify(usersMapper).toUsersResponseDto(user);
    }

    @Test
    void shouldThrowUserNotFoundException_whenGetUserByIdDoesNotFindUser() {
        // Arrange
        when(usersRepository.findById(5L)).thenReturn(Optional.empty());

        // Act
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> usersService.getUserById(5L));

        // Assert
        assertEquals("User with id 5 not found", exception.getMessage());
        verify(usersRepository).findById(5L);
        verify(usersMapper, never()).toUsersResponseDto(any(Users.class));
    }

    @Test
    void shouldCreateUserWithDefaultDisplayRole_whenCreateUserReceivesValidRequest() {
        // Arrange
        CreateUserRequest request = createRequest("sam", "secret1", true, Users.RoleName.ADMIN);
        Users mappedUser = user(null, "sam", null);
        Users savedUser = user(9L, "sam", Users.RoleName.DISPLAY);
        UsersResponseDto response = response(9L, "sam", Users.RoleName.DISPLAY);

        when(usersRepository.findByUsername("sam")).thenReturn(Optional.empty());
        when(usersMapper.toEntity(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded-secret1");
        when(usersRepository.save(mappedUser)).thenReturn(savedUser);
        when(usersMapper.toUsersResponseDto(savedUser)).thenReturn(response);

        // Act
        UsersResponseDto result = usersService.createUser(request);

        // Assert
        assertSame(response, result);
        assertEquals(Users.RoleName.DISPLAY, mappedUser.getRoleName());
        assertEquals("encoded-secret1", mappedUser.getPasswordHash());
        assertNotNull(mappedUser.getCreatedAt());
        verify(usersRepository).findByUsername("sam");
        verify(usersMapper).toEntity(request);
        verify(passwordEncoder).encode("secret1");
        verify(usersRepository).save(mappedUser);
        verify(usersMapper).toUsersResponseDto(savedUser);
    }

    @Test
    void shouldThrowConflict_whenCreateUserFindsExistingUsername() {
        // Arrange
        CreateUserRequest request = createRequest("sam", "secret1", true, null);

        when(usersRepository.findByUsername("sam")).thenReturn(Optional.of(user(3L, "sam", Users.RoleName.DISPLAY)));

        // Act
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> usersService.createUser(request));

        // Assert
        assertEquals("Username sam already exists", exception.getMessage());
        verify(usersRepository).findByUsername("sam");
        verify(usersMapper, never()).toEntity(any(CreateUserRequest.class));
        verify(usersRepository, never()).save(any(Users.class));
    }

    @Test
    void shouldUpdateRole_whenUpdateUserReceivesValidPatch() {
        // Arrange
        Users user = user(4L, "sam", Users.RoleName.DISPLAY);
        UserPatchDto patch = patch(Users.RoleName.ADMIN);
        UsersResponseDto response = response(4L, "sam", Users.RoleName.ADMIN);

        when(usersRepository.findById(4L)).thenReturn(Optional.of(user));
        when(usersRepository.save(user)).thenReturn(user);
        when(usersMapper.toUsersResponseDto(user)).thenReturn(response);

        // Act
        UsersResponseDto result = usersService.updateUser(4L, patch);

        // Assert
        assertSame(response, result);
        assertEquals(Users.RoleName.ADMIN, user.getRoleName());
        verify(usersRepository).findById(4L);
        verify(usersRepository).save(user);
        verify(usersMapper).toUsersResponseDto(user);
    }

    @Test
    void shouldThrowUserNotFoundException_whenUpdateUserDoesNotFindUser() {
        // Arrange
        UserPatchDto patch = patch(Users.RoleName.ADMIN);
        when(usersRepository.findById(4L)).thenReturn(Optional.empty());

        // Act
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> usersService.updateUser(4L, patch));

        // Assert
        assertEquals("User with id 4 not found", exception.getMessage());
        verify(usersRepository).findById(4L);
        verify(usersRepository, never()).save(any(Users.class));
    }

    private CreateUserRequest createRequest(String username, String passwordHash, boolean enabled, Users.RoleName role) {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(username);
        request.setPasswordHash(passwordHash);
        return request;
    }

    private UserPatchDto patch(Users.RoleName role) {
        UserPatchDto patch = new UserPatchDto();
        patch.setRoleName(role);
        return patch;
    }

    private Users user(Long id, String username, Users.RoleName roleName) {
        Users user = new Users();
        user.setId(id);
        user.setUsername(username);
        user.setPasswordHash("secret1");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
        user.setRoleName(roleName);
        return user;
    }

    private UsersResponseDto response(Long id, String username, Users.RoleName role) {
        UsersResponseDto response = new UsersResponseDto();
        response.setId(id);
        response.setUsername(username);
        response.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
        response.setRoleName(role);
        return response;
    }
}
