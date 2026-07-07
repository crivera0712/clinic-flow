package com.clinicflow.clinic_flow.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.clinicflow.clinic_flow.clinics.Clinics;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnPatientsMatchingOneTokenWithinClinic() {
        Clinics clinicOne = persistClinic("clinic-one");
        Clinics clinicTwo = persistClinic("clinic-two");
        persistPatient(clinicOne, "Sam", "Lee");
        persistPatient(clinicOne, "Samuel", "Kent");
        persistPatient(clinicTwo, "Sam", "Other");
        entityManager.flush();
        entityManager.clear();

        List<Patient> result = patientRepository.searchPatientByOneToken("Sam", clinicOne.getId());

        assertEquals(2, result.size());
        assertTrue(
                result.stream().allMatch(patient -> patient.getClinic().getId().equals(clinicOne.getId())));
    }

    @Test
    void shouldReturnPatientsMatchingTwoTokensWithinClinic() {
        Clinics clinic = persistClinic("clinic-one");
        Patient match = persistPatient(clinic, "Sam", "Lee");
        persistPatient(clinic, "Alex", "Lee");
        entityManager.flush();
        entityManager.clear();

        List<Patient> result = patientRepository.searchPatientByTwoTokens("Sam", "Lee", clinic.getId());

        assertEquals(1, result.size());
        assertEquals(match.getId(), result.get(0).getId());
    }

    @Test
    void shouldReturnPatientByIdWithinClinic() {
        Clinics clinic = persistClinic("clinic-one");
        Patient saved = persistPatient(clinic, "Sam", "Lee");
        entityManager.flush();
        entityManager.clear();

        Optional<Patient> result = patientRepository.findByIdAndClinicId(saved.getId(), clinic.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
    }

    @Test
    void shouldNotReturnPatientByIdFromAnotherClinic() {
        Clinics clinic = persistClinic("clinic-one");
        Clinics otherClinic = persistClinic("clinic-two");
        Patient saved = persistPatient(clinic, "Sam", "Lee");
        entityManager.flush();
        entityManager.clear();

        Optional<Patient> result = patientRepository.findByIdAndClinicId(saved.getId(), otherClinic.getId());

        assertFalse(result.isPresent());
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

    private Patient persistPatient(Clinics clinic, String firstName, String lastName) {
        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setClinic(clinic);
        entityManager.persist(patient);
        return patient;
    }
}
