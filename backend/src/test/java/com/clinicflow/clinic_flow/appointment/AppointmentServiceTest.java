package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.appointment.dtos.AppointmentPatchDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentRequestDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentResponseDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentUiDto;
import com.clinicflow.clinic_flow.cases.Case;
import com.clinicflow.clinic_flow.cases.CaseRepository;
import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.exception.DemoClinicReadOnlyException;
import com.clinicflow.clinic_flow.exception.AppointmentAtTimeExistsException;
import com.clinicflow.clinic_flow.exception.AppointmentNotFoundException;
import com.clinicflow.clinic_flow.exception.CaseNotFoundException;
import com.clinicflow.clinic_flow.exception.TherapistNotFoundException;
import com.clinicflow.clinic_flow.therapist.Therapist;
import com.clinicflow.clinic_flow.therapist.TherapistRepository;
import com.clinicflow.clinic_flow.users.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private TherapistRepository therapistRepository;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ClinicContextService clinicContextService;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void shouldReturnAllAppointmentsWithinClinic() {
        Appointment firstAppointment = appointmentWithId(1L);
        Appointment secondAppointment = appointmentWithId(2L);
        AppointmentResponseDto firstResponse = responseDto(1L);
        AppointmentResponseDto secondResponse = responseDto(2L);
        Pageable pageable = PageRequest.of(0, 10);

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findAllByClinicId(7L, pageable)).thenReturn(new PageImpl<>(List.of(firstAppointment, secondAppointment)));
        when(appointmentMapper.entityToAppointmentResponseDto(firstAppointment)).thenReturn(firstResponse);
        when(appointmentMapper.entityToAppointmentResponseDto(secondAppointment)).thenReturn(secondResponse);

        Page<AppointmentResponseDto> result = appointmentService.getAllAppointments(pageable, null, null);

        assertEquals(List.of(firstResponse, secondResponse), result.getContent());
        verify(appointmentRepository).findAllByClinicId(7L, pageable);
    }

    @Test
    void shouldReturnDateFilteredAppointmentsWithinClinic() {
        Appointment appointment = appointmentWithId(1L);
        AppointmentResponseDto response = responseDto(1L);
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate date = LocalDate.of(2026, 2, 13);

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findByClinicIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                7L, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), pageable
        )).thenReturn(new PageImpl<>(List.of(appointment)));
        when(appointmentMapper.entityToAppointmentResponseDto(appointment)).thenReturn(response);

        Page<AppointmentResponseDto> result = appointmentService.getAllAppointments(pageable, date, null);

        assertEquals(List.of(response), result.getContent());
    }

    @Test
    void shouldReturnCaseFilteredAppointmentsWithinClinic() {
        Appointment appointment = appointmentWithId(2L);
        AppointmentResponseDto response = responseDto(2L);
        Pageable pageable = PageRequest.of(0, 10);

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findByClinicIdAndPtCaseIdOrderByScheduledAtDesc(7L, 4L, pageable))
                .thenReturn(new PageImpl<>(List.of(appointment)));
        when(appointmentMapper.entityToAppointmentResponseDto(appointment)).thenReturn(response);

        Page<AppointmentResponseDto> result = appointmentService.getAllAppointments(pageable, null, 4L);

        assertEquals(List.of(response), result.getContent());
    }

    @Test
    void shouldReturnAppointmentByIdWithinClinic() {
        Appointment appointment = appointmentWithId(7L);
        AppointmentResponseDto response = responseDto(7L);

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findByIdAndClinicId(7L, 7L)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.entityToAppointmentResponseDto(appointment)).thenReturn(response);

        AppointmentResponseDto result = appointmentService.getAppointmentById(7L);

        assertSame(response, result);
    }

    @Test
    void shouldCreateAppointmentWithinClinic() {
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 2, 13, 9, 0);
        AppointmentRequestDto request = new AppointmentRequestDto(
                scheduledAt,
                null,
                4L,
                3L,
                Appointment.Status.SCHEDULED,
                Appointment.Type.EVALUATION
        );
        Appointment mappedAppointment = new Appointment();
        Therapist therapist = therapistWithId(3L);
        Case ptCase = caseWithId(4L);
        Appointment savedAppointment = appointmentWithId(11L);
        AppointmentResponseDto response = responseDto(11L);
        Clinics clinic = clinic(7L);

        when(clinicContextService.requireWritableClinic()).thenReturn(clinic);
        when(appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(scheduledAt, 7L, 3L)).thenReturn(Optional.empty());
        when(appointmentMapper.requestToAppointmentEntityDto(request)).thenReturn(mappedAppointment);
        when(therapistRepository.findByIdAndClinicId(3L, 7L)).thenReturn(Optional.of(therapist));
        when(caseRepository.findByIdAndClinicId(4L, 7L)).thenReturn(Optional.of(ptCase));
        when(appointmentRepository.save(mappedAppointment)).thenReturn(savedAppointment);
        when(appointmentMapper.entityToAppointmentResponseDto(savedAppointment)).thenReturn(response);

        AppointmentResponseDto result = appointmentService.createAppointment(request);

        assertSame(response, result);
        assertSame(clinic, mappedAppointment.getClinic());
        assertSame(therapist, mappedAppointment.getTherapist());
        assertSame(ptCase, mappedAppointment.getPtCase());
    }

    @Test
    void shouldThrowConflictWhenAppointmentAlreadyExistsForClinicTherapistAndTime() {
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 2, 13, 9, 0);
        AppointmentRequestDto request = new AppointmentRequestDto(
                scheduledAt,
                null,
                4L,
                3L,
                Appointment.Status.SCHEDULED,
                Appointment.Type.EVALUATION
        );

        when(clinicContextService.requireWritableClinic()).thenReturn(clinic(7L));
        when(appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(scheduledAt, 7L, 3L))
                .thenReturn(Optional.of(new Appointment()));

        assertThrows(AppointmentAtTimeExistsException.class, () -> appointmentService.createAppointment(request));
    }

    @Test
    void shouldRejectAppointmentWhenTherapistBelongsToAnotherClinic() {
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 2, 13, 9, 0);
        AppointmentRequestDto request = new AppointmentRequestDto(
                scheduledAt,
                null,
                4L,
                3L,
                Appointment.Status.SCHEDULED,
                Appointment.Type.EVALUATION
        );
        Clinics clinic = clinic(7L);

        when(clinicContextService.requireWritableClinic()).thenReturn(clinic);
        when(appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(scheduledAt, 7L, 3L)).thenReturn(Optional.empty());
        when(appointmentMapper.requestToAppointmentEntityDto(request)).thenReturn(new Appointment());
        when(therapistRepository.findByIdAndClinicId(3L, 7L)).thenReturn(Optional.empty());

        assertThrows(TherapistNotFoundException.class, () -> appointmentService.createAppointment(request));

        verify(caseRepository, never()).findByIdAndClinicId(any(), any());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldRejectAppointmentWhenCaseBelongsToAnotherClinic() {
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 2, 13, 9, 0);
        AppointmentRequestDto request = new AppointmentRequestDto(
                scheduledAt,
                null,
                4L,
                3L,
                Appointment.Status.SCHEDULED,
                Appointment.Type.EVALUATION
        );
        Clinics clinic = clinic(7L);
        Therapist therapist = therapistWithId(3L);

        when(clinicContextService.requireWritableClinic()).thenReturn(clinic);
        when(appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(scheduledAt, 7L, 3L)).thenReturn(Optional.empty());
        when(appointmentMapper.requestToAppointmentEntityDto(request)).thenReturn(new Appointment());
        when(therapistRepository.findByIdAndClinicId(3L, 7L)).thenReturn(Optional.of(therapist));
        when(caseRepository.findByIdAndClinicId(4L, 7L)).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class, () -> appointmentService.createAppointment(request));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldReturnAppointmentUiDtosForDateWithinClinic() {
        LocalDate date = LocalDate.of(2026, 2, 13);
        AppointmentScheduleProjection projection = projection(
                21L, LocalDateTime.of(2026, 2, 13, 9, 0), 31L, "Sam", "Lee", 41L,
                "Taylor", "PHYSICAL_THERAPIST", "Shoulder", Appointment.Status.SCHEDULED,
                Appointment.Type.EVALUATION, "Lee, S");
        AppointmentUiDto dto = uiDto(21L, projection.getScheduledAt());

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findDailyAppointments(date, 7L)).thenReturn(List.of(projection));
        when(appointmentMapper.toAppointmentUiDto(projection)).thenReturn(dto);

        List<AppointmentUiDto> result = appointmentService.getAppointmentsByDate(date);

        assertEquals(List.of(dto), result);
    }

    @Test
    void shouldUpdateAppointmentWithinClinic() {
        Appointment appointment = appointmentWithId(9L);
        appointment.setScheduledAt(LocalDateTime.of(2026, 2, 13, 9, 0));
        appointment.setTherapist(therapistWithId(5L));
        appointment.setPtCase(caseWithId(6L));
        AppointmentPatchDto request = patchRequest(
                LocalDateTime.of(2026, 2, 14, 11, 30), 17L, 16L, Appointment.Status.FINISHED);
        AppointmentResponseDto response = responseDto(9L);
        Therapist therapist = therapistWithId(16L);
        Case ptCase = caseWithId(17L);

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findByIdAndClinicId(9L, 7L)).thenReturn(Optional.of(appointment));
        when(therapistRepository.findByIdAndClinicId(16L, 7L)).thenReturn(Optional.of(therapist));
        when(caseRepository.findByIdAndClinicId(17L, 7L)).thenReturn(Optional.of(ptCase));
        when(appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(request.getScheduledAt(), 7L, 16L))
                .thenReturn(Optional.empty());
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.entityToAppointmentResponseDto(appointment)).thenReturn(response);

        AppointmentResponseDto result = appointmentService.updateAppointmentById(9L, request);

        assertSame(response, result);
        assertEquals(request.getScheduledAt(), appointment.getScheduledAt());
        assertSame(therapist, appointment.getTherapist());
        assertSame(ptCase, appointment.getPtCase());
        assertEquals(Appointment.Status.FINISHED, appointment.getStatus());
    }

    @Test
    void shouldThrowWhenAppointmentByIdDoesNotExistInClinic() {
        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findByIdAndClinicId(7L, 7L)).thenReturn(Optional.empty());

        AppointmentNotFoundException exception = assertThrows(
                AppointmentNotFoundException.class,
                () -> appointmentService.getAppointmentById(7L)
        );

        assertEquals("Appointment not found for this clinic", exception.getMessage());
    }

    @Test
    void shouldDeleteAppointmentWithinClinic() {
        Appointment appointment = appointmentWithId(15L);
        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findByIdAndClinicId(15L, 7L)).thenReturn(Optional.of(appointment));

        appointmentService.deleteAppointmentById(15L);

        verify(appointmentRepository).delete(appointment);
    }

    @Test
    void shouldThrowWhenDemoClinicCreatesAppointment() {
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 2, 13, 9, 0);
        AppointmentRequestDto request = new AppointmentRequestDto(
                scheduledAt,
                null,
                4L,
                3L,
                Appointment.Status.SCHEDULED,
                Appointment.Type.EVALUATION
        );

        when(clinicContextService.requireWritableClinic()).thenThrow(new DemoClinicReadOnlyException());

        assertThrows(DemoClinicReadOnlyException.class, () -> appointmentService.createAppointment(request));

        verify(appointmentRepository, never()).findByScheduledAtAndClinicIdAndTherapistId(any(), any(), any());
    }

    @Test
    void shouldThrowWhenDemoClinicUpdatesAppointment() {
        org.mockito.Mockito.doThrow(new DemoClinicReadOnlyException()).when(clinicContextService).assertWritableClinic();

        assertThrows(DemoClinicReadOnlyException.class, () -> appointmentService.updateAppointmentById(9L, new AppointmentPatchDto()));

        verify(appointmentRepository, never()).findByIdAndClinicId(any(), any());
    }

    @Test
    void shouldThrowWhenDemoClinicDeletesAppointment() {
        org.mockito.Mockito.doThrow(new DemoClinicReadOnlyException()).when(clinicContextService).assertWritableClinic();

        assertThrows(DemoClinicReadOnlyException.class, () -> appointmentService.deleteAppointmentById(15L));

        verify(appointmentRepository, never()).delete(any(Appointment.class));
    }

    @Test
    void shouldRejectAppointmentPatchWhenTherapistBelongsToAnotherClinic() {
        Appointment appointment = appointmentWithId(9L);
        appointment.setScheduledAt(LocalDateTime.of(2026, 2, 13, 9, 0));
        appointment.setTherapist(therapistWithId(5L));
        AppointmentPatchDto request = patchRequest(
                LocalDateTime.of(2026, 2, 14, 11, 30), null, 16L, Appointment.Status.FINISHED);

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findByIdAndClinicId(9L, 7L)).thenReturn(Optional.of(appointment));
        when(therapistRepository.findByIdAndClinicId(16L, 7L)).thenReturn(Optional.empty());

        assertThrows(TherapistNotFoundException.class, () -> appointmentService.updateAppointmentById(9L, request));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldRejectAppointmentPatchWhenCaseBelongsToAnotherClinic() {
        Appointment appointment = appointmentWithId(9L);
        appointment.setScheduledAt(LocalDateTime.of(2026, 2, 13, 9, 0));
        appointment.setTherapist(therapistWithId(5L));
        appointment.setPtCase(caseWithId(6L));
        AppointmentPatchDto request = patchRequest(
                LocalDateTime.of(2026, 2, 14, 11, 30), 17L, 16L, Appointment.Status.FINISHED);
        Therapist therapist = therapistWithId(16L);

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(appointmentRepository.findByIdAndClinicId(9L, 7L)).thenReturn(Optional.of(appointment));
        when(therapistRepository.findByIdAndClinicId(16L, 7L)).thenReturn(Optional.of(therapist));
        when(appointmentRepository.findByScheduledAtAndClinicIdAndTherapistId(request.getScheduledAt(), 7L, 16L))
                .thenReturn(Optional.empty());
        when(caseRepository.findByIdAndClinicId(17L, 7L)).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class, () -> appointmentService.updateAppointmentById(9L, request));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    private Appointment appointmentWithId(Long id) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setType(Appointment.Type.EVALUATION);
        appointment.setStatus(Appointment.Status.SCHEDULED);
        return appointment;
    }

    private Therapist therapistWithId(Long id) {
        Therapist therapist = new Therapist();
        therapist.setId(id);
        therapist.setTherapistName("Taylor");
        therapist.setType(Therapist.TherapistType.PHYSICAL_THERAPIST);
        return therapist;
    }

    private Case caseWithId(Long id) {
        Case ptCase = new Case();
        ptCase.setId(id);
        return ptCase;
    }

    private Clinics clinic(Long id) {
        Clinics clinic = new Clinics();
        clinic.setId(id);
        clinic.setSlug("clinic-" + id);
        return clinic;
    }

    private AppointmentResponseDto responseDto(Long id) {
        return new AppointmentResponseDto(id, null, null, null, null, null, null);
    }

    private AppointmentUiDto uiDto(Long aptId, LocalDateTime scheduledAt) {
        return new AppointmentUiDto(
                aptId,
                scheduledAt,
                31L,
                "Sam",
                "Lee",
                41L,
                "Taylor",
                "PHYSICAL_THERAPIST",
                "Shoulder",
                Appointment.Status.SCHEDULED,
                Appointment.Type.EVALUATION,
                "Lee, S"
        );
    }

    private AppointmentPatchDto patchRequest(LocalDateTime scheduledAt, Long caseId, Long therapistId, Appointment.Status status) {
        AppointmentPatchDto patch = new AppointmentPatchDto();
        setField(patch, "scheduledAt", scheduledAt);
        setField(patch, "caseId", caseId);
        setField(patch, "therapistId", therapistId);
        setField(patch, "status", status);
        return patch;
    }

    private AppointmentScheduleProjection projection(
            Long aptId,
            LocalDateTime scheduledAt,
            Long caseId,
            String firstName,
            String lastName,
            Long therapistId,
            String therapistName,
            String therapistType,
            String bodyRegionDisplayName,
            Appointment.Status status,
            Appointment.Type type,
            String displayName
    ) {
        return new AppointmentScheduleProjection() {
            @Override public LocalDateTime getScheduledAt() { return scheduledAt; }
            @Override public Long getAptId() { return aptId; }
            @Override public Long getCaseId() { return caseId; }
            @Override public String getFirstName() { return firstName; }
            @Override public String getLastName() { return lastName; }
            @Override public Long getTherapistId() { return therapistId; }
            @Override public String getTherapistName() { return therapistName; }
            @Override public String getTherapistType() { return therapistType; }
            @Override public String getBodyRegionDisplayName() { return bodyRegionDisplayName; }
            @Override public Appointment.Status getStatus() { return status; }
            @Override public Appointment.Type getType() { return type; }
            @Override public String getDisplayName() { return displayName; }
        };
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
