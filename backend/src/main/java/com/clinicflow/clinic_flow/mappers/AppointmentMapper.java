package com.clinicflow.clinic_flow.mappers;

import com.clinicflow.clinic_flow.dtos.AppointmentDto;
import com.clinicflow.clinic_flow.dtos.AppointmentUiDto;
import com.clinicflow.clinic_flow.entity.Appointment;
import com.clinicflow.clinic_flow.projections.AppointmentScheduleProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    Appointment toAppointmentEntity(AppointmentDto appointmentDto);
    @Mapping(target = "scheduledAt")
    AppointmentUiDto toAppointmentDto(Appointment appointment);
    AppointmentUiDto toAppointmentUiDto(AppointmentScheduleProjection appointmentScheduleProjection);
}
