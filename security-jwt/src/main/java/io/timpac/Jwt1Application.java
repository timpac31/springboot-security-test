package io.timpac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Jwt1Application {

	public static void main(String[] args) {
		SpringApplication.run(Jwt1Application.class, args);
	}

}
