package com.example.apigateway;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

@SpringBootApplication
public class ApiGatewayApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
