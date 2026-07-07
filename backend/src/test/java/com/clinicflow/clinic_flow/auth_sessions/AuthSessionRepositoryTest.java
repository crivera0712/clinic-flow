package com.clinicflow.clinic_flow.auth_sessions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.users.Users;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class AuthSessionRepositoryTest {

    @Autowired
    private AuthSessionRepository authSessionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnEmptyWhenSessionIsQueriedFromAnotherClinic() {
        Clinics clinicOne = persistClinic("clinic-one");
        Clinics clinicTwo = persistClinic("clinic-two");
        Users user = persistUser("sam", clinicOne);
        AuthSessions session = persistSession(user, clinicOne);
        entityManager.flush();
        entityManager.clear();

        Optional<AuthSessions> result = authSessionRepository.findByIdAndClinicId(session.getId(), clinicTwo.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnSessionWhenClinicMatches() {
        Clinics clinicOne = persistClinic("clinic-one");
        Users user = persistUser("sam", clinicOne);
        AuthSessions session = persistSession(user, clinicOne);
        entityManager.flush();
        entityManager.clear();

        Optional<AuthSessions> result = authSessionRepository.findByIdAndClinicId(session.getId(), clinicOne.getId());

        assertTrue(result.isPresent());
    }

    private Clinics persistClinic(String slug) {
        Clinics clinic = new Clinics();
        clinic.setName(slug);
        clinic.setSlug(slug);
        clinic.setIsDemo(false);
        clinic.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
        entityManager.persist(clinic);
        return clinic;
    }

    private Users persistUser(String username, Clinics clinic) {
        Users user = new Users();
        user.setUsername(username);
        user.setPasswordHash("encoded");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 30));
        user.setRoleName(Users.RoleName.ADMIN);
        user.setClinic(clinic);
        entityManager.persist(user);
        return user;
    }

    private AuthSessions persistSession(Users user, Clinics clinic) {
        AuthSessions session = new AuthSessions();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setClinic(clinic);
        session.setCreatedAt(LocalDateTime.of(2026, 3, 1, 11, 0));
        session.setRefreshExpiresAt(LocalDateTime.of(2026, 3, 2, 11, 0));
        entityManager.persist(session);
        return session;
    }
}
