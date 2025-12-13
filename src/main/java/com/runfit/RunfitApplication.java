package com.runfit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RunfitApplication {

	public static void main(String[] args) {
		SpringApplication.run(RunfitApplication.class, args);
	}

}
