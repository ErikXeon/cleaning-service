package com.konalyan.cleaning.cleaning_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CleaningServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CleaningServiceApplication.class, args);
	}

}
