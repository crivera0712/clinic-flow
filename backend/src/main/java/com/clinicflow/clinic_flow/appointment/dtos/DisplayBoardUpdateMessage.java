package com.clinicflow.clinic_flow.appointment.dtos;

import java.time.LocalDate;

public record DisplayBoardUpdateMessage (String type, LocalDate date){
}
