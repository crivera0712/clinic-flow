package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.appointment.dtos.AppointmentPatchDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentRequestDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentResponseDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentUiDto;
import com.clinicflow.clinic_flow.cases.Case;
import com.clinicflow.clinic_flow.cases.CaseRepository;
import com.clinicflow.clinic_flow.exception.AppointmentAtTimeExistsException;
import com.clinicflow.clinic_flow.therapist.Therapist;
import com.clinicflow.clinic_flow.therapist.TherapistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void shouldReturnAppointmentResponses_whenGetAllAppointmentsFindsAppointments() {
        // Arrange
        Appointment firstAppointment = appointmentWithId(1L);
        Appointment secondAppointment = appointmentWithId(2L);
        AppointmentResponseDto firstResponse = responseDto(1L);
        AppointmentResponseDto secondResponse = responseDto(2L);
        Pageable pageable = PageRequest.of(0, 10);

        when(appointmentRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(firstAppointment, secondAppointment)));
        when(appointmentMapper.entityToAppointmentResponseDto(firstAppointment)).thenReturn(firstResponse);
        when(appointmentMapper.entityToAppointmentResponseDto(secondAppointment)).thenReturn(secondResponse);

        // Act
        Page<AppointmentResponseDto> result = appointmentService.getAllAppointments(pageable);

        // Assert
        assertEquals(List.of(firstResponse, secondResponse), result.getContent());
        verify(appointmentRepository).findAll(pageable);
        verify(appointmentMapper).entityToAppointmentResponseDto(firstAppointment);
        verify(appointmentMapper).entityToAppointmentResponseDto(secondAppointment);
    }

    @Test
    void shouldReturnEmptyList_whenGetAllAppointmentsFindsNoAppointments() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(appointmentRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        // Act
        Page<AppointmentResponseDto> result = appointmentService.getAllAppointments(pageable);

        // Assert
        assertTrue(result.isEmpty());
        verify(appointmentRepository).findAll(pageable);
        verify(appointmentMapper, never()).entityToAppointmentResponseDto(any(Appointment.class));
    }

    @Test
    void shouldReturnAppointmentResponse_whenGetAppointmentByIdFindsAppointment() {
        // Arrange
        Appointment appointment = appointmentWithId(7L);
        AppointmentResponseDto response = responseDto(7L);

        when(appointmentRepository.findById(7L)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.entityToAppointmentResponseDto(appointment)).thenReturn(response);

        // Act
        AppointmentResponseDto result = appointmentService.getAppointmentById(7L);

        // Assert
        assertSame(response, result);
        verify(appointmentRepository).findById(7L);
        verify(appointmentMapper).entityToAppointmentResponseDto(appointment);
    }

    @Test
    void shouldThrowException_whenGetAppointmentByIdDoesNotFindAppointment() {
        // Arrange
        when(appointmentRepository.findById(7L)).thenReturn(Optional.empty());

        // Act
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> appointmentService.getAppointmentById(7L));

        // Assert
        assertNotNull(exception);
        verify(appointmentRepository).findById(7L);
        verify(appointmentMapper, never()).entityToAppointmentResponseDto(any(Appointment.class));
    }

    @Test
    void shouldCreateAppointment_whenCreateAppointmentReceivesValidRequest() {
        // Arrange
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

        when(appointmentRepository.findByScheduledAt(scheduledAt)).thenReturn(Optional.empty());
        when(appointmentMapper.requestToAppointmentEntityDto(request)).thenReturn(mappedAppointment);
        when(therapistRepository.findById(Long.valueOf(3L))).thenReturn(Optional.of(therapist));
        when(caseRepository.findById(4L)).thenReturn(Optional.of(ptCase));
        when(appointmentRepository.save(mappedAppointment)).thenReturn(savedAppointment);
        when(appointmentMapper.entityToAppointmentResponseDto(savedAppointment)).thenReturn(response);

        // Act
        AppointmentResponseDto result = appointmentService.createAppointment(request);

        // Assert
        assertSame(response, result);
        assertEquals(Appointment.Status.SCHEDULED, mappedAppointment.getStatus());
        assertSame(therapist, mappedAppointment.getTherapist());
        assertSame(ptCase, mappedAppointment.getPtCase());
        assertNotNull(mappedAppointment.getCreatedAt());
        verify(appointmentRepository).findByScheduledAt(scheduledAt);
        verify(appointmentMapper).requestToAppointmentEntityDto(request);
        verify(therapistRepository).findById(Long.valueOf(3L));
        verify(caseRepository).findById(4L);
        verify(appointmentRepository).save(mappedAppointment);
        verify(appointmentMapper).entityToAppointmentResponseDto(savedAppointment);
    }

    @Test
    void shouldThrowConflict_whenCreateAppointmentFindsExistingAppointmentAtSameTime() {
        // Arrange
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 2, 13, 9, 0);
        AppointmentRequestDto request = new AppointmentRequestDto(
                scheduledAt,
                null,
                4L,
                3L,
                Appointment.Status.SCHEDULED,
                Appointment.Type.EVALUATION
        );

        when(appointmentRepository.findByScheduledAt(scheduledAt)).thenReturn(Optional.of(new Appointment()));

        // Act
        AppointmentAtTimeExistsException exception = assertThrows(AppointmentAtTimeExistsException.class,
                () -> appointmentService.createAppointment(request));

        // Assert
        assertTrue(exception.getMessage().contains("already exists"));
        verify(appointmentRepository).findByScheduledAt(scheduledAt);
        verify(appointmentMapper, never()).requestToAppointmentEntityDto(any(AppointmentRequestDto.class));
        verify(therapistRepository, never()).findById(any(Long.class));
        verify(caseRepository, never()).findById(any(Long.class));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldThrowException_whenCreateAppointmentTherapistDoesNotExist() {
        // Arrange
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

        when(appointmentRepository.findByScheduledAt(scheduledAt)).thenReturn(Optional.empty());
        when(appointmentMapper.requestToAppointmentEntityDto(request)).thenReturn(mappedAppointment);
        when(therapistRepository.findById(Long.valueOf(3L))).thenReturn(Optional.empty());

        // Act
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> appointmentService.createAppointment(request));

        // Assert
        assertNotNull(exception);
        verify(appointmentRepository).findByScheduledAt(scheduledAt);
        verify(appointmentMapper).requestToAppointmentEntityDto(request);
        verify(therapistRepository).findById(Long.valueOf(3L));
        verify(caseRepository, never()).findById(any(Long.class));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldThrowException_whenCreateAppointmentCaseDoesNotExist() {
        // Arrange
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

        when(appointmentRepository.findByScheduledAt(scheduledAt)).thenReturn(Optional.empty());
        when(appointmentMapper.requestToAppointmentEntityDto(request)).thenReturn(mappedAppointment);
        when(therapistRepository.findById(Long.valueOf(3L))).thenReturn(Optional.of(therapistWithId(3L)));
        when(caseRepository.findById(4L)).thenReturn(Optional.empty());

        // Act
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> appointmentService.createAppointment(request));

        // Assert
        assertNotNull(exception);
        verify(appointmentRepository).findByScheduledAt(scheduledAt);
        verify(appointmentMapper).requestToAppointmentEntityDto(request);
        verify(therapistRepository).findById(Long.valueOf(3L));
        verify(caseRepository).findById(4L);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldReturnAppointmentUiDtos_whenGetAppointmentsByDateFindsAppointments() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 2, 13);
        AppointmentScheduleProjection firstProjection = projection(
                21L, LocalDateTime.of(2026, 2, 13, 9, 0), 31L, "Sam", "Lee", 41L,
                "Taylor", "PHYSICAL_THERAPIST", "Shoulder", Appointment.Status.SCHEDULED,
                Appointment.Type.EVALUATION, "Lee, S");
        AppointmentScheduleProjection secondProjection = projection(
                22L, LocalDateTime.of(2026, 2, 13, 10, 0), 32L, "Alex", "Kim", 42L,
                "Morgan", "OCCUPATIONAL_THERAPIST", "Knee", Appointment.Status.CHECKED_IN,
                Appointment.Type.FOLLOW_UP, "Kim, A");
        AppointmentUiDto firstDto = uiDto(21L, firstProjection.getScheduledAt());
        AppointmentUiDto secondDto = uiDto(22L, secondProjection.getScheduledAt());

        when(appointmentRepository.findDailyAppointments(date)).thenReturn(List.of(firstProjection, secondProjection));
        when(appointmentMapper.toAppointmentUiDto(firstProjection)).thenReturn(firstDto);
        when(appointmentMapper.toAppointmentUiDto(secondProjection)).thenReturn(secondDto);

        // Act
        List<AppointmentUiDto> result = appointmentService.getAppointmentsByDate(date);

        // Assert
        assertEquals(List.of(firstDto, secondDto), result);
        verify(appointmentRepository).findDailyAppointments(date);
        verify(appointmentMapper).toAppointmentUiDto(firstProjection);
        verify(appointmentMapper).toAppointmentUiDto(secondProjection);
    }

    @Test
    void shouldReturnEmptyList_whenGetAppointmentsByDateFindsNoAppointments() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 2, 13);
        when(appointmentRepository.findDailyAppointments(date)).thenReturn(List.of());

        // Act
        List<AppointmentUiDto> result = appointmentService.getAppointmentsByDate(date);

        // Assert
        assertTrue(result.isEmpty());
        verify(appointmentRepository).findDailyAppointments(date);
        verify(appointmentMapper, never()).toAppointmentUiDto(any(AppointmentScheduleProjection.class));
    }

    @Test
    void shouldUpdateAppointment_whenUpdateAppointmentByIdReceivesFullPatch() {
        // Arrange
        Appointment appointment = appointmentWithId(9L);
        appointment.setScheduledAt(LocalDateTime.of(2026, 2, 13, 9, 0));
        Therapist therapist = therapistWithId(6L);
        Case ptCase = caseWithId(7L);
        AppointmentPatchDto request = patchRequest(
                LocalDateTime.of(2026, 2, 14, 11, 30), 17L, 16L, Appointment.Status.FINISHED);
        AppointmentResponseDto response = responseDto(9L);

        when(appointmentRepository.findById(9L)).thenReturn(Optional.of(appointment));
        when(therapistRepository.findById(Long.valueOf(16L))).thenReturn(Optional.of(therapist));
        when(caseRepository.findById(17L)).thenReturn(Optional.of(ptCase));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.entityToAppointmentResponseDto(appointment)).thenReturn(response);

        // Act
        AppointmentResponseDto result = appointmentService.updateAppointmentById(9L, request);

        // Assert
        assertSame(response, result);
        assertEquals(request.getScheduledAt(), appointment.getScheduledAt());
        assertSame(therapist, appointment.getTherapist());
        assertSame(ptCase, appointment.getPtCase());
        assertEquals(Appointment.Status.FINISHED, appointment.getStatus());
        assertNotNull(appointment.getModifiedAt());
        verify(appointmentRepository).findById(9L);
        verify(therapistRepository).findById(Long.valueOf(16L));
        verify(caseRepository).findById(17L);
        verify(appointmentRepository).save(appointment);
        verify(appointmentMapper).entityToAppointmentResponseDto(appointment);
    }

    @Test
    void shouldReturnCurrentAppointment_whenUpdateAppointmentByIdReceivesNullRequest() {
        // Arrange
        Appointment appointment = appointmentWithId(9L);
        AppointmentResponseDto response = responseDto(9L);

        when(appointmentRepository.findById(9L)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.entityToAppointmentResponseDto(appointment)).thenReturn(response);

        // Act
        AppointmentResponseDto result = appointmentService.updateAppointmentById(9L, null);

        // Assert
        assertSame(response, result);
        verify(appointmentRepository).findById(9L);
        verify(appointmentMapper).entityToAppointmentResponseDto(appointment);
        verify(therapistRepository, never()).findById(any(Long.class));
        verify(caseRepository, never()).findById(any(Long.class));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldUpdateOnlyProvidedFields_whenUpdateAppointmentByIdReceivesPartialPatch() {
        // Arrange
        Appointment appointment = appointmentWithId(9L);
        LocalDateTime originalScheduledAt = LocalDateTime.of(2026, 2, 13, 9, 0);
        appointment.setScheduledAt(originalScheduledAt);
        Therapist originalTherapist = therapistWithId(5L);
        Case originalCase = caseWithId(6L);
        appointment.setTherapist(originalTherapist);
        appointment.setPtCase(originalCase);
        appointment.setStatus(Appointment.Status.SCHEDULED);
        AppointmentPatchDto request = patchRequest(null, null, null, Appointment.Status.CHECKED_IN);
        AppointmentResponseDto response = responseDto(9L);

        when(appointmentRepository.findById(9L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.entityToAppointmentResponseDto(appointment)).thenReturn(response);

        // Act
        AppointmentResponseDto result = appointmentService.updateAppointmentById(9L, request);

        // Assert
        assertSame(response, result);
        assertEquals(originalScheduledAt, appointment.getScheduledAt());
        assertSame(originalTherapist, appointment.getTherapist());
        assertSame(originalCase, appointment.getPtCase());
        assertEquals(Appointment.Status.CHECKED_IN, appointment.getStatus());
        assertNotNull(appointment.getModifiedAt());
        verify(appointmentRepository).findById(9L);
        verify(appointmentRepository).save(appointment);
        verify(appointmentMapper).entityToAppointmentResponseDto(appointment);
        verify(therapistRepository, never()).findById(any(Long.class));
        verify(caseRepository, never()).findById(any(Long.class));
    }

    @Test
    void shouldThrowRuntimeException_whenUpdateAppointmentByIdDoesNotFindAppointment() {
        // Arrange
        AppointmentPatchDto request = patchRequest(LocalDateTime.now(), 1L, 2L, Appointment.Status.SCHEDULED);
        when(appointmentRepository.findById(9L)).thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> appointmentService.updateAppointmentById(9L, request));

        // Assert
        assertEquals("Appointment not found with id 9", exception.getMessage());
        verify(appointmentRepository).findById(9L);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldThrowRuntimeException_whenUpdateAppointmentByIdDoesNotFindTherapist() {
        // Arrange
        Appointment appointment = appointmentWithId(9L);
        AppointmentPatchDto request = patchRequest(null, null, 3L, null);

        when(appointmentRepository.findById(9L)).thenReturn(Optional.of(appointment));
        when(therapistRepository.findById(Long.valueOf(3L))).thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> appointmentService.updateAppointmentById(9L, request));

        // Assert
        assertEquals("Therapist not found with id 3", exception.getMessage());
        verify(appointmentRepository).findById(9L);
        verify(therapistRepository).findById(Long.valueOf(3L));
        verify(caseRepository, never()).findById(any(Long.class));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldThrowRuntimeException_whenUpdateAppointmentByIdDoesNotFindCase() {
        // Arrange
        Appointment appointment = appointmentWithId(9L);
        AppointmentPatchDto request = patchRequest(null, 4L, null, null);

        when(appointmentRepository.findById(9L)).thenReturn(Optional.of(appointment));
        when(caseRepository.findById(4L)).thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> appointmentService.updateAppointmentById(9L, request));

        // Assert
        assertEquals("Case not found with id 4", exception.getMessage());
        verify(appointmentRepository).findById(9L);
        verify(caseRepository).findById(4L);
        verify(therapistRepository, never()).findById(any(Long.class));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldDeleteAppointment_whenDeleteAppointmentByIdIsCalled() {
        // Arrange

        // Act
        appointmentService.deleteAppointmentById(15L);

        // Assert
        verify(appointmentRepository).deleteById(15L);
    }

    private Appointment appointmentWithId(Long id) {
        Appointment appointment = new Appointment();
        appointment.setScheduledAt(LocalDateTime.of(2026, 2, 13, 9, 0));
        appointment.setCreatedAt(Instant.parse("2026-02-01T12:00:00Z"));
        appointment.setStatus(Appointment.Status.SCHEDULED);
        setAppointmentId(appointment, id);
        return appointment;
    }

    private AppointmentPatchDto patchRequest(
            LocalDateTime scheduledAt, Long caseId, Long therapistId, Appointment.Status status
    ) {
        AppointmentPatchDto patch = new AppointmentPatchDto();
        setField(patch, "scheduledAt", scheduledAt);
        setField(patch, "caseId", caseId);
        setField(patch, "therapistId", therapistId);
        setField(patch, "status", status);
        return patch;
    }

    private void setField(AppointmentPatchDto patch, String fieldName, Object value) {
        try {
            var field = AppointmentPatchDto.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(patch, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private AppointmentResponseDto responseDto(Long id) {
        return new AppointmentResponseDto(
                id,
                LocalDateTime.of(2026, 2, 13, 9, 0),
                Instant.parse("2026-02-01T12:00:00Z"),
                4L,
                3L,
                Appointment.Status.SCHEDULED,
                Appointment.Type.EVALUATION
        );
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
            @Override
            public LocalDateTime getScheduledAt() {
                return scheduledAt;
            }

            @Override
            public Long getAptId() {
                return aptId;
            }

            @Override
            public Long getCaseId() {
                return caseId;
            }

            @Override
            public String getFirstName() {
                return firstName;
            }

            @Override
            public String getLastName() {
                return lastName;
            }

            @Override
            public Long getTherapistId() {
                return therapistId;
            }

            @Override
            public String getTherapistName() {
                return therapistName;
            }

            @Override
            public String getTherapistType() {
                return therapistType;
            }

            @Override
            public String getBodyRegionDisplayName() {
                return bodyRegionDisplayName;
            }

            @Override
            public Appointment.Status getStatus() {
                return status;
            }

            @Override
            public Appointment.Type getType() {
                return type;
            }

            @Override
            public String getDisplayName() {
                return displayName;
            }
        };
    }

    private void setAppointmentId(Appointment appointment, Long id) {
        try {
            var field = Appointment.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(appointment, id);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
