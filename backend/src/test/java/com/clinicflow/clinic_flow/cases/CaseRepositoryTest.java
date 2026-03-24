package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.body_region.BodyRegion;
import com.clinicflow.clinic_flow.patient.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CaseRepositoryTest {

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnCasesOrderedByCreatedAt_whenGetCaseByPatientMatchesCases() {
        // Arrange
        Patient firstPatient = persistPatient("Sam", "Lee");
        Patient secondPatient = persistPatient("Alex", "Kim");
        BodyRegion shoulder = persistBodyRegion("SHOULDER", "Shoulder");
        BodyRegion knee = persistBodyRegion("KNEE", "Knee");
        Case earlyCase = persistCase(firstPatient, shoulder, Date.from(Instant.parse("2026-03-01T10:00:00Z")));
        Case lateCase = persistCase(firstPatient, knee, Date.from(Instant.parse("2026-03-02T10:00:00Z")));
        persistCase(secondPatient, shoulder, Date.from(Instant.parse("2026-03-03T10:00:00Z")));
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Case> result = caseRepository.getCaseByPatient(firstPatient.getId());

        // Assert
        assertEquals(2, result.size());
        assertEquals(earlyCase.getId(), result.get(0).getId());
        assertEquals(lateCase.getId(), result.get(1).getId());
        assertEquals(firstPatient.getId(), result.get(0).getPatient().getId());
        assertEquals(firstPatient.getId(), result.get(1).getPatient().getId());
    }

    @Test
    void shouldReturnEmptyList_whenGetCaseByPatientDoesNotMatchCases() {
        // Arrange
        Patient patient = persistPatient("Sam", "Lee");
        BodyRegion shoulder = persistBodyRegion("SHOULDER", "Shoulder");
        persistCase(patient, shoulder, Date.from(Instant.parse("2026-03-01T10:00:00Z")));
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Case> result = caseRepository.getCaseByPatient(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnCase_whenFindByIdMatchesExistingCase() {
        // Arrange
        Patient patient = persistPatient("Sam", "Lee");
        BodyRegion shoulder = persistBodyRegion("SHOULDER", "Shoulder");
        Case savedCase = persistCase(patient, shoulder, Date.from(Instant.parse("2026-03-01T10:00:00Z")));
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Case> result = caseRepository.findById(savedCase.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(savedCase.getId(), result.get().getId());
        assertEquals(patient.getId(), result.get().getPatient().getId());
        assertEquals(shoulder.getId(), result.get().getBodyRegion().getId());
    }

    @Test
    void shouldReturnEmptyOptional_whenFindByIdDoesNotMatchExistingCase() {
        // Arrange
        Patient patient = persistPatient("Sam", "Lee");
        BodyRegion shoulder = persistBodyRegion("SHOULDER", "Shoulder");
        persistCase(patient, shoulder, Date.from(Instant.parse("2026-03-01T10:00:00Z")));
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Case> result = caseRepository.findById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    private Patient persistPatient(String firstName, String lastName) {
        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        entityManager.persist(patient);
        return patient;
    }

    private BodyRegion persistBodyRegion(String code, String displayName) {
        BodyRegion bodyRegion = new BodyRegion();
        bodyRegion.setCode(code);
        bodyRegion.setDisplayName(displayName);
        bodyRegion.setIsActive(true);
        entityManager.persist(bodyRegion);
        return bodyRegion;
    }

    private Case persistCase(Patient patient, BodyRegion bodyRegion, Date createdAt) {
        Case caseEntity = new Case();
        caseEntity.setPatient(patient);
        caseEntity.setBodyRegion(bodyRegion);
        caseEntity.setCreatedAt(createdAt);
        entityManager.persist(caseEntity);
        return caseEntity;
    }
}
