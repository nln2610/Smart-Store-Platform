package com.smartstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing  // ⭐ Bật tính năng tự set createdAt/updatedAt
public class SmartStoreApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmartStoreApplication.class, args);
	}
}