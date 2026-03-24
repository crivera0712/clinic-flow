package com.clinicflow.clinic_flow.patient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnPatientsMatchingFirstNamePrefix_whenSearchPatientByOneTokenUsesFirstName() {
        // Arrange
        persistPatient("Sam", "Lee");
        persistPatient("Samuel", "Kent");
        persistPatient("Alex", "Smith");
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Patient> result = patientRepository.searchPatientByOneToken("Sam");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(patient -> patient.getFirstName().startsWith("Sam")
                || patient.getLastName().startsWith("Sam")));
    }

    @Test
    void shouldReturnPatientsMatchingLastNamePrefix_whenSearchPatientByOneTokenUsesLastName() {
        // Arrange
        persistPatient("Sam", "Lee");
        persistPatient("Alex", "Lewis");
        persistPatient("Morgan", "Kim");
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Patient> result = patientRepository.searchPatientByOneToken("Le");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(patient -> patient.getFirstName().startsWith("Le")
                || patient.getLastName().startsWith("Le")));
    }

    @Test
    void shouldReturnEmptyList_whenSearchPatientByOneTokenHasNoMatches() {
        // Arrange
        persistPatient("Sam", "Lee");
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Patient> result = patientRepository.searchPatientByOneToken("Zo");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUsePrefixSemanticsOnly_whenSearchPatientByOneTokenReceivesSubstring() {
        // Arrange
        persistPatient("Sam", "Lee");
        persistPatient("Alex", "Smith");
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Patient> result = patientRepository.searchPatientByOneToken("am");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnPatients_whenSearchPatientByTwoTokensMatchesFirstAndLastName() {
        // Arrange
        Patient match = persistPatient("Sam", "Lee");
        persistPatient("Sam", "Kent");
        persistPatient("Alex", "Lee");
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Patient> result = patientRepository.searchPatientByTwoTokens("Sam", "Lee");

        // Assert
        assertEquals(1, result.size());
        assertEquals(match.getId(), result.get(0).getId());
    }

    @Test
    void shouldReturnPatients_whenSearchPatientByTwoTokensReceivesReversedTokens() {
        // Arrange
        Patient match = persistPatient("Sam", "Lee");
        persistPatient("Sam", "Kent");
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Patient> result = patientRepository.searchPatientByTwoTokens("Lee", "Sam");

        // Assert
        assertEquals(1, result.size());
        assertEquals(match.getId(), result.get(0).getId());
    }

    @Test
    void shouldReturnEmptyList_whenSearchPatientByTwoTokensMatchesOnlyOneSide() {
        // Arrange
        persistPatient("Sam", "Kent");
        persistPatient("Alex", "Lee");
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Patient> result = patientRepository.searchPatientByTwoTokens("Sam", "Lee");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnPatient_whenGetPatientByIdMatchesExistingPatient() {
        // Arrange
        Patient savedPatient = persistPatient("Sam", "Lee");
        entityManager.flush();
        entityManager.clear();

        // Act
        Patient result = patientRepository.getPatientById(savedPatient.getId());

        // Assert
        assertEquals(savedPatient.getId(), result.getId());
        assertEquals("Sam", result.getFirstName());
        assertEquals("Lee", result.getLastName());
    }

    @Test
    void shouldReturnNull_whenGetPatientByIdDoesNotMatchExistingPatient() {
        // Arrange
        persistPatient("Sam", "Lee");
        entityManager.flush();
        entityManager.clear();

        // Act
        Patient result = patientRepository.getPatientById(999L);

        // Assert
        assertNull(result);
    }

    private Patient persistPatient(String firstName, String lastName) {
        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        entityManager.persist(patient);
        return patient;
    }
}
