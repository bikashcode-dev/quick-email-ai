package com.email.emailgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class EmailgenApplication {
	public static void main(String[] args) {
		SpringApplication.run(EmailgenApplication.class, args);
	}
}
