package com.clinicflow.clinic_flow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ClinicFlowApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(ClinicFlowApplication.class, args);
		//var service = context.getBean(AppointmentRepository.class);
	}

}
