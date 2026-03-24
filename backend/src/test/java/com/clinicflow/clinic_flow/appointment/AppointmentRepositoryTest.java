package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.body_region.BodyRegion;
import com.clinicflow.clinic_flow.cases.Case;
import com.clinicflow.clinic_flow.patient.Patient;
import com.clinicflow.clinic_flow.therapist.Therapist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
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
    void shouldReturnDailyAppointmentsOrderedByScheduledAt_whenFindDailyAppointmentsMatchesAppointments() {
        // Arrange
        AppointmentFixture fixture = persistFixtureGraph();
        Appointment earlyAppointment = persistAppointment(
                fixture,
                LocalDateTime.of(2026, 2, 13, 9, 0),
                Appointment.Status.SCHEDULED,
                Instant.parse("2026-02-01T12:00:00Z")
        );
        Appointment lateAppointment = persistAppointment(
                fixture,
                LocalDateTime.of(2026, 2, 13, 11, 0),
                Appointment.Status.CHECKED_IN,
                Instant.parse("2026-02-01T13:00:00Z")
        );
        persistAppointment(
                fixture,
                LocalDateTime.of(2026, 2, 14, 9, 0),
                Appointment.Status.FINISHED,
                Instant.parse("2026-02-01T14:00:00Z")
        );
        entityManager.flush();
        entityManager.clear();

        // Act
        List<AppointmentScheduleProjection> result =
                appointmentRepository.findDailyAppointments(LocalDate.of(2026, 2, 13));

        // Assert
        assertEquals(2, result.size());
        assertEquals(earlyAppointment.getId(), result.get(0).getAptId());
        assertEquals(LocalDateTime.of(2026, 2, 13, 9, 0), result.get(0).getScheduledAt());
        assertEquals(fixture.ptCase().getId(), result.get(0).getCaseId());
        assertEquals("Sam", result.get(0).getFirstName());
        assertEquals("Lee", result.get(0).getLastName());
        assertEquals(fixture.therapist().getId(), result.get(0).getTherapistId());
        assertEquals("Taylor", result.get(0).getTherapistName());
        assertEquals("PHYSICAL_THERAPIST", result.get(0).getTherapistType());
        assertEquals("Shoulder", result.get(0).getBodyRegionDisplayName());
        assertEquals(Appointment.Status.SCHEDULED, result.get(0).getStatus());
        assertEquals(lateAppointment.getId(), result.get(1).getAptId());
        assertEquals(LocalDateTime.of(2026, 2, 13, 11, 0), result.get(1).getScheduledAt());
        assertEquals(Appointment.Status.CHECKED_IN, result.get(1).getStatus());
    }

    @Test
    void shouldReturnEmptyList_whenFindDailyAppointmentsDoesNotMatchAppointments() {
        // Arrange
        AppointmentFixture fixture = persistFixtureGraph();
        persistAppointment(
                fixture,
                LocalDateTime.of(2026, 2, 13, 9, 0),
                Appointment.Status.SCHEDULED,
                Instant.parse("2026-02-01T12:00:00Z")
        );
        entityManager.flush();
        entityManager.clear();

        // Act
        List<AppointmentScheduleProjection> result =
                appointmentRepository.findDailyAppointments(LocalDate.of(2026, 2, 15));

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnAppointment_whenFindByScheduledAtMatchesExistingAppointment() {
        // Arrange
        AppointmentFixture fixture = persistFixtureGraph();
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 2, 13, 9, 0);
        Appointment appointment = persistAppointment(
                fixture,
                scheduledAt,
                Appointment.Status.SCHEDULED,
                Instant.parse("2026-02-01T12:00:00Z")
        );
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Appointment> result = appointmentRepository.findByScheduledAt(scheduledAt);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(appointment.getId(), result.get().getId());
        assertEquals(scheduledAt, result.get().getScheduledAt());
    }

    @Test
    void shouldReturnEmptyOptional_whenFindByScheduledAtDoesNotMatchExistingAppointment() {
        // Arrange
        AppointmentFixture fixture = persistFixtureGraph();
        persistAppointment(
                fixture,
                LocalDateTime.of(2026, 2, 13, 9, 0),
                Appointment.Status.SCHEDULED,
                Instant.parse("2026-02-01T12:00:00Z")
        );
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Appointment> result =
                appointmentRepository.findByScheduledAt(LocalDateTime.of(2026, 2, 13, 10, 0));

        // Assert
        assertFalse(result.isPresent());
    }

    private AppointmentFixture persistFixtureGraph() {
        Patient patient = new Patient();
        patient.setFirstName("Sam");
        patient.setLastName("Lee");
        entityManager.persist(patient);

        BodyRegion bodyRegion = new BodyRegion();
        bodyRegion.setCode("SHOULDER");
        bodyRegion.setDisplayName("Shoulder");
        bodyRegion.setIsActive(true);
        entityManager.persist(bodyRegion);

        Case ptCase = new Case();
        ptCase.setCreatedAt(new Date());
        ptCase.setPatient(patient);
        ptCase.setBodyRegion(bodyRegion);
        entityManager.persist(ptCase);

        Therapist therapist = new Therapist();
        therapist.setTherapistName("Taylor");
        therapist.setType(Therapist.TherapistType.PHYSICAL_THERAPIST);
        entityManager.persist(therapist);

        return new AppointmentFixture(patient, bodyRegion, ptCase, therapist);
    }

    private Appointment persistAppointment(
            AppointmentFixture fixture,
            LocalDateTime scheduledAt,
            Appointment.Status status,
            Instant createdAt
    ) {
        Appointment appointment = new Appointment();
        appointment.setScheduledAt(scheduledAt);
        appointment.setStatus(status);
        appointment.setCreatedAt(createdAt);
        appointment.setPtCase(fixture.ptCase());
        appointment.setTherapist(fixture.therapist());
        entityManager.persist(appointment);
        return appointment;
    }

    private record AppointmentFixture(
            Patient patient,
            BodyRegion bodyRegion,
            Case ptCase,
            Therapist therapist
    ) {
    }
}
