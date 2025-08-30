package com.nirmaan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NirmaanStudentManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(NirmaanStudentManagementApplication.class, args);
	}

}
