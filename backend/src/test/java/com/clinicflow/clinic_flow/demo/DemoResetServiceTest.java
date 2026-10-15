package com.clinicflow.clinic_flow.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinicflow.clinic_flow.appointment.Appointment;
import com.clinicflow.clinic_flow.appointment.AppointmentRepository;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.clinics.ClinicsRepository;
import com.clinicflow.clinic_flow.patient.PatientRepository;
import com.clinicflow.clinic_flow.therapist.Therapist;
import com.clinicflow.clinic_flow.therapist.TherapistRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DemoResetServiceTest {

    private static final ZoneId LA = ZoneId.of("America/Los_Angeles");

    // A clock the test can move forward.
    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(now, zone);
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    @Test
    void slotsStartAboutThreeHoursBackOnAHalfHour() {
        List<LocalDateTime> slots = DemoResetService.buildSlots(LocalDateTime.of(2026, 10, 14, 13, 10), 10);

        assertEquals(LocalDateTime.of(2026, 10, 14, 10, 0), slots.get(0));
        assertEquals(LocalDateTime.of(2026, 10, 14, 10, 45), slots.get(1));
        assertEquals(LocalDateTime.of(2026, 10, 14, 16, 45), slots.get(9));
    }

    @Test
    void slotsStayInsideTodayJustAfterMidnight() {
        List<LocalDateTime> slots = DemoResetService.buildSlots(LocalDateTime.of(2026, 10, 14, 0, 5), 10);

        assertEquals(LocalDateTime.of(2026, 10, 14, 0, 0), slots.get(0));
        assertTrue(slots.stream()
                .allMatch(slot -> slot.toLocalDate().equals(slots.get(0).toLocalDate())));
    }

    @Test
    void slotsStayInsideTodayLateAtNight() {
        List<LocalDateTime> slots = DemoResetService.buildSlots(LocalDateTime.of(2026, 10, 14, 23, 40), 10);

        assertEquals(LocalDateTime.of(2026, 10, 14, 23, 30), slots.get(9));
        assertTrue(slots.stream().allMatch(slot -> slot.getDayOfMonth() == 14));
    }

    @Test
    void statusFollowsTheVisitTime() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 14, 13, 0);

        assertEquals(Appointment.Status.DONE, DemoResetService.computeStatus(now.minusHours(1), now));
        assertEquals(Appointment.Status.WAITING, DemoResetService.computeStatus(now.minusMinutes(20), now));
        assertEquals(Appointment.Status.WAITING, DemoResetService.computeStatus(now.plusMinutes(25), now));
        assertEquals(Appointment.Status.SCHEDULED, DemoResetService.computeStatus(now.plusHours(1), now));
    }

    @Test
    void statusIsCorrectAroundMidnight() {
        LocalDateTime justAfterMidnight = LocalDateTime.of(2026, 10, 14, 0, 5);

        // A midnight visit is in progress, not "scheduled for 23:30 yesterday".
        assertEquals(
                Appointment.Status.WAITING,
                DemoResetService.computeStatus(LocalDateTime.of(2026, 10, 14, 0, 0), justAfterMidnight));
        assertEquals(
                Appointment.Status.SCHEDULED,
                DemoResetService.computeStatus(LocalDateTime.of(2026, 10, 14, 6, 0), justAfterMidnight));
    }

    @Test
    void midDayResetSeedsAMixOfFinishedWaitingAndUpcomingVisits() {
        Fixture fixture =
                new Fixture(ZonedDateTime.of(2026, 10, 14, 13, 10, 0, 0, LA).toInstant());

        fixture.service.resetDemoData();

        List<Appointment> seeded = fixture.savedAppointments();
        assertEquals(10, seeded.size());
        assertTrue(seeded.stream().anyMatch(a -> a.getStatus() == Appointment.Status.DONE));
        assertTrue(seeded.stream().anyMatch(a -> a.getStatus() == Appointment.Status.WAITING));
        assertTrue(seeded.stream().anyMatch(a -> a.getStatus() == Appointment.Status.SCHEDULED));
    }

    @Test
    void loginResetRunsAtMostOncePerInterval() {
        Fixture fixture = new Fixture(Instant.parse("2026-10-14T20:00:00Z"));

        assertTrue(fixture.service.resetDemoDataIfStale());
        fixture.clock.advance(Duration.ofMinutes(10));
        assertFalse(fixture.service.resetDemoDataIfStale());
        fixture.clock.advance(Duration.ofMinutes(6));
        assertTrue(fixture.service.resetDemoDataIfStale());

        verify(fixture.appointments, times(2)).deleteAllByClinicId(5L);
    }

    private static final class Fixture {
        final MutableClock clock;
        final AppointmentRepository appointments = mock(AppointmentRepository.class);
        final DemoResetService service;

        Fixture(Instant now) {
            clock = new MutableClock(now);
            ClinicsRepository clinics = mock(ClinicsRepository.class);
            TherapistRepository therapists = mock(TherapistRepository.class);
            PatientRepository patients = mock(PatientRepository.class);

            Clinics demo = new Clinics();
            demo.setId(5L);
            when(clinics.findClinicsBySlug("demo")).thenReturn(Optional.of(demo));
            when(therapists.findAllByClinicId(5L)).thenReturn(List.of(new Therapist(), new Therapist()));
            when(patients.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            service = new DemoResetService(clinics, therapists, patients, appointments, clock);
        }

        @SuppressWarnings("unchecked")
        List<Appointment> savedAppointments() {
            var captor = org.mockito.ArgumentCaptor.forClass(List.class);
            verify(appointments).saveAll(captor.capture());
            return (List<Appointment>) captor.getValue();
        }
    }
}
