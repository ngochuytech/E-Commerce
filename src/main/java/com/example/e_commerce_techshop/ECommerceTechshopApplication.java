package com.example.e_commerce_techshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableMongoAuditing
public class ECommerceTechshopApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceTechshopApplication.class, args);
	}

}
