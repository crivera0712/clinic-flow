package com.clinicflow.clinic_flow.mappers;

import com.clinicflow.clinic_flow.dtos.PatientDto;
import com.clinicflow.clinic_flow.entity.Patient;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientDto toPatientDto(Patient patient);
}
