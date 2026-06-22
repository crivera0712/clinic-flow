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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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
    private final AppointmentRepository appointmentRepository;

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
        LocalDate today = LocalDate.now(LA);
        LocalTime nowLA = LocalTime.now(LA);

        int[][] slots = {
                {9, 0}, {9, 30}, {10, 0}, {10, 30}, {11, 0},
                {11, 30}, {13, 0}, {13, 30}, {14, 0}, {16, 30}
        };
        Appointment.Type[] types = {
                Appointment.Type.EVALUATION, Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
                Appointment.Type.REASSESSMENT, Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
                Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP, Appointment.Type.FOLLOW_UP,
                Appointment.Type.EVALUATION,
        };

        List<Appointment> appointments = new ArrayList<>();
        for (int i = 0; i < patients.size(); i++) {
            int[] slot = slots[i % slots.length];
            LocalDateTime scheduledAt = today.atTime(slot[0], slot[1]);
            Appointment a = new Appointment();
            a.setScheduledAt(scheduledAt);
            a.setStatus(computeStatus(scheduledAt, nowLA));
            a.setType(types[i % types.length]);
            a.setPatient(patients.get(i));
            a.setTherapist(therapists.get(i % therapists.size()));
            a.setClinic(clinic);
            a.setCreatedAt(Instant.now());
            a.setModifiedAt(Instant.now());
            appointments.add(a);
        }
        return appointments;
    }

    private Appointment.Status computeStatus(LocalDateTime scheduledAt, LocalTime nowLA) {
        LocalTime slotTime = scheduledAt.toLocalTime();
        LocalTime slotEnd = slotTime.plusMinutes(45);

        if (slotEnd.isBefore(nowLA)) {
            return Appointment.Status.DONE;
        } else if (!slotTime.minusMinutes(30).isAfter(nowLA)) {
            // within 30 min before start, or in progress → in the waiting room
            return Appointment.Status.WAITING;
        } else {
            return Appointment.Status.SCHEDULED;
        }
    }
}
