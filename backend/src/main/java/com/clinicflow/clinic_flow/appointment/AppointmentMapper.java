package com.clinicflow.clinic_flow.appointment;

import com.clinicflow.clinic_flow.appointment.dtos.AppointmentRequestDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentResponseDto;
import com.clinicflow.clinic_flow.appointment.dtos.AppointmentUiDto;
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
