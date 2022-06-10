package io.timpac;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import io.timpac.jwt.JwtProperties;

@SpringBootTest
class Jwt1ApplicationTests {

	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private JwtProperties jwtProperties;
	
	@Test
	void configurationPropertyMapping() {
		System.out.println(jwtProperties.getSecretKey());
		System.out.println(jwtProperties.getExpiredMilliSeconds());
	}

	@Test
	void contextLoads() {
		System.out.println("============= Spring Bean List ====================");
		for(String bean : context.getBeanDefinitionNames()) {
			System.out.println(bean);
		}
		System.out.println("============= Spring Bean List ====================");
	}

}
