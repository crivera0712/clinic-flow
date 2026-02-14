package com.clinicflow.clinic_flow.mappers;

import com.clinicflow.clinic_flow.dtos.AppointmentRequest;
import com.clinicflow.clinic_flow.dtos.AppointmentResponse;
import com.clinicflow.clinic_flow.dtos.AppointmentUiDto;
import com.clinicflow.clinic_flow.entity.Appointment;
import com.clinicflow.clinic_flow.projections.AppointmentScheduleProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    Appointment requestToAppointmentEntity(AppointmentRequest request);

    AppointmentResponse entityToAppointmentResponse(Appointment appointment);

    AppointmentUiDto toAppointmentUiDto(AppointmentScheduleProjection projection);
}
