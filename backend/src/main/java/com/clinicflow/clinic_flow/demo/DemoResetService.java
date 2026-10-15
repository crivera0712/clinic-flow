package com.clinicflow.clinic_flow.demo;

import com.clinicflow.clinic_flow.appointment.Appointment;
import com.clinicflow.clinic_flow.appointment.AppointmentRepository;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.clinics.ClinicsRepository;
import com.clinicflow.clinic_flow.patient.Patient;
import com.clinicflow.clinic_flow.patient.PatientRepository;
import com.clinicflow.clinic_flow.therapist.Therapist;
import com.clinicflow.clinic_flow.therapist.TherapistRepository;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemoResetService {

    private static final ZoneId LA = ZoneId.of("America/Los_Angeles");
    private static final String DEMO_SLUG = "demo";

    // A demo admin login resets the demo at most this often, so each visitor starts from a fresh,
    // time-correct day while a second visitor a few minutes later doesn't wipe the first one's
    // check-ins.
    static final Duration LOGIN_RESET_INTERVAL = Duration.ofMinutes(15);

    // Visits are spaced this far apart, starting about 3 hours before now.
    static final Duration SLOT_GAP = Duration.ofMinutes(45);
    static final Duration VISIT_LENGTH = Duration.ofMinutes(45);

    private final ClinicsRepository clinicsRepository;
    private final TherapistRepository therapistRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    private final AtomicReference<Instant> lastReset = new AtomicReference<>();

    // Called when a demo admin signs in. Returns true if it reset the demo.
    @Transactional
    public synchronized boolean resetDemoDataIfStale() {
        Instant last = lastReset.get();
        if (last != null && Duration.between(last, clock.instant()).compareTo(LOGIN_RESET_INTERVAL) < 0) {
            return false;
        }
        resetDemoData();
        return true;
    }

    @Transactional
    public void resetDemoData() {
        Optional<Clinics> demoOpt = clinicsRepository.findClinicsBySlug(DEMO_SLUG);
        if (demoOpt.isEmpty()) {
            log.warn("DemoResetService: demo clinic not found, skipping reset");
            return;
        }
        Clinics clinic = demoOpt.get();
        Long clinicId = clinic.getId();

        List<Therapist> therapists = therapistRepository.findAllByClinicId(clinicId);
        if (therapists.isEmpty()) {
            log.error("DemoResetService: no therapists for demo clinic (clinicId={}), skipping reset", clinicId);
            return;
        }

        // Delete in FK-safe order: appointments → patients
        appointmentRepository.deleteAllByClinicId(clinicId);
        patientRepository.deleteAllByClinicId(clinicId);

        List<Patient> patients = patientRepository.saveAll(buildPatients(clinic));
        appointmentRepository.saveAll(buildAppointments(patients, therapists, clinic));
        lastReset.set(clock.instant());

        log.info("DemoResetService: reset complete for demo clinic (clinicId={})", clinicId);
    }

    private List<Patient> buildPatients(Clinics clinic) {
        String[][] data = {
            {"Marcus", "Webb"}, {"Priya", "Nair"}, {"Daniel", "Cho"}, {"Sofia", "Reyes"}, {"Jordan", "Flores"},
            {"Aaliyah", "Brooks"}, {"Ethan", "Park"}, {"Camila", "Torres"}, {"Noah", "Schmidt"}, {"Leila", "Hassan"},
        };
        List<Patient> patients = new ArrayList<>();
        for (String[] row : data) {
            Patient p = new Patient();
            p.setFirstName(row[0]);
            p.setLastName(row[1]);
            p.setClinic(clinic);
            patients.add(p);
        }
        return patients;
    }

    private List<Appointment> buildAppointments(List<Patient> patients, List<Therapist> therapists, Clinics clinic) {
        LocalDateTime now = LocalDateTime.now(clock.withZone(LA));
        List<LocalDateTime> slots = buildSlots(now, patients.size());

        Appointment.Type[] types = {
            Appointment.Type.EVALUATION, Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
            Appointment.Type.REASSESSMENT, Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
            Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
            Appointment.Type.EVALUATION,
        };

        List<Appointment> appointments = new ArrayList<>();
        for (int i = 0; i < patients.size(); i++) {
            LocalDateTime scheduledAt = slots.get(i);
            Appointment a = new Appointment();
            a.setScheduledAt(scheduledAt);
            a.setStatus(computeStatus(scheduledAt, now));
            a.setType(types[i % types.length]);
            a.setPatient(patients.get(i));
            a.setTherapist(therapists.get(i % therapists.size()));
            a.setClinic(clinic);
            a.setCreatedAt(Instant.now(clock));
            a.setModifiedAt(Instant.now(clock));
            appointments.add(a);
        }
        return appointments;
    }

    // Visit times around `now` (starting about 3 hours back, on a half hour), so whenever someone
    // opens the demo it shows finished, waiting and upcoming visits. The window is shifted to stay
    // inside today, since the schedule shows one day.
    static List<LocalDateTime> buildSlots(LocalDateTime now, int count) {
        LocalDate today = now.toLocalDate();
        LocalDateTime earliest = today.atStartOfDay();
        LocalDateTime latest = today.atTime(23, 30).minus(SLOT_GAP.multipliedBy(Math.max(0, count - 1)));

        LocalDateTime start = now.minusHours(3).withSecond(0).withNano(0);
        start = start.withMinute(start.getMinute() < 30 ? 0 : 30);
        if (start.isBefore(earliest)) start = earliest;
        if (start.isAfter(latest)) start = latest;

        List<LocalDateTime> slots = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            slots.add(start.plus(SLOT_GAP.multipliedBy(i)));
        }
        return slots;
    }

    // Finished if the visit has ended; waiting from 30 minutes before the start until it ends;
    // otherwise scheduled. Compares full date-times, so it stays correct around midnight.
    static Appointment.Status computeStatus(LocalDateTime scheduledAt, LocalDateTime now) {
        if (scheduledAt.plus(VISIT_LENGTH).isBefore(now)) {
            return Appointment.Status.DONE;
        }
        if (!scheduledAt.minusMinutes(30).isAfter(now)) {
            return Appointment.Status.WAITING;
        }
        return Appointment.Status.SCHEDULED;
    }
}
