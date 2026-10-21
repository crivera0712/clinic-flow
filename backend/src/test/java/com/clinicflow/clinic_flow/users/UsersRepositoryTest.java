package com.clinicflow.clinic_flow.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.clinicflow.clinic_flow.clinics.Clinics;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

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
                "sam", clinicOneUser.getClinic().getId());

        assertTrue(result.isPresent());
        assertEquals(clinicOneUser.getUsername(), result.get().getUsername());
        assertEquals(clinicOneUser.getClinic().getId(), result.get().getClinic().getId());
    }

    @Test
    void shouldReturnDemoUser_whenFindingDemoUserByRole() {
        Clinics demoClinic = persistClinic("demo", true);
        persistUser("real_admin", Users.RoleName.ADMIN, persistClinic("real", false), true);
        Users demoAdmin = persistUser("demo_admin", Users.RoleName.ADMIN, demoClinic, true);
        persistUser("demo_display", Users.RoleName.DISPLAY, demoClinic, true);
        entityManager.flush();
        entityManager.clear();

        Optional<Users> result =
                usersRepository.findFirstByClinic_IsDemoTrueAndRoleNameAndEnabledTrueOrderByIdAsc(Users.RoleName.ADMIN);

        assertTrue(result.isPresent());
        assertEquals(demoAdmin.getId(), result.get().getId());
    }

    @Test
    void shouldNeverReturnNonDemoUser_whenFindingDemoUserByRole() {
        persistUser("real_admin", Users.RoleName.ADMIN, persistClinic("real", false), true);
        persistUser("legacy_admin", Users.RoleName.ADMIN, persistClinic("legacy", null), true);
        entityManager.flush();
        entityManager.clear();

        Optional<Users> result =
                usersRepository.findFirstByClinic_IsDemoTrueAndRoleNameAndEnabledTrueOrderByIdAsc(Users.RoleName.ADMIN);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldSkipDisabledDemoUser_whenFindingDemoUserByRole() {
        persistUser("demo_admin", Users.RoleName.ADMIN, persistClinic("demo", true), false);
        entityManager.flush();
        entityManager.clear();

        Optional<Users> result =
                usersRepository.findFirstByClinic_IsDemoTrueAndRoleNameAndEnabledTrueOrderByIdAsc(Users.RoleName.ADMIN);

        assertFalse(result.isPresent());
    }

    private Users persistUser(String username, Users.RoleName roleName, Long clinicId) {
        return persistUser(username, roleName, persistClinic("clinic-" + clinicId, false), true);
    }

    private Clinics persistClinic(String slug, Boolean isDemo) {
        Clinics clinic = new Clinics();
        clinic.setName("Clinic " + slug);
        clinic.setSlug(slug);
        clinic.setIsDemo(isDemo);
        clinic.setCreatedAt(LocalDateTime.of(2026, 3, 1, 9, 0));
        entityManager.persist(clinic);
        return clinic;
    }

    private Users persistUser(String username, Users.RoleName roleName, Clinics clinic, boolean enabled) {
        Users user = new Users();
        user.setUsername(username);
        user.setPasswordHash("secret1");
        user.setEnabled(enabled);
        user.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
        user.setRoleName(roleName);
        user.setClinic(clinic);
        entityManager.persist(user);
        return user;
    }
}
