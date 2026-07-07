package com.clinicflow.clinic_flow.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinicflow.clinic_flow.appointment.dtos.AppointmentPatchDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentRequestDto;
import com.clinicflow.clinic_flow.appointment.dtos.BoardRowDto;
import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.exception.AppointmentAtTimeExistsException;
import com.clinicflow.clinic_flow.exception.AppointmentNotFoundException;
import com.clinicflow.clinic_flow.exception.DemoClinicReadOnlyException;
import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
import com.clinicflow.clinic_flow.exception.TherapistNotFoundException;
import com.clinicflow.clinic_flow.patient.Patient;
import com.clinicflow.clinic_flow.patient.PatientRepository;
import com.clinicflow.clinic_flow.therapist.Therapist;
import com.clinicflow.clinic_flow.therapist.TherapistRepository;
import com.clinicflow.clinic_flow.users.CurrentUserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private TherapistRepository therapistRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ClinicContextService clinicContextService;

    @InjectMocks
    private AppointmentService appointmentService;

    private static final LocalDateTime AT = LocalDateTime.of(2026, 2, 13, 9, 0);

    private BoardRowDto boardRow() {
        return new BoardRowDto(
                1L, AT, Appointment.Type.EVALUATION, Appointment.Status.SCHEDULED, 5L, "Jane Doe", 3L, "Dr. Smith");
    }

    private Clinics clinic(long id) {
        Clinics clinic = new Clinics();
        clinic.setId(id);
        return clinic;
    }

    @Test
    void getAppointmentsByDate_mapsProjectionsToBoardRows() {
        var projection = mock(AppointmentBoardProjection.class);
        var row = boardRow();
        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findDailyAppointments(LocalDate.of(2026, 2, 13), 7L))
                .thenReturn(List.of(projection));
        when(appointmentMapper.projectionToBoardRow(projection)).thenReturn(row);

        var result = appointmentService.getAppointmentsByDate(LocalDate.of(2026, 2, 13));

        assertEquals(List.of(row), result);
    }

    @Test
    void createAppointment_withExistingPatient_savesScheduledAppointment() {
        Therapist therapist = new Therapist();
        therapist.setId(3L);
        Patient patient = new Patient();
        patient.setId(5L);

        when(clinicContextService.requireWritableClinic()).thenReturn(clinic(7L));
        when(therapistRepository.findByIdAndClinicId(3L, 7L)).thenReturn(Optional.of(therapist));
        when(patientRepository.findByIdAndClinicId(5L, 7L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(AT, 7L, 3L))
                .thenReturn(Optional.empty());
        when(appointmentRepository.existsPatientAppointmentConflict(7L, 5L, AT)).thenReturn(false);

        Appointment saved = new Appointment();
        saved.setId(1L);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);
        when(appointmentMapper.entityToBoardRow(saved)).thenReturn(boardRow());

        var request = new AppointmentRequestDto(AT, 3L, Appointment.Type.EVALUATION, 5L, null);
        var result = appointmentService.createAppointment(request);

        assertSame(boardRow().getId(), result.getId());
        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertSame(patient, captor.getValue().getPatient());
        assertSame(therapist, captor.getValue().getTherapist());
        assertEquals(Appointment.Status.SCHEDULED, captor.getValue().getStatus());
    }

    @Test
    void createAppointment_withInlineNewPatient_createsPatient() {
        Therapist therapist = new Therapist();
        therapist.setId(3L);
        Patient created = new Patient();
        created.setId(9L);

        when(clinicContextService.requireWritableClinic()).thenReturn(clinic(7L));
        when(therapistRepository.findByIdAndClinicId(3L, 7L)).thenReturn(Optional.of(therapist));
        when(patientRepository.save(any(Patient.class))).thenReturn(created);
        when(appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(AT, 7L, 3L))
                .thenReturn(Optional.empty());
        when(appointmentRepository.existsPatientAppointmentConflict(7L, 9L, AT)).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(new Appointment());
        when(appointmentMapper.entityToBoardRow(any(Appointment.class))).thenReturn(boardRow());

        var request = new AppointmentRequestDto(
                AT, 3L, Appointment.Type.EVALUATION, null, new AppointmentRequestDto.NewPatient("New", "Patient"));
        appointmentService.createAppointment(request);

        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void createAppointment_onTherapistTimeConflict_throws() {
        Therapist therapist = new Therapist();
        therapist.setId(3L);
        Patient patient = new Patient();
        patient.setId(5L);

        when(clinicContextService.requireWritableClinic()).thenReturn(clinic(7L));
        when(therapistRepository.findByIdAndClinicId(3L, 7L)).thenReturn(Optional.of(therapist));
        when(patientRepository.findByIdAndClinicId(5L, 7L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(AT, 7L, 3L))
                .thenReturn(Optional.of(new Appointment()));

        var request = new AppointmentRequestDto(AT, 3L, Appointment.Type.EVALUATION, 5L, null);
        assertThrows(AppointmentAtTimeExistsException.class, () -> appointmentService.createAppointment(request));
    }

    @Test
    void createAppointment_whenTherapistMissing_throws() {
        when(clinicContextService.requireWritableClinic()).thenReturn(clinic(7L));
        when(therapistRepository.findByIdAndClinicId(3L, 7L)).thenReturn(Optional.empty());

        var request = new AppointmentRequestDto(AT, 3L, Appointment.Type.EVALUATION, 5L, null);
        assertThrows(TherapistNotFoundException.class, () -> appointmentService.createAppointment(request));
    }

    @Test
    void createAppointment_whenPatientMissing_throws() {
        Therapist therapist = new Therapist();
        therapist.setId(3L);
        when(clinicContextService.requireWritableClinic()).thenReturn(clinic(7L));
        when(therapistRepository.findByIdAndClinicId(3L, 7L)).thenReturn(Optional.of(therapist));
        when(patientRepository.findByIdAndClinicId(5L, 7L)).thenReturn(Optional.empty());

        var request = new AppointmentRequestDto(AT, 3L, Appointment.Type.EVALUATION, 5L, null);
        assertThrows(PatientNotFoundException.class, () -> appointmentService.createAppointment(request));
    }

    @Test
    void createAppointment_inDemoClinic_throws() {
        when(clinicContextService.requireWritableClinic()).thenThrow(new DemoClinicReadOnlyException());
        var request = new AppointmentRequestDto(AT, 3L, Appointment.Type.EVALUATION, 5L, null);
        assertThrows(DemoClinicReadOnlyException.class, () -> appointmentService.createAppointment(request));
    }

    @Test
    void updateAppointment_appliesStatusChange() {
        Appointment appointment = new Appointment();
        appointment.setId(9L);
        appointment.setScheduledAt(AT);
        appointment.setStatus(Appointment.Status.SCHEDULED);
        appointment.setTherapist(new Therapist());
        Patient patient = new Patient();
        patient.setId(5L);
        appointment.setPatient(patient);

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findByIdAndClinicId(9L, 7L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.entityToBoardRow(appointment)).thenReturn(boardRow());

        var patch = new AppointmentPatchDto(null, null, Appointment.Status.WAITING, null);
        appointmentService.updateAppointmentById(9L, patch);

        assertEquals(Appointment.Status.WAITING, appointment.getStatus());
        verify(clinicContextService).assertWritableClinic();
    }

    @Test
    void updateAppointment_whenMissing_throws() {
        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findByIdAndClinicId(9L, 7L)).thenReturn(Optional.empty());

        var patch = new AppointmentPatchDto(null, null, Appointment.Status.WAITING, null);
        assertThrows(AppointmentNotFoundException.class, () -> appointmentService.updateAppointmentById(9L, patch));
    }

    @Test
    void deleteAppointment_deletesWhenFound() {
        Appointment appointment = new Appointment();
        appointment.setId(9L);
        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findByIdAndClinicId(9L, 7L)).thenReturn(Optional.of(appointment));

        appointmentService.deleteAppointmentById(9L);

        verify(appointmentRepository).delete(appointment);
    }
}
