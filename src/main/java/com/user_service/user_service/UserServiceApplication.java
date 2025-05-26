package com.user_service.user_service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "USER SERVICE API",
				version = "1.0",
				description = "This service handles the management of user registration, authentication, and profile operations.",
				contact = @io.swagger.v3.oas.annotations.info.Contact(
						name = "Isaac Hagan",
						email = "isaachagan320@gmail.com"
				),
				termsOfService = "http://swagger.io/terms/",
				license = @io.swagger.v3.oas.annotations.info.License(
						name = "Apache 2.0",
						url = "http://springdoc.org"
				)
		)
)
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
