package com.clinicflow.clinic_flow.patient;

import com.clinicflow.clinic_flow.patient.dtos.PatientRequestDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientResponseDto toPatientResponseDto(Patient patient);

    Patient toPatient(PatientRequestDto patientRequestDto);
}
