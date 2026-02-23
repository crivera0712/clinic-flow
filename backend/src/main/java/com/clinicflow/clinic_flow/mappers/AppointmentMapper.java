package com.clinicflow.clinic_flow.mappers;

import com.clinicflow.clinic_flow.dtos.appointments.AppointmentRequestDto;
import com.clinicflow.clinic_flow.dtos.appointments.AppointmentResponseDto;
import com.clinicflow.clinic_flow.dtos.appointments.AppointmentUiDto;
import com.clinicflow.clinic_flow.entity.Appointment;
import com.clinicflow.clinic_flow.projections.AppointmentScheduleProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    Appointment requestToAppointmentEntityDto(AppointmentRequestDto request);

    @Mapping(target = "caseId", source = "ptCase.id")
    @Mapping(target = "therapistId", source = "therapist.id")
    AppointmentResponseDto entityToAppointmentResponseDto(Appointment appointment);

    AppointmentUiDto toAppointmentUiDto(AppointmentScheduleProjection projection);


}
