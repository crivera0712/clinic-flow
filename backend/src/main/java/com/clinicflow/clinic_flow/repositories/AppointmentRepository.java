package com.clinicflow.clinic_flow.repositories;

import com.clinicflow.clinic_flow.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    //@Query(value ="select * " )
   // List<Appointment>findAllByStartTime(LocalTime startTime);
}