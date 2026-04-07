package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.body_region.BodyRegion;
import com.clinicflow.clinic_flow.cases.Case;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.patient.Patient;
import com.clinicflow.clinic_flow.therapist.Therapist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
    void shouldReturnDailyAppointmentsOrderedByScheduledAtWithinClinic() {
        AppointmentFixture fixture = persistFixtureGraph("clinic-one");
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
        AppointmentFixture otherFixture = persistFixtureGraph("clinic-two");
        persistAppointment(
                otherFixture,
                LocalDateTime.of(2026, 2, 13, 10, 0),
                Appointment.Status.FINISHED,
                Instant.parse("2026-02-01T14:00:00Z")
        );
        entityManager.flush();
        entityManager.clear();

        List<AppointmentScheduleProjection> result =
                appointmentRepository.findDailyAppointments(LocalDate.of(2026, 2, 13), fixture.clinic().getId());

        assertEquals(2, result.size());
        assertEquals(earlyAppointment.getId(), result.get(0).getAptId());
        assertEquals(lateAppointment.getId(), result.get(1).getAptId());
    }

    @Test
    void shouldReturnAppointmentByScheduledAtClinicAndTherapist() {
        AppointmentFixture fixture = persistFixtureGraph("clinic-one");
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 2, 13, 9, 0);
        Appointment appointment = persistAppointment(
                fixture,
                scheduledAt,
                Appointment.Status.SCHEDULED,
                Instant.parse("2026-02-01T12:00:00Z")
        );
        entityManager.flush();
        entityManager.clear();

        Optional<Appointment> result = appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(
                scheduledAt,
                fixture.clinic().getId(),
                fixture.therapist().getId()
        );

        assertTrue(result.isPresent());
        assertEquals(appointment.getId(), result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenClinicDoesNotMatch() {
        AppointmentFixture fixture = persistFixtureGraph("clinic-one");
        persistAppointment(
                fixture,
                LocalDateTime.of(2026, 2, 13, 9, 0),
                Appointment.Status.SCHEDULED,
                Instant.parse("2026-02-01T12:00:00Z")
        );
        entityManager.flush();
        entityManager.clear();

        Optional<Appointment> result = appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(
                LocalDateTime.of(2026, 2, 13, 9, 0),
                999L,
                fixture.therapist().getId()
        );

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnOnlyClinicAppointmentsForDateRangeFilter() {
        AppointmentFixture clinicOne = persistFixtureGraph("clinic-one");
        AppointmentFixture clinicTwo = persistFixtureGraph("clinic-two");
        Appointment first = persistAppointment(
                clinicOne,
                LocalDateTime.of(2026, 2, 13, 9, 0),
                Appointment.Status.SCHEDULED,
                Instant.parse("2026-02-01T12:00:00Z")
        );
        persistAppointment(
                clinicTwo,
                LocalDateTime.of(2026, 2, 13, 10, 0),
                Appointment.Status.SCHEDULED,
                Instant.parse("2026-02-01T12:30:00Z")
        );
        entityManager.flush();
        entityManager.clear();

        Page<Appointment> result = appointmentRepository.findByClinicIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                clinicOne.clinic().getId(),
                LocalDate.of(2026, 2, 13).atStartOfDay(),
                LocalDate.of(2026, 2, 14).atStartOfDay(),
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(first.getId(), result.getContent().get(0).getId());
    }

    @Test
    void shouldReturnOnlyClinicAppointmentsForCaseFilter() {
        AppointmentFixture clinicOne = persistFixtureGraph("clinic-one");
        AppointmentFixture clinicTwo = persistFixtureGraph("clinic-two");
        Appointment first = persistAppointment(
                clinicOne,
                LocalDateTime.of(2026, 2, 13, 9, 0),
                Appointment.Status.SCHEDULED,
                Instant.parse("2026-02-01T12:00:00Z")
        );
        persistAppointment(
                clinicTwo,
                LocalDateTime.of(2026, 2, 13, 10, 0),
                Appointment.Status.SCHEDULED,
                Instant.parse("2026-02-01T12:30:00Z")
        );
        entityManager.flush();
        entityManager.clear();

        Page<Appointment> result = appointmentRepository.findByClinicIdAndPtCaseIdOrderByScheduledAtDesc(
                clinicOne.clinic().getId(),
                clinicOne.ptCase().getId(),
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(first.getId(), result.getContent().get(0).getId());
    }

    private AppointmentFixture persistFixtureGraph(String slug) {
        Clinics clinic = new Clinics();
        clinic.setName(slug);
        clinic.setSlug(slug);
        clinic.setIsDemo(false);
        clinic.setCreatedAt(java.time.LocalDateTime.of(2026, 3, 1, 10, 0));
        entityManager.persist(clinic);

        Patient patient = new Patient();
        patient.setFirstName("Sam");
        patient.setLastName("Lee");
        patient.setDisplayName("Lee, S");
        patient.setClinic(clinic);
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
        ptCase.setClinic(clinic);
        entityManager.persist(ptCase);

        Therapist therapist = new Therapist();
        therapist.setTherapistName("Taylor");
        therapist.setType(Therapist.TherapistType.PHYSICAL_THERAPIST);
        therapist.setClinic(clinic);
        entityManager.persist(therapist);

        return new AppointmentFixture(clinic, patient, bodyRegion, ptCase, therapist);
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
        appointment.setClinic(fixture.clinic());
        entityManager.persist(appointment);
        return appointment;
    }

    private record AppointmentFixture(
            Clinics clinic,
            Patient patient,
            BodyRegion bodyRegion,
            Case ptCase,
            Therapist therapist
    ) {
    }
}
