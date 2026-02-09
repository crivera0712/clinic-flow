package com.clinicflow.clinic_flow.mappers;

import com.clinicflow.clinic_flow.dtos.AppointmentDto;
import com.clinicflow.clinic_flow.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    @Mapping(target = "scheduledAt")
    //@Mapping(target = "createdAt")
    //@Mapping(target = "modifiedAt")
    AppointmentDto toAppointmentDto(Appointment appointment);
}
