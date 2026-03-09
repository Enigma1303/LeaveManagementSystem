package com.aryan.springboot.leavemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@SpringBootApplication
public class LeavemanagementApplication {

	public static void main(String[] args) {

		//System.out.println(new BCryptPasswordEncoder().encode("admin123"));
		SpringApplication.run(LeavemanagementApplication.class, args);
	}

}
