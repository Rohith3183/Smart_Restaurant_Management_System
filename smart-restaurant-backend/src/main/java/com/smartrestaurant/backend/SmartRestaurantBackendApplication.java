package com.smartrestaurant.backend;

import org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(exclude = {ChatClientAutoConfiguration.class})
public class SmartRestaurantBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartRestaurantBackendApplication.class, args);
	}
}
