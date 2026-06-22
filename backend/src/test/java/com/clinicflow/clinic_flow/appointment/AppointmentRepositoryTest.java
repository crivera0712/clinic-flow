package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.patient.Patient;
import com.clinicflow.clinic_flow.therapist.Therapist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findDailyAppointments_returnsClinicRowsOrderedByTime_withJoinedNames() {
        Fixture fixture = persistFixture("clinic-one");
        Appointment early = persistAppointment(fixture, LocalDateTime.of(2026, 2, 13, 9, 0), Appointment.Status.SCHEDULED);
        Appointment late = persistAppointment(fixture, LocalDateTime.of(2026, 2, 13, 11, 0), Appointment.Status.WAITING);

        Fixture other = persistFixture("clinic-two");
        persistAppointment(other, LocalDateTime.of(2026, 2, 13, 10, 0), Appointment.Status.SCHEDULED);

        entityManager.flush();
        entityManager.clear();

        List<AppointmentBoardProjection> result =
                appointmentRepository.findDailyAppointments(LocalDate.of(2026, 2, 13), fixture.clinic.getId());

        assertEquals(2, result.size());
        assertEquals(early.getId(), result.get(0).getId());
        assertEquals(late.getId(), result.get(1).getId());
        assertEquals("Sam Lee", result.get(0).getPatientName());
        assertEquals("Taylor", result.get(0).getTherapistName());
        assertEquals(Appointment.Status.SCHEDULED, result.get(0).getStatus());
        assertEquals(Appointment.Type.EVALUATION, result.get(0).getType());
    }

    @Test
    void findByScheduledAtAndClinicIdAndTherapistId_matchesAndIsClinicScoped() {
        Fixture fixture = persistFixture("clinic-one");
        LocalDateTime at = LocalDateTime.of(2026, 2, 13, 9, 0);
        Appointment appointment = persistAppointment(fixture, at, Appointment.Status.SCHEDULED);
        entityManager.flush();
        entityManager.clear();

        Optional<Appointment> found = appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(
                at, fixture.clinic.getId(), fixture.therapist.getId());
        assertTrue(found.isPresent());
        assertEquals(appointment.getId(), found.get().getId());

        Optional<Appointment> wrongClinic = appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(
                at, 999L, fixture.therapist.getId());
        assertFalse(wrongClinic.isPresent());
    }

    @Test
    void existsPatientAppointmentConflict_detectsSamePatientAndTime() {
        Fixture fixture = persistFixture("clinic-one");
        LocalDateTime at = LocalDateTime.of(2026, 2, 13, 9, 0);
        Appointment appointment = persistAppointment(fixture, at, Appointment.Status.SCHEDULED);
        entityManager.flush();
        entityManager.clear();

        assertTrue(appointmentRepository.existsPatientAppointmentConflict(
                fixture.clinic.getId(), fixture.patient.getId(), at));
        assertFalse(appointmentRepository.existsPatientAppointmentConflict(
                fixture.clinic.getId(), fixture.patient.getId(), at.plusHours(1)));
        assertFalse(appointmentRepository.existsPatientAppointmentConflictExcludingAppointment(
                fixture.clinic.getId(), fixture.patient.getId(), at, appointment.getId()));
    }

    private Fixture persistFixture(String slug) {
        Clinics clinic = new Clinics();
        clinic.setName(slug);
        clinic.setSlug(slug);
        clinic.setIsDemo(false);
        clinic.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
        entityManager.persist(clinic);

        Patient patient = new Patient();
        patient.setFirstName("Sam");
        patient.setLastName("Lee");
        patient.setClinic(clinic);
        entityManager.persist(patient);

        Therapist therapist = new Therapist();
        therapist.setTherapistName("Taylor");
        therapist.setClinic(clinic);
        entityManager.persist(therapist);

        return new Fixture(clinic, patient, therapist);
    }

    private Appointment persistAppointment(Fixture fixture, LocalDateTime scheduledAt, Appointment.Status status) {
        Appointment appointment = new Appointment();
        appointment.setScheduledAt(scheduledAt);
        appointment.setStatus(status);
        appointment.setType(Appointment.Type.EVALUATION);
        appointment.setCreatedAt(Instant.now());
        appointment.setPatient(fixture.patient);
        appointment.setTherapist(fixture.therapist);
        appointment.setClinic(fixture.clinic);
        entityManager.persist(appointment);
        return appointment;
    }

    private record Fixture(Clinics clinic, Patient patient, Therapist therapist) {
    }
}
