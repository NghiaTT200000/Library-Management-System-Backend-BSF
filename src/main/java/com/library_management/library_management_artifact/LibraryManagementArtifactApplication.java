package com.library_management.library_management_artifact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LibraryManagementArtifactApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraryManagementArtifactApplication.class, args);
	}
	
}
