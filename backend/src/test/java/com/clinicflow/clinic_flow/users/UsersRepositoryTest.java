package com.clinicflow.clinic_flow.users;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UsersRepositoryTest {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnUser_whenFindByUsernameMatchesExistingUser() {
        // Arrange
        Users user = persistUser("sam", Users.RoleName.DISPLAY);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Users> result = usersRepository.findByUsername("sam");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getId());
        assertEquals("sam", result.get().getUsername());
        assertEquals(Users.RoleName.DISPLAY, result.get().getRoleName());
    }

    @Test
    void shouldReturnEmptyOptional_whenFindByUsernameDoesNotMatchExistingUser() {
        // Arrange
        persistUser("sam", Users.RoleName.DISPLAY);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Users> result = usersRepository.findByUsername("alex");

        // Assert
        assertFalse(result.isPresent());
    }

    private Users persistUser(String username, Users.RoleName roleName) {
        Users user = new Users();
        user.setUsername(username);
        user.setPasswordHash("secret1");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
        user.setRoleName(roleName);
        entityManager.persist(user);
        return user;
    }
}
