package com.clinicflow.clinic_flow.mappers;

import com.clinicflow.clinic_flow.dtos.patients.PatientRequestDto;
import com.clinicflow.clinic_flow.dtos.patients.PatientResponseDto;
import com.clinicflow.clinic_flow.entity.Patient;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientResponseDto toPatientResponseDto(Patient patient);
    Patient toPatient(PatientRequestDto patientRequestDto);
}
