package com.clinicflow.clinic_flow.users;

import com.clinicflow.clinic_flow.clinics.Clinics;
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
        Users user = persistUser("sam", Users.RoleName.DISPLAY, 1L);
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
        persistUser("sam", Users.RoleName.DISPLAY, 1L);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Users> result = usersRepository.findByUsername("alex");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnClinicScopedUser_whenFindByUsernameAndClinicIdMatches() {
        Users clinicOneUser = persistUser("sam", Users.RoleName.DISPLAY, 1L);
        persistUser("sam", Users.RoleName.ADMIN, 2L);
        entityManager.flush();
        entityManager.clear();

        Optional<Users> result = usersRepository.findByUsernameAndClinicId(
                "sam",
                clinicOneUser.getClinic().getId()
        );

        assertTrue(result.isPresent());
        assertEquals(clinicOneUser.getUsername(), result.get().getUsername());
        assertEquals(clinicOneUser.getClinic().getId(), result.get().getClinic().getId());
    }

    private Users persistUser(String username, Users.RoleName roleName, Long clinicId) {
        Clinics clinic = new Clinics();
        clinic.setName("Clinic " + clinicId);
        clinic.setSlug("clinic-" + clinicId);
        clinic.setIsDemo(false);
        clinic.setCreatedAt(LocalDateTime.of(2026, 3, 1, 9, 0));
        entityManager.persist(clinic);

        Users user = new Users();
        user.setUsername(username);
        user.setPasswordHash("secret1");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
        user.setRoleName(roleName);
        user.setClinic(clinic);
        entityManager.persist(user);
        return user;
    }
}
