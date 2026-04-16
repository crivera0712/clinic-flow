package com.clinicflow.clinic_flow.demo;

import com.clinicflow.clinic_flow.appointment.Appointment;
import com.clinicflow.clinic_flow.appointment.AppointmentRepository;
import com.clinicflow.clinic_flow.body_region.BodyRegion;
import com.clinicflow.clinic_flow.body_region.BodyRegionRepository;
import com.clinicflow.clinic_flow.cases.Case;
import com.clinicflow.clinic_flow.cases.CaseRepository;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.clinics.ClinicsRepository;
import com.clinicflow.clinic_flow.patient.Patient;
import com.clinicflow.clinic_flow.patient.PatientRepository;
import com.clinicflow.clinic_flow.therapist.Therapist;
import com.clinicflow.clinic_flow.therapist.TherapistRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class DemoResetService {

    private static final ZoneId LA = ZoneId.of("America/Los_Angeles");
    private static final String DEMO_SLUG = "demo";

    private final ClinicsRepository clinicsRepository;
    private final TherapistRepository therapistRepository;
    private final PatientRepository patientRepository;
    private final CaseRepository caseRepository;
    private final AppointmentRepository appointmentRepository;
    private final BodyRegionRepository bodyRegionRepository;

    @Transactional
    public void resetDemoData() {
        Optional<Clinics> demoOpt = clinicsRepository.findClinicsBySlug(DEMO_SLUG);
        if (demoOpt.isEmpty()) {
            log.warn("DemoResetService: demo clinic not found, skipping reset");
            return;
        }
        Clinics clinic = demoOpt.get();
        Long clinicId = clinic.getId();

        List<Therapist> allTherapists = therapistRepository.findAllByClinicId(clinicId);
        Therapist pt  = findFirstByType(allTherapists, Therapist.TherapistType.PHYSICAL_THERAPIST);
        Therapist ot  = findFirstByType(allTherapists, Therapist.TherapistType.OCCUPATIONAL_THERAPIST);
        Therapist pta = findFirstByType(allTherapists, Therapist.TherapistType.PHYSICAL_THERAPY_ASSISTANT);

        if (pt == null || ot == null || pta == null) {
            log.error("DemoResetService: could not find one therapist of each type for demo clinic (clinicId={}), skipping reset", clinicId);
            return;
        }

        Map<String, BodyRegion> regionMap = loadBodyRegions(
                "SHOULDER", "KNEE", "HIP", "ANKLE", "ELBOW",
                "WRIST", "NECK", "LOW_BACK", "HAND", "FOOT"
        );

        // Delete in FK-safe order: appointments → cases → patients
        appointmentRepository.deleteAllByClinicId(clinicId);
        caseRepository.deleteAllByClinicId(clinicId);
        patientRepository.deleteAllByClinicId(clinicId);

        List<Patient> patients = patientRepository.saveAll(buildPatients(clinic));
        List<Case> cases = caseRepository.saveAll(buildCases(patients, clinic, regionMap));
        appointmentRepository.saveAll(buildAppointments(cases, patients, pt, ot, pta, clinic));

        log.info("DemoResetService: reset complete for demo clinic (clinicId={})", clinicId);
    }

    // ── Patients ──────────────────────────────────────────────────────────────

    private List<Patient> buildPatients(Clinics clinic) {
        String[][] data = {
                {"Marcus",  "Webb",    "SHOULDER"},
                {"Priya",   "Nair",    "KNEE"},
                {"Daniel",  "Cho",     "HIP"},
                {"Sofia",   "Reyes",   "ANKLE"},
                {"Jordan",  "Flores",  "ELBOW"},
                {"Aaliyah", "Brooks",  "WRIST"},
                {"Ethan",   "Park",    "NECK"},
                {"Camila",  "Torres",  "LOW_BACK"},
                {"Noah",    "Schmidt", "HAND"},
                {"Leila",   "Hassan",  "FOOT"},
        };
        List<Patient> patients = new ArrayList<>();
        for (String[] row : data) {
            Patient p = new Patient();
            p.setFirstName(row[0]);
            p.setLastName(row[1]);
            p.setDisplayName(row[1] + ", " + row[0].charAt(0));
            p.setClinic(clinic);
            patients.add(p);
        }
        return patients;
    }

    // ── Cases ─────────────────────────────────────────────────────────────────

    private List<Case> buildCases(List<Patient> patients, Clinics clinic, Map<String, BodyRegion> regionMap) {
        String[] regionCodes = {
                "SHOULDER", "KNEE", "HIP", "ANKLE", "ELBOW",
                "WRIST", "NECK", "LOW_BACK", "HAND", "FOOT"
        };
        List<Case> cases = new ArrayList<>();
        for (int i = 0; i < patients.size(); i++) {
            Case c = new Case();
            c.setPatient(patients.get(i));
            c.setBodyRegion(regionMap.get(regionCodes[i]));
            c.setClinic(clinic);
            c.setCreatedAt(Date.from(Instant.now().minus(30, ChronoUnit.DAYS)));
            cases.add(c);
        }
        return cases;
    }

    // ── Appointments ──────────────────────────────────────────────────────────

    private List<Appointment> buildAppointments(
            List<Case> cases, List<Patient> patients,
            Therapist pt, Therapist ot, Therapist pta, Clinics clinic) {

        // Build a map from patient index → case for easy lookup
        Map<Long, Case> caseByPatientId = new HashMap<>();
        for (int i = 0; i < patients.size(); i++) {
            caseByPatientId.put(patients.get(i).getId(), cases.get(i));
        }

        LocalDate today = LocalDate.now(LA);
        LocalTime nowLA = LocalTime.now(LA);

        List<Appointment> appointments = new ArrayList<>();

        // PT (Jill Valentine) — 30-min slots, EVALs at start/end
        int[][] ptSlots = {
                {9,  0},  {9, 30}, {10,  0}, {10, 30}, {11,  0},
                {11, 30}, {13, 0}, {13, 30}, {14,  0}, {16, 30}
        };
        Appointment.Type[] ptTypes = {
                Appointment.Type.EVALUATION,
                Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
                Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
                Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
                Appointment.Type.EVALUATION
        };
        int[] ptPatients = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        for (int i = 0; i < ptSlots.length; i++) {
            LocalDateTime scheduledAt = today.atTime(ptSlots[i][0], ptSlots[i][1]);
            appointments.add(buildAppointment(
                    scheduledAt, computeStatus(scheduledAt, nowLA),
                    ptTypes[i], caseByPatientId.get(patients.get(ptPatients[i]).getId()),
                    pt, clinic));
        }

        // OT (Claire Redfield) — 30-min slots, EVALs at start/end
        int[][] otSlots = {
                {9,  0},  {9, 30}, {10,  0}, {10, 30}, {11,  0},
                {11, 30}, {13, 0}, {13, 30}, {14,  0}, {16, 30}
        };
        Appointment.Type[] otTypes = {
                Appointment.Type.EVALUATION,
                Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
                Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
                Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
                Appointment.Type.EVALUATION
        };
        int[] otPatients = {0, 2, 3, 4, 5, 1, 7, 8, 9, 6};

        for (int i = 0; i < otSlots.length; i++) {
            LocalDateTime scheduledAt = today.atTime(otSlots[i][0], otSlots[i][1]);
            appointments.add(buildAppointment(
                    scheduledAt, computeStatus(scheduledAt, nowLA),
                    otTypes[i], caseByPatientId.get(patients.get(otPatients[i]).getId()),
                    ot, clinic));
        }

        // PTA (Rebecca Chambers) — 45-min slots, 2 REASSESSMENTs at positions 2 and 6
        int[][] ptaSlots = {
                {9,  0},  {9, 45}, {10, 30}, {11, 15}, {13,  0},
                {13, 45}, {14, 30}, {15, 15}, {16,  0}, {16, 45}
        };
        Appointment.Type[] ptaTypes = {
                Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
                Appointment.Type.REASSESSMENT,
                Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
                Appointment.Type.REASSESSMENT,
                Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP
        };
        int[] ptaPatients = {3, 4, 0, 5, 6, 7, 1, 8, 9, 2};

        for (int i = 0; i < ptaSlots.length; i++) {
            LocalDateTime scheduledAt = today.atTime(ptaSlots[i][0], ptaSlots[i][1]);
            appointments.add(buildAppointment(
                    scheduledAt, computeStatus(scheduledAt, nowLA),
                    ptaTypes[i], caseByPatientId.get(patients.get(ptaPatients[i]).getId()),
                    pta, clinic));
        }

        return appointments;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Appointment buildAppointment(LocalDateTime scheduledAt, Appointment.Status status,
                                         Appointment.Type type, Case ptCase,
                                         Therapist therapist, Clinics clinic) {
        Appointment a = new Appointment();
        a.setScheduledAt(scheduledAt);
        a.setStatus(status);
        a.setType(type);
        a.setPtCase(ptCase);
        a.setTherapist(therapist);
        a.setClinic(clinic);
        a.setCreatedAt(Instant.now());
        a.setModifiedAt(Instant.now());
        return a;
    }

    private Appointment.Status computeStatus(LocalDateTime scheduledAt, LocalTime nowLA) {
        LocalTime slotTime = scheduledAt.toLocalTime();
        LocalTime slotEnd  = slotTime.plusMinutes(45);

        if (slotEnd.isBefore(nowLA)) {
            return Appointment.Status.FINISHED;
        } else if (!slotTime.isAfter(nowLA) && slotEnd.isAfter(nowLA)) {
            return Appointment.Status.IN_SESSION;
        } else if (slotTime.minusMinutes(30).isBefore(nowLA)) {
            return Appointment.Status.CHECKED_IN;
        } else {
            return Appointment.Status.SCHEDULED;
        }
    }

    private Therapist findFirstByType(List<Therapist> therapists, Therapist.TherapistType type) {
        return therapists.stream()
                .filter(t -> t.getType() == type)
                .findFirst()
                .orElse(null);
    }

    private Map<String, BodyRegion> loadBodyRegions(String... codes) {
        Map<String, BodyRegion> map = new HashMap<>();
        for (String code : codes) {
            bodyRegionRepository.findByCode(code).ifPresentOrElse(
                    br -> map.put(code, br),
                    () -> log.warn("DemoResetService: body region '{}' not found", code)
            );
        }
        return map;
    }
}
